@file:JsModule("@duckdb/node-api")

package io.github.shijianjs.ktjs.duckdb.node

import kotlin.js.JsAny
import kotlin.js.Promise

/**
 * Upstream `class DuckDBConnection`: one connection of a [DuckDBInstance].
 *
 * The four families of run methods differ in how the result is consumed:
 *
 * - `run*` materializes the whole result in memory, then returns it;
 * - `stream*` produces the result chunks on demand;
 * - `start*` runs the query as background tasks the caller drives;
 * - `runAndReadAll` and friends wrap those in a [DuckDBResultReader] and read everything.
 */
external interface DuckDBConnection {
    /** Shares the instance's client context. */
    val clientContext: JsAny

    /** Closes the connection synchronously; equivalent to [disconnectSync]. */
    fun closeSync(): Unit

    /** Closes the connection synchronously. */
    fun disconnectSync(): Unit

    /** Interrupts the query currently running on this connection. */
    fun interrupt(): Unit

    /** Runs SQL and materializes the whole result. */
    fun run(sql: String): Promise<DuckDBMaterializedResult>

    /** Runs SQL with bound values; `values` binds positionally or by `$name`. */
    fun run(sql: String, values: Array<JsAny?>): Promise<DuckDBMaterializedResult>

    /** Runs SQL and returns a reader that has not read any chunk yet. */
    fun runAndRead(sql: String): Promise<DuckDBResultReader>

    /** Runs SQL with bound values and returns a reader that has not read any chunk yet. */
    fun runAndRead(sql: String, values: Array<JsAny?>): Promise<DuckDBResultReader>

    /** Runs SQL and reads every chunk into the reader. */
    fun runAndReadAll(sql: String): Promise<DuckDBResultReader>

    /** Runs SQL with bound values and reads every chunk into the reader. */
    fun runAndReadAll(sql: String, values: Array<JsAny?>): Promise<DuckDBResultReader>

    /** Runs SQL and reads at least `targetRowCount` rows into the reader. */
    fun runAndReadUntil(sql: String, targetRowCount: Double): Promise<DuckDBResultReader>

    /** Runs SQL with bound values and reads at least `targetRowCount` rows into the reader. */
    fun runAndReadUntil(
        sql: String,
        targetRowCount: Double,
        values: Array<JsAny?>,
    ): Promise<DuckDBResultReader>

    /** Prepares a statement; the statement stays bound to this connection until it is destroyed. */
    fun prepare(sql: String): Promise<DuckDBPreparedStatement>

    /** The names of the tables `query` references; `qualified` prefixes them with schema and catalog. */
    fun getTableNames(query: String, qualified: Boolean): Array<String>
}

/**
 * Upstream `class DuckDBPreparedStatement`: a prepared statement.
 *
 * Upstream exposes one `bind*` method per DuckDB type plus a generic `bind(values, types)`; only the
 * generic form is declared here, taking JS values directly.
 */
external interface DuckDBPreparedStatement {
    /** The number of parameters the statement expects. */
    val parameterCount: Double

    /** The number of result columns. */
    val columnCount: Double

    /** The SQL statement kind, as the bindings report it. */
    val statementType: JsAny

    /** The column name at `columnIndex`. */
    fun columnName(columnIndex: Double): String

    /** The column type at `columnIndex`. */
    fun columnType(columnIndex: Double): DuckDBType

    /** Binds positional `values` (or a `$name` object), optionally with explicit `types`. */
    fun bind(values: Array<JsAny?>, types: Array<DuckDBType>?): Unit

    /** Clears the values bound by a previous [bind]. */
    fun clearBindings(): Unit

    /** Runs the statement and returns a result that has not been read. */
    fun run(): Promise<DuckDBResult>

    /** Runs the statement and returns a reader over the result. */
    fun runAndRead(): Promise<DuckDBResultReader>

    /** Runs the statement and reads the whole result into the reader. */
    fun runAndReadAll(): Promise<DuckDBResultReader>

    /** Frees the statement synchronously. */
    fun destroySync(): Unit
}
