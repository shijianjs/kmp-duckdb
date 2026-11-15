@file:JsModule("@duckdb/node-api")

package io.github.shijianjs.ktjs.duckdb.node

import kotlin.js.JsName
import kotlin.js.Promise

/*
 * `@duckdb/node-api` is a CommonJS package whose exports are assembled at runtime, so Node cannot
 * discover its named exports and `import { DuckDBInstance }` fails with "Named export not found".
 * Kotlin therefore imports the module's *default* export, which is the namespace object itself, and
 * reaches the classes through it: [duckdbModule] is typed as [DuckDBModule], so the member access is
 * still checked. `DuckDBNodeApi` in `NodeApiAccess.kt` is the Kotlin-facing entry point.
 *
 * Classes with static members cannot be modelled as a Kotlin `external class` (an external class
 * cannot carry a companion), so the statics live on the interfaces below.
 */

/** The module's default export: the namespace object holding the classes. */
@JsName("default")
external val duckdbModule: DuckDBModule

/** The classes `@duckdb/node-api` exports. */
external interface DuckDBModule {
    /** The `DuckDBInstance` class value, whose statics open and cache database instances. */
    val DuckDBInstance: DuckDBInstanceStatics

    /** The `DuckDBConnection` class value, whose statics open a connection on the shared cache. */
    val DuckDBConnection: DuckDBConnectionStatics
}

/** The static side of upstream `class DuckDBInstance`. */
external interface DuckDBInstanceStatics {
    /** Opens (or creates) a database at `path`; it is a file path, or omit it for `:memory:`. */
    fun create(path: String?): Promise<DuckDBInstance>

    /** Opens a database through the process-wide instance cache; [create] bypasses it. */
    fun fromCache(path: String?): Promise<DuckDBInstance>
}

/** The static side of upstream `class DuckDBConnection`. */
external interface DuckDBConnectionStatics {
    /** Opens a connection on `instance`, or on the shared cache instance when omitted. */
    fun create(instance: DuckDBInstance?): Promise<DuckDBConnection>
}

/** Upstream `class DuckDBInstance`: one open database file or in-memory database. */
external interface DuckDBInstance {
    /** Opens a new connection to this database. */
    fun connect(): Promise<DuckDBConnection>

    /** Closes the database synchronously; open connections become unusable. */
    fun closeSync(): Unit
}
