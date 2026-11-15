@file:JsModule("@duckdb/duckdb-wasm")

package io.github.shijianjs.ktjs.duckdb.wasm

/*
 * The configuration surface of `AsyncDuckDB.open`.
 *
 * The enums' value types are here; their numeric literals and wrapping factories are in
 * `WasmSupport.kt`, because a `@JsModule` file may hold external declarations only.
 */

/** Upstream `enum DuckDBAccessMode`: how [AsyncDuckDB.open] opens the database file. */
sealed external interface DuckDBAccessMode

/** Upstream `interface DuckDBQueryConfig`: how query results are converted to Arrow. */
external interface DuckDBQueryConfig {
    /** The polling interval of a pending query, in milliseconds. */
    var queryPollingInterval: Double?

    /** Cast `BIGINT` to a double instead of keeping 64-bit integers. */
    var castBigIntToDouble: Boolean?

    /** Cast timestamps to `Date64`. */
    var castTimestampToDate: Boolean?

    /** Cast durations to `Time64`. */
    var castDurationToTime64: Boolean?

    /** Cast `DECIMAL` to a double. */
    var castDecimalToDouble: Boolean?
}

/** Upstream `interface DuckDBFilesystemConfig`: how `http(s)://` files are read. */
external interface DuckDBFilesystemConfig {
    /** Trust `HEAD` requests to report the file size. */
    var reliableHeadRequests: Boolean?

    /** Fall back to full downloads when the server does not support range requests. */
    var allowFullHTTPReads: Boolean?

    /** Always download the whole file instead of issuing range requests. */
    var forceFullHTTPReads: Boolean?
}

/** Upstream `interface DuckDBOPFSConfig`: how `opfs://` files are handled during execution. */
external interface DuckDBOPFSConfig {
    /** `auto` registers and drops `opfs://` files around each query; `manual` leaves that to the caller. */
    var fileHandling: String?
}

/** Upstream `interface DuckDBConfig`: what [AsyncDuckDB.open] accepts. */
external interface DuckDBConfig {
    /** The database path; `:memory:` when omitted. */
    var path: String?

    /** How the file is opened; see [DuckDBAccessModes]. */
    var accessMode: DuckDBAccessMode?

    /** The thread count; needs cross-origin isolation (SharedArrayBuffer) to exceed 1. */
    var maximumThreads: Double?

    /** Use direct I/O for the database file. */
    var useDirectIO: Boolean?

    /** Result conversion options; see [DuckDBQueryConfig]. */
    var query: DuckDBQueryConfig?

    /** Filesystem options; see [DuckDBFilesystemConfig]. */
    var filesystem: DuckDBFilesystemConfig?

    /** Allow loading extensions that are not signed. */
    var allowUnsignedExtensions: Boolean?

    /** Use the alternate Arrow conversion that preserves full range and precision. */
    var arrowLosslessConversion: Boolean?

    /** The `User-Agent` sent with HTTP requests. */
    var customUserAgent: String?

    /** OPFS options; see [DuckDBOPFSConfig]. */
    var opfs: DuckDBOPFSConfig?
}
