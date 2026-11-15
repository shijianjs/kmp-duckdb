# ktjs-duckdb-wasm

Kotlin/JS declarations for [`@duckdb/duckdb-wasm`](https://www.npmjs.com/package/@duckdb/duckdb-wasm)
1.32.0: the async database handle, connections, prepared statements, the browser file registry and the
bundle selection helpers. Published as
`io.github.shijianjs:ktjs-duckdb-wasm:1.32.0-kt.1`.

[English](README.md) | [简体中文](README.zh-CN.md)

- [Documentation](https://shijianjs.github.io/kmp-duckdb/guide/duckdb-wasm)
- [Live demo](https://shijianjs.github.io/kmp-duckdb/demo/) — the page runs this wrapper in a browser
- [API reference](https://shijianjs.github.io/kmp-duckdb/api/)

```kotlin
val worker = Worker(bundle.mainWorker!!)
val db = AsyncDuckDB(consoleLogger(LogLevels.WARNING), worker)

db.instantiate(bundle.mainModule, bundle.pthreadWorker)
    .then { db.open(unsafeJso<DuckDBConfig> { path = ":memory:" }) }
    .then { db.connect() }
```

`query()` resolves to an Arrow `Table` from [`ktjs-apache-arrow`](../ktjs-apache-arrow/README.md), so the
wrapper depends on that module and you read results through it.

The page has to serve the `.wasm` module and the worker script; `getJsDelivrBundles()` or your own
`DuckDBBundles` object both work, and the demo module's `duckdbAssets` task shows how to copy them out
of the npm package instead of relying on a CDN.

`1.32.0` is the npm stable line, not the `1.33.x-dev` pre-release that `latest` points at.
