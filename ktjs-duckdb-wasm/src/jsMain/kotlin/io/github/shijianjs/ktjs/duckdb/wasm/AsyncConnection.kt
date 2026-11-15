@file:JsModule("@duckdb/duckdb-wasm")

package io.github.shijianjs.ktjs.duckdb.wasm

import io.github.shijianjs.ktjs.duckdb.arrow.Table
import kotlin.js.JsAny
import kotlin.js.Promise

/**
 * Upstream `class AsyncDuckDBConnection`: one SQL connection of an [AsyncDuckDB].
 *
 * [query] is the whole reason this wrapper exists: it runs SQL and resolves to an Arrow table, so
 * results are read with the Arrow wrapper rather than through rows of untyped values.
 */
external class AsyncDuckDBConnection {
    /** The instance this connection belongs to. */
    val bindings: AsyncDuckDB

    /** Closes the connection. */
    fun close(): Promise<JsAny?>

    /** Runs SQL and resolves to the result as an Arrow table. */
    fun query(text: String): Promise<Table>

    /** Cancels the query this connection is currently streaming. */
    fun cancelSent(): Promise<Boolean>

    /** The names of the tables `query` references. */
    fun getTableNames(query: String): Promise<Array<String>>

    /** Prepares a statement on this connection. */
    fun prepare(text: String): Promise<AsyncPreparedStatement>

    /** Inserts an Arrow table into `options.name`. */
    fun insertArrowTable(table: Table, options: ArrowInsertOptions): Promise<JsAny?>

    /** Inserts an Arrow IPC stream into `options.name`. */
    fun insertArrowFromIPCStream(buffer: ArrowBytes, options: ArrowInsertOptions): Promise<JsAny?>

    /** Inserts a registered CSV file into `options.name`. */
    fun insertCSVFromPath(text: String, options: CSVInsertOptions): Promise<JsAny?>

    /** Inserts a registered JSON file into `options.name`. */
    fun insertJSONFromPath(text: String, options: JSONInsertOptions): Promise<JsAny?>
}

/**
 * Upstream `class AsyncResultStreamIterator`: the chunks of a streaming query result.
 *
 * Returned by `AsyncPreparedStatement.send`; iterate it through [next] until `done` is set.
 */
external class AsyncResultStreamIterator {
    /** The next result chunk; `done` is set when the stream is exhausted. */
    fun next(): Promise<JsAny>
}

/**
 * Upstream `class AsyncPreparedStatement`: a statement prepared on an [AsyncDuckDBConnection].
 *
 * Upstream types the parameters as `any[]`; the wrapper keeps that as `JsAny?` because the accepted
 * bind types depend on the statement's own parameter types.
 */
external class AsyncPreparedStatement {
    /** Frees the statement inside the worker. */
    fun close(): Promise<JsAny?>

    /** Binds `params` and runs the statement to completion, resolving to an Arrow table. */
    fun query(vararg params: JsAny?): Promise<Table>
}
