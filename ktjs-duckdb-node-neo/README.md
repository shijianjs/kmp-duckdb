# ktjs-duckdb-node-neo

Kotlin/JS declarations for [`@duckdb/node-api`](https://www.npmjs.com/package/@duckdb/node-api)
2.0.0-r.1, DuckDB's native Node client. Published as
`io.github.shijianjs:ktjs-duckdb-node-neo:2.0.0-r.1-kt.1`.

[English](README.md) | [简体中文](README.zh-CN.md)

- [Documentation](https://shijianjs.github.io/kmp-duckdb/guide/duckdb-node)
- [API reference](https://shijianjs.github.io/kmp-duckdb/api/)

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

`DuckDBNodeApi` is a plain Kotlin object in front of the module: `@duckdb/node-api` is CommonJS, so a
named ESM import fails and the wrapper imports the default export and types it instead. Callers still
write `DuckDBNodeApi.DuckDBInstance.create(…)`.

Results are native JS values (`number`, `bigint`, `string`, …), kept as `JsAny` because a column's
type decides which one you get; `columnTypesJson()` describes it. There is no Arrow dependency here.

Not covered in this pass: `DuckDBAppender`, the full `DuckDBValue` union, the scalar / table function
APIs and the value converters. The list with reasons is in
[`docs/development/node-api-coverage.md`](../docs/development/node-api-coverage.md).
