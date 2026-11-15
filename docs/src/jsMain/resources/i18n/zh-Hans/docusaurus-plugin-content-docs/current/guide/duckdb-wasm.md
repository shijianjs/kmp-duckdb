# DuckDB WASM

`ktjs-duckdb-wasm` 声明浏览器侧使用的 `@duckdb/duckdb-wasm@1.32.0`：异步数据库句柄、连接、预处理语句、
文件注册表与 bundle 选择辅助。

## Bundle

`1.32.0` 是本模块建模所依据的 npm stable 线。该包提供三种构建，封装保留了用于挑选的平台辅助函数：

```kotlin
import io.github.shijianjs.ktjs.duckdb.wasm.getPlatformFeatures
import io.github.shijianjs.ktjs.duckdb.wasm.selectBundle

val bundles = getJsDelivrBundles()      // 或自备路径
val bundle = selectBundle(bundles, getPlatformFeatures())
```

一个 bundle 暴露 `mainModule`（`.wasm` 的 URL）、`mainWorker`（worker 脚本 URL），跨源隔离的 `coi`
构建还有 `pthreadWorker`。`selectBundle` 是异步的，因为它读取的平台能力是异步的。

## 实例化与查询

```kotlin
val worker = Worker(bundle.mainWorker!!)
val db = AsyncDuckDB(consoleLogger(LogLevels.WARNING), worker)

db.instantiate(bundle.mainModule, bundle.pthreadWorker)
    .then { db.open(unsafeJso<DuckDBConfig> { path = ":memory:" }) }
    .then { db.connect() }
    .then { connection -> connection.query("SELECT 1 AS n") }
```

构造函数只接收 logger 与 worker——上游自己不会创建 worker，所以生命周期由调用方负责（`isNode`
决定了根本有没有 worker）。

## 文件与表

浏览器构建打不开路径，数据通过文件注册表进入：

```kotlin
db.registerFileText("people.csv", csvText)
db.insertCSVFromPath("people.csv", unsafeJso<CSVInsertOptions> {
    schema = "main"
    name = "people"
    create = true
})
```

其他来源用 `registerFileURL`、`registerFileBuffer`、`registerEmptyFileBuffer`、
`registerOPFSFileName`；注册表本身用 `dropFile`、`dropFiles`、`flushFiles` 管理，导出文件用
`copyFileToBuffer` / `copyFileToPath`。

DuckDB 自己的 Arrow 输出就是 Arrow 表，因此依赖 arrow 模块：`connection.query()` 解析为 `Table`，
`insertArrowTable` / `insertArrowFromIPCStream` 又把表送回去。

## 预处理语句与流式结果

```kotlin
val prepared = connection.prepare("SELECT ?::INTEGER + 1 AS answer")
prepared.query(1.0)
```

大结果集走增量路径 `startPendingQuery` / `pollPendingQuery` / `fetchQueryResults` /
`cancelPendingQuery`，`AsyncResultStreamIterator` 把它们包成 `next()`。

## 配置

`DuckDBConfig` 包含 `path`、`accessMode`、`maximumThreads`、`useDirectIO`、`query`、`filesystem`、
`allowUnsignedExtensions`、`arrowLosslessConversion`、`customUserAgent` 与 `opfs`。所有成员都可选，
所以 `unsafeJso<DuckDBConfig> { path = ":memory:" }` 就是合法配置；`AccessMode` 的值类型是
`AccessModes.AUTOMATIC` / `READ_ONLY` / `READ_WRITE`。

## Node 呢？

同一个包上游也有 Node 入口，但本模块只声明浏览器侧接口。服务端使用改为
`ktjs-duckdb-node-neo`——那是原生客户端，完全不需要 wasm。
