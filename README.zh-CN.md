# kmp-duckdb

<p align="center"><img src="docs/src/jsMain/resources/static/img/kmp-duckdb-logo.svg" width="120" alt="kmp-duckdb"></p>

[DuckDB](https://duckdb.org/) 与 [Apache Arrow](https://arrow.apache.org/) 的 Kotlin/JS 封装：浏览器里跑的
WASM 版本、Node 原生客户端，以及两者共用的 Arrow 包。每个封装都按上游 `.d.ts` 手写，并由真正调用上游库的
测试把关。

[English](README.md) | [简体中文](README.zh-CN.md)

**[文档](https://shijianjs.github.io/kmp-duckdb/zh-Hans/)** ·
**[在线演示](https://shijianjs.github.io/kmp-duckdb/demo/)** ·
**[API 参考](https://shijianjs.github.io/kmp-duckdb/api/)**

## 模块

| 模块 | npm 包 | 封装版本 |
| --- | --- | --- |
| [ktjs-apache-arrow](ktjs-apache-arrow/README.zh-CN.md) | `apache-arrow` 21.2.0 | `21.2.0-kt.1` |
| [ktjs-duckdb-wasm](ktjs-duckdb-wasm/README.zh-CN.md) | `@duckdb/duckdb-wasm` 1.32.0 | `1.32.0-kt.1` |
| [ktjs-duckdb-node-neo](ktjs-duckdb-node-neo/README.zh-CN.md) | `@duckdb/node-api` 2.0.0-r.1 | `2.0.0-r.1-kt.1` |

Arrow 排在最前是有原因的：它不是 DuckDB 的包，但 DuckDB 的查询结果就是 Arrow 表，Kotlin 使用方要读一行
数据就得先有这个模块。这也是 `ktjs-duckdb-wasm` 依赖它、而 `ktjs-duckdb-node-neo` 不依赖它的原因——Node
客户端返回的是原生 JS 值，不是 Arrow。

## 使用

```kotlin
dependencies {
    implementation("io.github.shijianjs:ktjs-duckdb-wasm:1.32.0-kt.1")
}
```

封装自己带上了上游 npm 包，不需要再单独声明 npm 依赖。浏览器与 Node 的完整查询见
[快速开始](https://shijianjs.github.io/kmp-duckdb/zh-Hans/guide/getting-started)，演示页可以直接跑。

## 版本约定

每个模块的版本是 `{上游 npm 版本}-kt.{修订号}`。上游版本只在根 [`package.json`](package.json) 里声明一次，
修订号按模块记录在 [`wrapper-revisions.properties`](wrapper-revisions.properties)。只发布 `-kt.N` 版本，
工作树额外带 `-SNAPSHOT`，修订号只增不减。

## 环境要求

- JDK 21 或更高（构建使用 Gradle 9.4.1）。
- Node 与 npm 由 Kotlin Gradle 插件自动下载；`kotlin.js.yarn=false` 让 npm 继续作为包管理器。
- 第一次跑浏览器测试时由 Playwright 下载 Chromium。

## 构建与测试

```bash
./gradlew build                              # 编译、Detekt、浏览器测试
./gradlew :ktjs-duckdb-node-neo:jsNodeTest    # Node 客户端，不需要浏览器
./gradlew :demo:e2eTest                       # Playwright 跑打包后的演示页
./gradlew :docs:docusaurusBuild               # 文档站
./gradlew pagesSite                           # 文档在根、demo 在 /demo/、Dokka 在 /api/
```

## 仓库结构

- `ktjs-apache-arrow`、`ktjs-duckdb-wasm`、`ktjs-duckdb-node-neo`：发布的模块。
- `demo`：浏览器演示页，含实时 SQL 控制台与 Arrow 往返，中英文切换。
- `docs`：文档站，同样中英双语，也是 Pages 站点的入口。
- `docs/development`：任务与覆盖清单（见 [`TODO.md`](TODO.md)）。

## 许可证

以 [MIT License](LICENSE) 发布。
