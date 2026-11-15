# Technical decisions

本文件记录本仓库里「为什么这样做」的结论，避免后续上下文重复讨论。

## 模块划分：按运行环境拆，而不是按 npm 包拆

三个上游包分别活在浏览器、worker 与 Node 里，而且发布节奏不同：

- `apache-arrow` 是纯 TypeScript，哪都能跑；
- `@duckdb/duckdb-wasm` 需要浏览器 worker + `.wasm` 二进制；
- `@duckdb/node-api` 加载原生 addon，只能在服务端。

一个模块一个 npm 包，浏览器页面不会拉进原生 addon 的类型声明，Node 服务也不会拉进无法实例化的 wasm。
`ktjs-duckdb-wasm` 依赖 `ktjs-apache-arrow`（结果是 Arrow 表），`ktjs-duckdb-node-neo` 不依赖它（结果是原生
JS 值）。这条依赖关系是**故意不对称**的，不要为了「统一」给 node 模块也加 arrow 依赖。

## 版本锁定：duckdb-wasm 用 `1.32.0` 而不是 `latest`

npm 的 `dist-tags.latest` 指向 `1.33.1-dev57.0` 一类的开发版，stable 线最高是 `1.32.0`。wrapper 跟 stable 走：
上游 API 在 dev 版上会变，跟着 dev 走意味着每次 wrapper 发布都对着一个未定型的接口。本地克隆检出在 tag
`v1.32.0`，声明按它建模；若要升级，先切到目标 tag 重新核对 `.d.ts`。

## CommonJS 包用 default 导入

`@duckdb/node-api` 的导出对象在运行时组装，Node 的 ESM 静态分析看不到具名导出，于是
`import { DuckDBInstance } from '@duckdb/node-api'` 直接报 `Named export ... not found`。Kotlin 的
`@file:JsModule` + `external object` 生成的就是具名导入，所以改成：

```kotlin
@JsName("default")
external val duckdbModule: DuckDBModule
```

default 导出就是这个命名空间对象，类型仍是 `DuckDBModule`，成员访问依旧被编译器检查；对外再由普通
`object DuckDBNodeApi` 转发，调用方写起来与 TypeScript 侧一致。

## 静态成员放 `external interface`

Kotlin 的 `external class` 不能带 companion，而 `DuckDBInstance.create` / `DuckDBConnection.create` 是类的静态
成员。做法是把「类值」声明成 `external interface`（`DuckDBInstanceStatics`），实例侧仍是 `external interface`
（`DuckDBInstance`）。这与 kotlin-wrappers 处理上游静态成员的方式一致。

## 接口型参数不用 `external class : Interface` 实现

`external class VoidLogger : Logger` 会让编译器报「未实现抽象成员」。上游的 `Logger` 只是结构描述，实现交给
普通函数返回 `unsafeCast` 出来的对象：`voidLogger()`、`consoleLogger(level)`。这样既保留了 `Logger` 作为字段
类型的检查，又不必伪造一个 Kotlin 实现。

## `@file:JsModule` 文件只放 external 声明

Kotlin 的硬规则：带 `@file:JsModule` 的文件里不能有 `inline fun`、常量 `object`、`typealias`。它们全部拆到
普通文件：

- arrow → `ArrowSupport.kt`（联合工厂、常量对象、类型别名、扩展属性）
- wasm → `WasmSupport.kt`（`LogLevels` 等常量、`voidLogger` / `consoleLogger`）
- node → `NodeApiAccess.kt`（`DuckDBNodeApi` 转发）、`Results.kt`（值类型别名与结果类）

## 数值枚举用 sealed interface + 常量对象

`Type`、`LogLevel` 这些上游是数值枚举。Kotlin `enum class` 会编译成对象/序号，赋给 JS 属性立刻类型不对；
`const val Double` 拼错不报错。因此统一 `sealed external interface` + `X(String|Double)` 工厂 + `Xxxs` 常量
对象，测试逐条断言常量数值与上游一致。

## WASM 资源从 npm 包拷，不用 CDN

demo 的 `duckdbAssets` 任务把 `duckdb-mvp.wasm` / `duckdb-eh.wasm` 与两个 worker 脚本从
`build/js/node_modules/@duckdb/duckdb-wasm/dist` 拷到 `generated/duckdb-assets/duckdb/`。理由：页面要在离线与
内网可用，e2e 也不该依赖外网。页面里引用时用**绝对 URL**（`document.baseURI + "duckdb/..."`）——worker 内部
的相对路径会以 worker 脚本目录为基准，裸相对路径会 404，`WebAssembly.compile` 报
`HTTP status code is not ok`（踩过）。

## 演示页的语言切换就地改写文本

`Translations` 注册 `(element, en, zh)` 三元组，切换时只改 `textContent`，不重载页面——重载会丢掉已经跑出来的
查询结果与 Arrow 表格。新增文案必须成对提供两种语言。

## 文档站：单模块、Kotlin 配置 + Markdown 页面

`docs` 是 Kotlin/JS 模块，`Config.kt` 写站点配置与侧边栏，页面是 Markdown；中文副本放
`i18n/zh-Hans/docusaurus-plugin-content-docs/current/`，路径与英文一一对应。不用 ktjs-docusaurus 的
kit bundle：本项目用不到它的落地页组件，去掉后 `docs/build.gradle.kts` 只剩 `sidebarsFunction`，
也就没有 `extractKitBundle` 与客户端模块那一层。`gradle.properties` 里 `ktjs-docusaurus-kit.version`
因此不再被引用。

`onBrokenLinks = throw` 保留：链接坏掉就地失败比带着坏链上线好。中文页面的 front matter 必须与英文一致
（`id` + `slug: /`），否则相对链接会按中文页的 slug 解析到不存在的目录（踩过，`zh-Hans/lang/guide/...`）。

## demo e2e 用 Playwright + Hutool，JVM 目标只承载测试

演示页是 JS 产物，e2e 在 `demo` 的 JVM 目标里跑：Playwright 驱动 Chromium，`DemoServer` 用 Hutool 的
`SimpleServer` 在本进程内提供 `dist/js/productionExecutable`。`jvmTest` 被禁用，所以 `build` 不依赖浏览器；
`e2eTest` 单独存在并被 CI 单独调用。Playwright Java 版本必须与 KGP 内置的 npm playwright 一致（1.61.0），
否则找不到浏览器。

`DemoOptions` 里把 locale 固定成 `en-US`：页面按 `navigator.language` 选语言，跟着机器 locale 走会让断言在
不同环境下时中时英。

## 浏览器测试用新版 DSL，Node 测试单独跑

`jsBrowserTest` 由 Playwright 安装的 Chromium 驱动；`ktjs-apache-arrow` 的测试覆盖枚举与真实读写，
`ktjs-duckdb-node-neo` 的 `jsNodeTest` 在真实 Node 里跑（不需要浏览器）。两者都不放在
`src/jsTest` 之外的地方，避免一份测试在两个 target 上重复执行。
