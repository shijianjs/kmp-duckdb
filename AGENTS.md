# AGENTS.md

本文件记录跨会话需要保留的项目约定：构建方式、模块结构、建模规范、发布与文档流程、容易踩的坑。只与某个
目录强相关的说明放在该目录下的说明文件里，这里保留指向它的简短索引。任务进度不写在这里：总览在根
[`TODO.md`](TODO.md)，明细在 [`docs/development/`](docs/development/)。

## 本地开发环境

- 仓库按 `<workspace>/<owner>/<repo>` 存放；目录约定、自有项目清单与可参考的第三方克隆见上级目录的
  `AGENTS.md`，查资料前先读它。
- 上游资料一律读本地克隆，**不要访问 `raw.githubusercontent.com`，也不要用网页抓取 GitHub**：国内网络访问
  这些域名经常超时。
- 查上游 API 的语义信息（父类型链、实现列表、方法覆盖）优先走 IDE 的 MCP server，而不是只 grep 源码：
  两个 server 的分工、`scope: project_and_libraries` 之类的坑与现成脚本见
  [`.agents/skills/intellij-mcp-plugins/`](.agents/skills/intellij-mcp-plugins/SKILL.md)。

## 项目概况与模块结构

`kmp-duckdb` 为 DuckDB 与 Apache Arrow 的 npm 包提供 Kotlin/JS wrapper（本阶段只做 JS 目标）。一个上游 npm
包 = 一个 Gradle 模块 = 一个 Kotlin 包：

| 模块 | npm 包 | 上游源码 clone | Kotlin 包 |
| --- | --- | --- | --- |
| `ktjs-apache-arrow` | `apache-arrow@21.2.0` | `S:\workspace\github\apache\arrow-js` | `io.github.shijianjs.ktjs.duckdb.arrow` |
| `ktjs-duckdb-wasm` | `@duckdb/duckdb-wasm@1.32.0` | `S:\workspace\github\duckdb\duckdb-wasm`（tag `v1.32.0`） | `io.github.shijianjs.ktjs.duckdb.wasm` |
| `ktjs-duckdb-node-neo` | `@duckdb/node-api@2.0.0-r.1` | `S:\workspace\github\duckdb\duckdb-node-neo` | `io.github.shijianjs.ktjs.duckdb.node` |

- `demo`：演示页，发布在 Pages 的 `/demo/`，不发布到 Maven、也不进 Dokka。浏览器卡片用 wasm 模块，Arrow
  卡片用 arrow 模块；页面内所有文案都走 `Translations`，中英文就地切换不重载。
