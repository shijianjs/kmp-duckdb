# ktjs-duckdb-node-neo

[`@duckdb/node-api`](https://www.npmjs.com/package/@duckdb/node-api) 2.0.0-r.1 的 Kotlin/JS 声明，
DuckDB 的 Node 原生客户端。发布坐标为 `io.github.shijianjs:ktjs-duckdb-node-neo:2.0.0-r.1-kt.1`。

[English](README.md) | [简体中文](README.zh-CN.md)

- [文档](https://shijianjs.github.io/kmp-duckdb/zh-Hans/guide/duckdb-node)
- [API 参考](https://shijianjs.github.io/kmp-duckdb/api/)

```kotlin
DuckDBNodeApi.DuckDBInstance.create(":memory:")
    .then { instance ->
        instance.connect().then { connection ->
            connection.runAndReadAll("SELECT 42 AS answer").then { reader ->
                println(reader.value(0.0, 0.0))
                reader.closeSync(); connection.closeSync(); instance.closeSync()
            }
        }
    }
```

`DuckDBNodeApi` 是模块前面的普通 Kotlin object：`@duckdb/node-api` 是 CommonJS 包，ESM 具名导入会失败，
所以封装导入 default 导出并为其声明类型。调用方仍写 `DuckDBNodeApi.DuckDBInstance.create(…)`。

结果是原生 JS 值（`number`、`bigint`、`string` 等），保留为 `JsAny`，因为具体是哪种由列的類型决定；
`columnTypesJson()` 会说明。本模块不依赖 Arrow。

本轮未覆盖：`DuckDBAppender`、完整的 `DuckDBValue` 联合、标量/表函数 API 与值转换器。清单与理由在
[`docs/development/node-api-coverage.md`](../docs/development/node-api-coverage.md)。
