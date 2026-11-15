@file:JsModule("@duckdb/duckdb-wasm")

package io.github.shijianjs.ktjs.duckdb.wasm

import web.workers.Worker
import kotlin.js.JsAny
import kotlin.js.Promise

/**
 * Upstream `class AsyncDuckDB`: the main-thread handle of a duckdb-wasm instance.
 *
 * The instance talks to a worker; the worker is created by the caller (a `new Worker(workerUrl)` in
 * the browser) and passed to the constructor. After [instantiate] and [open], [connect] returns a
 * connection whose [AsyncDuckDBConnection.query] resolves to an Arrow table.
 */
external class AsyncDuckDB {
    /** The logger this instance was created with. */
    val logger: Logger

    /** The configuration this instance was opened with; the default until [open] runs. */
    val config: DuckDBConfig

    /** @param logger the sink for log entries @param worker the worker the instance talks to; `null` makes it detached */
    constructor(logger: Logger, worker: Worker)

    /** @param logger the sink for log entries; the instance is detached and cannot run queries */
    constructor(logger: Logger)

    /** Drops the worker reference, so the instance can no longer run queries. */
    fun detach(): Unit

    /** Whether the instance is detached from its worker. */
    fun isDetached(): Boolean

    /** Terminates the worker. */
    fun terminate(): Promise<JsAny?>

    /** Resets the database inside the worker. */
    fun reset(): Promise<JsAny?>

    /** A round trip to the worker. */
    fun ping(): Promise<JsAny?>

    /** Drops one registered file. */
    fun dropFile(name: String): Promise<JsAny?>

    /** Drops every registered file when `names` is omitted. */
    fun dropFiles(names: Array<String>?): Promise<JsAny?>

    /** Flushes the registered files to the virtual filesystem. */
    fun flushFiles(): Promise<JsAny?>

    /** Loads the WASM module into the worker; the worker script comes from the constructor call. */
    fun instantiate(mainModuleURL: String): Promise<JsAny?>

    /** @param mainModuleURL the URL of the `.wasm` module @param pthreadWorkerURL the URL of the pthread worker, for the `coi` bundle */
    fun instantiate(mainModuleURL: String, pthreadWorkerURL: String?): Promise<JsAny?>

    /** @param mainModuleURL the URL of the `.wasm` module @param pthreadWorkerURL the URL of the pthread worker, for the `coi` bundle @param progress called as the module downloads */
    fun instantiate(
        mainModuleURL: String,
        pthreadWorkerURL: String?,
        progress: InstantiationProgressHandler,
    ): Promise<JsAny?>

    /** The DuckDB version the worker runs. */
    fun getVersion(): Promise<String>

    /** The DuckDB feature flags of the worker. */
    fun getFeatureFlags(): Promise<Double>

    /** Opens (or creates) the database described by `config`. */
    fun open(config: DuckDBConfig): Promise<JsAny?>

    /** Tokenizes a SQL script. */
    fun tokenize(text: String): Promise<ScriptTokens>

    /** Opens a raw connection and returns its id, for the low-level bindings calls below. */
    fun connectInternal(): Promise<Double>

    /** Opens a connection that answers with a Kotlin-typed [AsyncDuckDBConnection]. */
    fun connect(): Promise<AsyncDuckDBConnection>

    /** Closes a connection opened by [connectInternal]. */
    fun disconnect(conn: Double): Promise<JsAny?>

    /** Runs a query on a connection id and returns the Arrow IPC stream bytes. */
    fun runQuery(conn: Double, text: String): Promise<ArrowBytes>

    /** Starts a query that the caller polls with [pollPendingQuery]. */
    fun startPendingQuery(conn: Double, text: String, allowStreamResult: Boolean?): Promise<ArrowBytes?>

    /** Polls a query started by [startPendingQuery]; `null` means "still running". */
    fun pollPendingQuery(conn: Double): Promise<ArrowBytes?>

    /** Cancels a pending query; `true` when it was still running. */
    fun cancelPendingQuery(conn: Double): Promise<Boolean>

    /** Takes the next result chunk of a query, or `null` when the query is done. */
    fun fetchQueryResults(conn: Double): Promise<ArrowBytes?>

    /** The names of the tables `text` references. */
    fun getTableNames(conn: Double, text: String): Promise<Array<String>>

    /** Prepares a statement on a connection id and returns its statement id. */
    fun createPrepared(conn: Double, text: String): Promise<Double>

    /** Closes a statement created by [createPrepared]. */
    fun closePrepared(conn: Double, statement: Double): Promise<JsAny?>

    /** Runs a prepared statement to completion; the result is Arrow IPC bytes. */
    fun runPrepared(conn: Double, statement: Double, params: Array<JsAny?>): Promise<ArrowBytes>

    /** Runs a prepared statement as a stream; the first chunk is Arrow IPC bytes. */
    fun sendPrepared(conn: Double, statement: Double, params: Array<JsAny?>): Promise<ArrowBytes>

    /** Lists the registered files matching a glob. */
    fun globFiles(path: String): Promise<Array<WebFile>>

    /** Registers a string as a file, visible under `name` in SQL. */
    fun registerFileText(name: String, text: String): Promise<JsAny?>

    /** Registers a URL as a file. */
    fun registerFileURL(
        name: String,
        url: String,
        proto: DuckDBDataProtocol,
        directIO: Boolean,
    ): Promise<JsAny?>

    /** Registers a zero-length buffer, to be filled by [copyFileToBuffer] or `COPY … TO`. */
    fun registerEmptyFileBuffer(name: String): Promise<JsAny?>

    /** Registers an in-memory buffer as a file. */
    fun registerFileBuffer(name: String, buffer: ArrowBytes): Promise<JsAny?>

    /** Registers a file in the origin-private file system. */
    fun registerOPFSFileName(name: String): Promise<JsAny?>

    /** Turns read statistics collection on or off for a registered file. */
    fun collectFileStatistics(name: String, enable: Boolean): Promise<JsAny?>

    /** The read statistics collected for a registered file. */
    fun exportFileStatistics(name: String): Promise<FileStatistics>

    /** Copies a registered file's contents into a buffer. */
    fun copyFileToBuffer(name: String): Promise<ArrowBytes>

    /** Copies a registered file to a path inside the virtual filesystem. */
    fun copyFileToPath(name: String, path: String): Promise<JsAny?>

    /** Inserts a table into `options.name` through an Arrow IPC stream. */
    fun insertArrowFromIPCStream(
        conn: Double,
        buffer: ArrowBytes,
        options: ArrowInsertOptions,
    ): Promise<JsAny?>

    /** Inserts a registered CSV file into `options.name`. */
    fun insertCSVFromPath(conn: Double, path: String, options: CSVInsertOptions): Promise<JsAny?>

    /** Inserts a registered JSON file into `options.name`. */
    fun insertJSONFromPath(conn: Double, path: String, options: JSONInsertOptions): Promise<JsAny?>
}