- `docs`：文档站，也是 Kotlin/JS 模块——站点配置与侧边栏是 Kotlin（`docs/src/jsMain/kotlin/.../Config.kt`），
  页面是 Markdown（`docs/src/jsMain/resources/docs/**`，中文副本在
  `docs/src/jsMain/resources/i18n/zh-Hans/docusaurus-plugin-content-docs/current/**`），由
  [ktjs-docusaurus](https://github.com/shijianjs/ktjs-docusaurus) 的 Gradle 插件组装并调用 Docusaurus CLI。
  `baseUrl` 是 `/kmp-duckdb/`，导航里 Demo 与 API 的跳转由 `Config.kt` 的常量推出来，改布局要一起改。
- Logo 只有一份（`docs/src/jsMain/resources/static/img/kmp-duckdb-logo.png`），demo 用 `brandAssets` 任务拷进
  自己的资源；DuckDB 的 wasm 与 worker 由 `duckdbAssets` 从安装好的 npm 包拷入，页面不依赖 CDN。
- 落地页 `docs/src/jsMain/resources/pages/index.mdx` 用 `ktjs-docusaurus-kit` 的 `<ktjs-hero>` / `<ktjs-features>` / `<ktjs-next-steps>`；
  这些元素和右侧目录折叠 `TocToggle` 的浏览器 bundle 由 `kitBundle` + `extractKitBundle` 拷到 `static/ktjs-client/`，
  再由 `ClientModule.kt` 在 hydration 之后加载（`Config.kt` 的 `clientModules` 列出生成出来的 `./client-module.mjs`）。

## 构建与测试

```bash
./gradlew build                              # 编译、Detekt、浏览器测试
./gradlew :ktjs-apache-arrow:jsBrowserTest   # 单个模块的浏览器测试
./gradlew :ktjs-duckdb-node-neo:jsNodeTest   # node 模块跑在 Node 里，不需要浏览器
./gradlew :demo:e2eTest                      # Playwright 套件，跑打包后的演示页
./gradlew :docs:docusaurusBuild              # 文档站
./gradlew pagesSite                          # 文档站（根）+ demo（/demo/）+ Dokka（/api/）
```

- 只有一个测试方法时用 `--tests '*methodName*'`。
- `demo` 的 JVM 目标只承载 e2e 套件，`jvmTest` 被禁用，所以 `build` 不依赖浏览器。
- Playwright 默认从 `cdn.playwright.dev` 下载浏览器，国内经常 0 速度。本机把下载源指到国内镜像：
  `$env:PLAYWRIGHT_DOWNLOAD_HOST='https://npmmirror.com/mirrors/playwright/'`，然后 `./gradlew --stop` 让新
  daemon 继承变量再跑安装任务。**只在本机生效，不要提交到仓库，也不要写进工作流——CI 走官方源。**

## 上游 API 建模约定

wrapper 的目标是让上游 npm 包暴露的公开 API 在 Kotlin/JS 侧都能用，而不是只覆盖几个常用入口。动手前先读
本地克隆的 `.d.ts` 与源码：导出列表就是范围清单。

- 完整规范读技能 [`ktjs-type-mapping`](.agents/skills/ktjs-type-mapping/SKILL.md)（完整对照表在其
  `references/type-mapping.md`）。摘要：上游写了类型，Kotlin 侧就必须是被编译器检查的类型；`JsAny` 只用于
  上游的 `any` / `unknown` / `object`，并在 KDoc 里写明原因；`dynamic` 不进入公开 API。
- 联合类型 = 不透明 `external interface` + 每个成员一个同名工厂；字符串字面量 = `sealed external interface`
  + `Xxx(String)` 工厂 + `Xxxs` 常量对象；数值枚举 = `sealed external interface` + `Double` 工厂 + 常量对象
  （`Types.Int32`、`LogLevels.WARNING`），**不要** `enum class`。
- 对象字面量用 `js.objects.unsafeJso`；集合用 `Array` / `Record` / `JsSet`；异步是 `Promise<T>`；
  回调是 Kotlin 函数类型；DOM 类型用 `web.*`，禁止 `org.w3c.dom.*` 与 `kotlinx.browser.*`（Detekt 会拦）。
- 类型名与 Kotlin 冲突时用 `@JsName` 保留线上名字（`IntType` → `@JsName("Int")`、`ListType` →
  `@JsName("List")`）。
- 每个公开声明都要 KDoc：上游有说明就翻译，没有就按源码行为写。

### Kotlin/JS 声明的硬性约束（踩过，别再踩）

- **`@file:JsModule` 文件只能放 external 声明**。工厂函数、常量 `object`、`typealias` 一律拆到普通文件：
  arrow 在 `ArrowSupport.kt`，wasm 在 `WasmSupport.kt`，node 在 `NodeApiAccess.kt` / `Results.kt`。
- 上游类是 CommonJS 包（`@duckdb/node-api`）时，具名导入在 ESM 下报 `Named export ... not found`。改为
  `@JsName("default") external val duckdbModule: DuckDBModule`（default 导出就是这个命名空间对象），再用普通
  `object DuckDBNodeApi` 转发；静态成员放 `external interface`，因为 `external class` 不能带 companion。
- `external class X : Logger` 会报「未实现抽象成员」。上游接口只当结构描述用，实现由
  `unsafeCast` 出来的普通函数返回：`voidLogger()` / `consoleLogger(level)`。
- `js.typedarrays` 的 `TypedArray` 需要 4 个类型参数，`ArrayBufferLike` 在 **`js.buffer`** 而不是
  `js.typedarrays`：项目里统一用 `typealias ArrowTypedArray = TypedArray<*, *, *, *>` 与
  `typealias ArrowBytes = Uint8Array<*>`，构造写 `Int32Array<ArrayBufferLike>(n)`，`set` 取 `Int`。
- 基类要被 `external class` 继承时必须是 `external open class`（`Vector<T>`）。

### 上游行为里容易误解的几处（写测试前先看）

- Arrow 的 `Int_` 子类（`Int8` … `Uint64`）`typeId` 都是共享的 `Type.Int`（2），宽度只在 `bitWidth` 上；
  因此 `VectorName` 是 `IntVector` 而不是 `Int32Vector`。`Types.Int32`（-4）只用于比较类型实例本身。
- `Table.toString()` 输出的是行的 JSON，不是 `Table<...>`；`Vector.slice(offset, length)` 从 `offset` 起。
- `@duckdb/node-api` 的 `DuckDBInstance.create(null)` 抛 `TypeError: A string was expected`，内存库写
  `":memory:"`。
- `range()` 的列是 `BIGINT`（JS `bigint`），与 Kotlin `Double` 直接 `assertEquals` 会得到
  `Expected <4>, actual <4>` 这类诡异消息；断言按 `toString()` 比较，或显式转换。

## 版本约定

每个模块独立版本，格式 `{上游 npm 版本}-kt.{修订号}`。上游版本只在根 `package.json` 的 `dependencies` 里
声明；修订号在根 `wrapper-revisions.properties`；根 `build.gradle.kts` 解析后放入 `extra["npmVersions"]` /
  `extra["wrapperRevisions"]`，模块脚本据此声明 `npm(...)` 并推导版本。禁止在模块脚本里硬编码版本。

- 只发布带 `-kt.N` 的版本；工作树一律再带 `-SNAPSHOT`，`-Pktjs.release=true` 去掉后缀（发布工作流会传）。
- 修订号按模块独立递增、永不重置；只提升实际变更模块的修订号，发布命令里也只列这些模块。
- 仅上游版本变化时修订号不变，基础版本自动跟随。
- 提升修订号、新增 npm 依赖都会改生成的 workspace 包版本与依赖图，**必须同时刷新锁文件**：
  先在官方源下 `$env:npm_config_registry='https://registry.npmjs.org/'`、`./gradlew --stop`，再
  `./gradlew kotlinUpgradePackageLock --rerun-tasks`，确认 diff 只有预期变化后一起提交。否则 CI 在
  `:kotlinStorePackageLock` 报 `Lock file was changed`。

## 仓库与本地镜像

仓库里提交的构建配置只用官方源：`settings.gradle.kts` 用 Gradle Plugin Portal 与 Maven Central，wrapper 用
`services.gradle.org`。这样 Actions 与任何克隆者的行为一致。

国内加速只在本机生效：Gradle 走 `~/.gradle/init.d/aliyun-mirror.gradle`，npm 走 `~/.npmrc` 的
`registry=https://registry.npmmirror.com/`。不要把这俩地址写进仓库或工作流。已锁定的条目不会因镜像变脏
（npm 只替换下载主机），但锁里没有的新包会写成镜像地址，所以刷新锁必须先切回官方源（见上一节）。

`kotlin-wrappers` 在阿里云镜像上同步滞后，Gradle 找到元数据就不会回退，因此 `settings.gradle.kts` 用
`exclusiveContent` 把 `org.jetbrains.kotlin-wrappers` 与 `kotlin-js-plain-objects` 固定到 Maven Central。

## CI 与发布

- `.github/workflows/ci.yml`：所有 PR + `workflow_dispatch`，跑 `./gradlew build`、`pagesSite`、
  `:demo:e2eTest`。Dependabot 的 PR 先 `kotlinUpgradePackageLock` 并把锁文件作为 artifact 上传
  （`dependabot-auto-merge.yml` 下载后提交再 squash 合并）。
- `.github/workflows/pages.yml`：只有 `workflow_dispatch`，`pagesSite` 后用 `deploy-pages` 发布。仓库
  Settings → Pages 的 Source 必须是 **GitHub Actions**（`gh api -X POST repos/<owner>/<repo>/pages -f build_type=workflow`）。
- `.github/workflows/release.yml`：两段式发布，本仓库只签名与打归档，Central 上传交给
  `shijianjs/maven-central-publishing` hub。新增 KMP 模块（产生新的 `*-js` 坐标）时先在 hub 的
  `artifactOverrides` 里补 `jar: false` 并关闭 `verifyPom`。
- Dokka 聚合任务在 2.x 是 `dokkaGeneratePublicationHtml`，输出 `build/dokka/html`；`pagesSite` 把它放到
  `/api/`。非发布模块（`demo`、`docs`）不进 Dokka。

## 演示页与文档

- 演示页的外壳（顶栏、抽屉菜单、路由、语言与主题菜单、页面容器）来自
  `io.github.shijianjs:ktjs-demo-kit-js`，demo 只写「这一页演示什么」。外壳组件 `DemoLayout` 由
  `index.html` 里的 `<ktjs-demo-layout id="demo">` 命名，`main` 只负责 `define()` 与 `mount(...)`；
  不要再自己拼 navbar、抽屉或页面容器，也不要往页面里塞外壳级别的样式。
- 页面类实现 `Page`：`newPage()` 建内容（`setTitle` / `setSummary` / `addLink` / `addSheet`），
  `applyTranslation(lang)` 就地改写。外壳按路由缓存页面实例，所以控制台页的数据库、输入框与上一次结果
  在切走再切回时都还在——不要用「一处文案变化就重建整页」的写法，那会丢掉结果。
- 演示页的所有可见文案都注册进 `Translations`（en/zh 成对），切换时**就地改写文本**，不重载页面——重载会
  丢掉已经跑出来的查询结果。新增文案必须同时给两种语言，否则中文界面会露出英文。组件属性（
  `text-area` 的 `label`、按钮的 `textContent`）不经过 `Binder`，在 `applyTranslation` 里显式赋值。
- 状态行（`Readout`）报告的是状态而不是文案：语言切换时按当前状态重算一次，不要为它绑 `Binder`。
- 文档站每个页面都要有中英两份：英文在 `docs/.../docs/`，中文在
  `docs/.../i18n/zh-Hans/docusaurus-plugin-content-docs/current/`，路径与文件名一一对应。新增页面还要在
  `Config.kt` 的 `sidebars()` 里登记。
- `onBrokenLinks = throw`：导航与正文里的链接必须可解析，`/demo/` 与 `/api/` 用绝对 URL（相对路径会落到
  locale 前缀下）。
- 文档以「怎么用」为主，API 细节交给 Dokka：每页给可运行的片段，不抄整份签名清单。
- **资源路径不要用 `document.baseURI` 拼**：外壳用 hash 路由，在 `#/console` 上它读作
  `http://host/#/console`，拼出来的 URL 会把路径接到 fragment 后面，worker 拿到的就是本页 HTML
  （报错形如 `Uncaught SyntaxError: Unexpected token '<'`）。按 `location.origin` + `location.pathname`
  的目录部分解析，见 `demo/duckdb/DuckDb.kt` 的 `assetUrl`。
- 演示页的构建要点：`compilerOptions { target = "es2015" }`（自定义元素在 ES5 产物里无法升级）、
  `copyLumoStyles` 从 npm 工作区复制 `lumo.css` / `utility.css`、`duckdbAssets` 从
  `@duckdb/duckdb-wasm/dist` 复制 wasm 与 worker（页面不依赖 CDN）。

## 长期任务跟踪

跨上下文的任务状态写在仓库里，不依赖聊天记录：根 [`TODO.md`](TODO.md) 只放总览与统计（总数 = 已完成 +
明确不做 + 待完成），明细清单在 [`docs/development/`](docs/development/)。规范见
[`.agents/skills/long-task-tracking/`](.agents/skills/long-task-tracking/SKILL.md)。每完成一批事项就更新
统计与待办；排除项必须写具体理由，不能把「还没做」记成「明确不做」。
