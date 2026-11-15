package io.github.shijianjs.ktjs.duckdb.node

/**
 * The Kotlin-facing entry point of `@duckdb/node-api`.
 *
 * A plain Kotlin object rather than an `external` one: the classes come from the module's default
 * export (see `NodeApi.kt` for why), and this object only forwards them so callers write
 * `DuckDBNodeApi.DuckDBInstance.create(…)` the same way they would in TypeScript.
 */
object DuckDBNodeApi {
    /** The `DuckDBInstance` class value: [DuckDBInstanceStatics.create] and [DuckDBInstanceStatics.fromCache]. */
    val DuckDBInstance: DuckDBInstanceStatics get() = duckdbModule.DuckDBInstance

    /** The `DuckDBConnection` class value: [DuckDBConnectionStatics.create]. */
    val DuckDBConnection: DuckDBConnectionStatics get() = duckdbModule.DuckDBConnection
}
