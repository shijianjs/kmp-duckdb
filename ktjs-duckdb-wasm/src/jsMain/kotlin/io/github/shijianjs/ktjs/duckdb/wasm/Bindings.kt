@file:JsModule("@duckdb/duckdb-wasm")

package io.github.shijianjs.ktjs.duckdb.wasm

import io.github.shijianjs.ktjs.duckdb.arrow.DataType
import js.objects.Record
import js.typedarrays.Uint8Array
import kotlin.js.JsAny

/*
 * The bindings, file and insert types of duckdb-wasm.
 *
 * Enum value types and the option interfaces are here; every numeric literal, wrapping factory and
 * constant object is in `WasmSupport.kt`, because a `@JsModule` file may hold external declarations
 * only.
 */

/** Upstream `enum DuckDBDataProtocol`: how a registered file is reached. */
sealed external interface DuckDBDataProtocol

/** Upstream `enum FileFlags`: the `open` flags the WASM filesystem passes to a runtime. */
sealed external interface FileFlags

/** Upstream `interface S3Config`: credentials and endpoint of an S3 filesystem. */
external interface S3Config {
    /** The AWS region. */
    var region: String?

    /** A custom endpoint, for S3-compatible storage. */
    var endpoint: String?

    /** The access key id. */
    var accessKeyId: String?

    /** The secret access key. */
    var secretAccessKey: String?

    /** The session token of temporary credentials. */
    var sessionToken: String?
}

/** Upstream `interface FileBlockStatistics`: read statistics of one block of a registered file. */
external interface FileBlockStatistics {
    /** Reads that missed the cache. */
    var fileReadsCold: Double

    /** Reads issued ahead of time. */
    var fileReadsAhead: Double

    /** Reads served from the cache. */
    var fileReadsCached: Double

    /** Writes to the file. */
    var fileWrites: Double

    /** Page accesses. */
    var pageAccesses: Double

    /** Pages loaded. */
    var pageLoads: Double
}

/** Upstream `class FileStatistics`: read statistics of a registered file, as the WASM filesystem reports them. */
external class FileStatistics {
    /** The number of reads that missed the cache. */
    val totalFileReadsCold: Double

    /** The number of reads issued ahead of time. */
    val totalFileReadsAhead: Double

    /** The number of reads served from the cache. */
    val totalFileReadsCached: Double

    /** The number of writes. */
    val totalFileWrites: Double

    /** The number of page accesses. */
    val totalPageAccesses: Double

    /** The number of pages loaded. */
    val totalPageLoads: Double

    /** The block size the statistics are grouped by. */
    val blockSize: Double

    /** The per-block statistics, packed as a byte array. */
    val blockStats: Uint8Array<*>

    /** @param u8array the packed statistics the WASM filesystem produced */
    constructor(u8array: Uint8Array<*>)

    /** The statistics of one block, written into `out` when it is given. */
    fun getBlockStats(index: Double, out: FileBlockStatistics?): FileBlockStatistics
}

/** Upstream `interface WebFile`: a file registered with the virtual filesystem. */
external interface WebFile {
    /** The name the file is visible under in SQL. */
    var fileName: String

    /** How the file is reached; see [DuckDBDataProtocols]. */
    var dataProtocol: DuckDBDataProtocol

    /** The id the WASM filesystem assigned. */
    var fileId: Double?

    /** The size in bytes, when known. */
    var fileSize: Double?

    /** The URL the file was registered from. */
    var dataUrl: String?

    /** The native file descriptor, for `NODE_FS`. */
    var dataNativeFd: Double?

    /** Whether read statistics are collected. */
    var collectStatistics: Boolean?

    /** Whether `HEAD` requests are trusted. */
    var reliableHeadRequests: Boolean?

    /** Whether a full download is allowed when range requests are unsupported. */
    var allowFullHttpReads: Boolean?
}

/** Upstream `interface InstantiationProgress`: how far the WASM module download has got. */
external interface InstantiationProgress {
    /** When instantiation started. */
    var startedAt: kotlin.js.Date

    /** When [bytesLoaded] was last updated. */
    var updatedAt: kotlin.js.Date

    /** The size of the module being downloaded. */
    var bytesTotal: Double

    /** How many bytes have been downloaded. */
    var bytesLoaded: Double
}

/** Upstream `type InstantiationProgressHandler`. */
typealias InstantiationProgressHandler = (InstantiationProgress) -> Unit

/** Upstream `enum TokenType`: the kind of one token of a SQL script. */
sealed external interface TokenType

/** Upstream `interface ScriptTokens`: a tokenized SQL script. */
external interface ScriptTokens {
    /** The byte offset of each token. */
    var offsets: Array<Double>

    /** The [TokenType] of each token, in the same order as [offsets]. */
    var types: Array<TokenType>
}

/** Upstream `enum JSONTableShape`: the JSON layout [JSONInsertOptions.shape] selects. */
sealed external interface JSONTableShape

/** Upstream `interface ArrowInsertOptions`: how an Arrow table is inserted. */
external interface ArrowInsertOptions {
    /** The table name to insert into. */
    var name: String

    /** The schema of the table. */
    var schema: String?

    /** Create the table before inserting. */
    var create: Boolean?
}

/** Upstream `interface CSVInsertOptions`: how a registered CSV file is inserted. */
external interface CSVInsertOptions {
    /** The table name to insert into. */
    var name: String

    /** The schema of the table. */
    var schema: String?

    /** Create the table before inserting. */
    var create: Boolean?

    /** Whether the file has a header row. */
    var header: Boolean?

    /** The field delimiter. */
    var delimiter: String?

    /** The quote character. */
    var quote: String?

    /** The escape character. */
    var escape: String?

    /** How many leading lines to skip. */
    var skip: Double?

    /** Detect the column types instead of reading them as strings. */
    var detect: Boolean?

    /** The `strptime` format of date columns. */
    var dateFormat: String?

    /** The `strptime` format of timestamp columns. */
    var timestampFormat: String?

    /** The column types, keyed by column name. */
    var columns: Record<JsAny, DataType>?

    /** The column definitions in the upstream SQL field shape. */
    var columnsFlat: Array<JsAny>?
}

/** Upstream `interface JSONInsertOptions`: how a registered JSON file is inserted. */
external interface JSONInsertOptions {
    /** The table name to insert into. */
    var name: String

    /** The schema of the table. */
    var schema: String?

    /** Create the table before inserting. */
    var create: Boolean?

    /** The JSON layout; see [JSONTableShapes]. */
    var shape: JSONTableShape?

    /** The column types, keyed by column name. */
    var columns: Record<JsAny, DataType>?

    /** The column definitions in the upstream SQL field shape. */
    var columnsFlat: Array<JsAny>?
}

/** Upstream `enum StatusCode`: the status word the WASM module returns with a response. */
sealed external interface StatusCode

/** Upstream `IsArrowBuffer`: whether the status encodes an Arrow error. */
external fun IsArrowBuffer(status: StatusCode): Boolean

/** Upstream `IsDuckDBWasmRetry`: whether the status asks the caller to retry. */
external fun IsDuckDBWasmRetry(status: StatusCode): Boolean
