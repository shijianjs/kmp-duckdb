# ktjs-duckdb-wasm

[`@duckdb/duckdb-wasm`](https://www.npmjs.com/package/@duckdb/duckdb-wasm) 1.32.0 的 Kotlin/JS 声明：
异步数据库句柄、连接、预处理语句、浏览器文件注册表与 bundle 选择辅助。发布坐标为
`io.github.shijianjs:ktjs-duckdb-wasm:1.32.0-kt.1`。

[English](README.md) | [简体中文](README.zh-CN.md)

- [文档](https://shijianjs.github.io/kmp-duckdb/zh-Hans/guide/duckdb-wasm)
- [在线演示](https://shijianjs.github.io/kmp-duckdb/demo/) —— 该页面就是在浏览器里跑这个封装
- [API 参考](https://shijianjs.github.io/kmp-duckdb/api/)

```kotlin
val worker = Worker(bundle.mainWorker!!)
val db = AsyncDuckDB(consoleLogger(LogLevels.WARNING), worker)

db.instantiate(bundle.mainModule, bundle.pthreadWorker)
    .then { db.open(unsafeJso<DuckDBConfig> { path = ":memory:" }) }
    .then { db.connect() }
```

`query()` 解析为 [`ktjs-apache-arrow`](../ktjs-apache-arrow/README.zh-CN.md) 的 Arrow `Table`，因此本模块
依赖该模块，结果也通过它读取。

页面需要自己提供 `.wasm` 与 worker 脚本：`getJsDelivrBundles()` 或自备 `DuckDBBundles` 对象都可用，
demo 模块的 `duckdbAssets` 任务演示了如何从安装好的 npm 包拷出这两个文件而不依赖 CDN。

`1.32.0` 是 npm 的 stable 线，`latest` 指向的 `1.33.x-dev` 预发布版没有纳入建模。
