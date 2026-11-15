# DuckDB WASM coverage

## 来源与范围

- 来源：`@duckdb/duckdb-wasm` 1.32.0（本地克隆 `S:\workspace\github\duckdb\duckdb-wasm`，检出在 tag
  `v1.32.0`；npm 的 `latest` 指向 `1.33.x-dev` 预发布版，未纳入）。
- 核对日期：2026-11-15。
- 范围：`packages/duckdb-wasm/src/index.ts` 导出的每一个子模块，共 7 项。
- 不纳入统计：`packages/duckdb-wasm/src/bindings/` 之下逐文件拆分的内部实现细节（它们的内容计入
  `bindings` 这一条）、`wasm/` 的 C++ 构建、`examples/`。

## 统计

| 状态 | 数量 |
| --- | ---: |
| 总计 | 7 |
| 已完成 | 6 |
| 明确不做 | 1 |
| 待完成 | 0 |

## 明确不做

| 名称 | 排除理由 |
| --- | --- |
| `parallel` | 只导出内部线程池的配置常量，浏览器侧真正的并行开关是 `DuckDBConfig.maximumThreads` 与 `coi` bundle 的 `pthreadWorker`，两者都已声明。单独包装这组常量没有调用点。 |

## 已完成

1. `bindings`：`AsyncDuckDB`（含 3 个 `instantiate` 重载与全部查询/文件/统计方法）、`AsyncDuckDBConnection`、
   `AsyncPreparedStatement`、`AsyncResultStreamIterator`；`DataProtocol`、`FileFlags`、`WebFile`、`S3Config`、
   `FileBlockStatistics`、`FileStatistics`、`InitializationProgress` / `InitializationProgressHandler`、
   `TokenType` / `ScriptTokens`、`JSONTableShape`、`ArrowInsertOptions` / `CSVInsertOptions` /
   `JSONInsertOptions`、`StatusCode` / `IsArrowBuffer` / `IsDuckDBWasmRetry`。
2. `log`：`LogLevel`、`LogTopic`、`LogEvent`、`LogOrigin`、`LogEntry`、`Logger`、`VoidLogger`、`ConsoleLogger`
   与 `voidLogger()` / `consoleLogger()` 工厂。
3. `status`：`StatusCode`（成功、错误上限、重试）与两个类型守卫。
4. `platform`：`DuckDBBundles` / `DuckDBBundleFiles` / `DuckDBCOIBundleFiles` / `DuckDBBundle`、
   `PlatformFeatures`、`getJsDelivrBundles`、`getPlatformFeatures`、`selectBundle`、`isNode` / `isFirefox` /
   `isSafari`、`createWorker`。
5. `version`：`PACKAGE_NAME` / `PACKAGE_VERSION` / `MAJOR` / `MINOR` / `PATCH`。
6. `worker`：worker 的创建与销毁入口，由 `AsyncDuckDB` 与 `createWorker` 组合使用。

## 验证

- `:ktjs-duckdb-wasm:compileKotlinJs` 通过。
- 运行时行为由 demo 的 e2e 套件覆盖：页面真的从本地文件启动 `duckdb-browser-mvp.worker.js`、实例化
  `duckdb-mvp.wasm`、`registerFileText` 一个 CSV、执行 join + `range()` 的查询，并用 Arrow 模块把结果渲染成
  表格（断言 5 列 12 行）。
- 没有为这个模块单独建 `jsTest`：绝大多数成员是浏览器侧 Promise API，独立测试需要再准备一份 wasm 与
  worker，而 e2e 已经端到端跑过同一条路径。如果以后要覆盖 `selectBundle` 的分支选择、`OPFS` 与
  `copyFileTo*`，再补一个浏览器测试任务。

## 建模备注

- 构造函数只接收 logger 与 worker：上游 `AsyncDuckDB` 不自己创建 worker，所以 Kotlin 侧也没有
  `mainWorker` 参数，调用方必须传入 `Worker`（或在 Node 下传 `null`）。
- `selectBundle` 与 `getPlatformFeatures` 都是异步的，Kotlin 侧签名就是 `Promise`，不做 `suspend` 包装：
  这个模块的定位是与上游一比一。
- `wc` 类接口（`Logger`）不用 `external class : Logger` 实现——Kotlin 会要求实现抽象成员；改为普通函数返回
  `unsafeCast` 出来的对象（`voidLogger()` / `consoleLogger(level)`）。
