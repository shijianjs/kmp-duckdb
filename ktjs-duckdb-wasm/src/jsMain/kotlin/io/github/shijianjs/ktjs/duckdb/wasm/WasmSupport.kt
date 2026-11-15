package io.github.shijianjs.ktjs.duckdb.wasm

import js.typedarrays.Uint8Array
import kotlin.js.unsafeCast

/*
 * Kotlin-side helpers of the duckdb-wasm wrapper.
 *
 * They live outside the `@JsModule("@duckdb/duckdb-wasm")` files because Kotlin allows nothing but
 * external declarations in a file that carries that annotation, while inline factories, constant
 * objects and type aliases are ordinary Kotlin declarations.
 */

/** Upstream's `Uint8Array`: the buffer type of the raw Arrow IPC payloads. */
typealias ArrowBytes = Uint8Array<*>

/** Upstream `new VoidLogger()`, as a [Logger]. */
fun voidLogger(): Logger = VoidLogger().unsafeCast<Logger>()

/** Upstream `new ConsoleLogger(level)`, as a [Logger]; upstream defaults to [LogLevels.INFO]. */
fun consoleLogger(level: LogLevel = LogLevels.INFO): Logger =
    ConsoleLogger(level).unsafeCast<Logger>()

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [LogLevels]. */
inline fun LogLevel(value: Double): LogLevel = value.unsafeCast<LogLevel>()

/** Every literal of [LogLevel]. */
object LogLevels {
    inline val NONE: LogLevel get() = LogLevel(0.0)
    inline val DEBUG: LogLevel get() = LogLevel(1.0)
    inline val INFO: LogLevel get() = LogLevel(2.0)
    inline val WARNING: LogLevel get() = LogLevel(3.0)
    inline val ERROR: LogLevel get() = LogLevel(4.0)
}

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [LogTopics]. */
inline fun LogTopic(value: Double): LogTopic = value.unsafeCast<LogTopic>()

/** Every literal of [LogTopic]. */
object LogTopics {
    inline val NONE: LogTopic get() = LogTopic(0.0)
    inline val CONNECT: LogTopic get() = LogTopic(1.0)
    inline val DISCONNECT: LogTopic get() = LogTopic(2.0)
    inline val OPEN: LogTopic get() = LogTopic(3.0)
    inline val QUERY: LogTopic get() = LogTopic(4.0)
    inline val INSTANTIATE: LogTopic get() = LogTopic(5.0)
}

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [LogEvents]. */
inline fun LogEvent(value: Double): LogEvent = value.unsafeCast<LogEvent>()

/** Every literal of [LogEvent]. */
object LogEvents {
    inline val NONE: LogEvent get() = LogEvent(0.0)
    inline val OK: LogEvent get() = LogEvent(1.0)
    inline val ERROR: LogEvent get() = LogEvent(2.0)
    inline val START: LogEvent get() = LogEvent(3.0)
    inline val RUN: LogEvent get() = LogEvent(4.0)
    inline val CAPTURE: LogEvent get() = LogEvent(5.0)
}

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [LogOrigins]. */
inline fun LogOrigin(value: Double): LogOrigin = value.unsafeCast<LogOrigin>()

/** Every literal of [LogOrigin]. */
object LogOrigins {
    inline val NONE: LogOrigin get() = LogOrigin(0.0)
    inline val WEB_WORKER: LogOrigin get() = LogOrigin(1.0)
    inline val NODE_WORKER: LogOrigin get() = LogOrigin(2.0)
    inline val BINDINGS: LogOrigin get() = LogOrigin(3.0)
    inline val ASYNC_DUCKDB: LogOrigin get() = LogOrigin(4.0)
}

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [DuckDBAccessModes]. */
inline fun DuckDBAccessMode(value: Double): DuckDBAccessMode = value.unsafeCast<DuckDBAccessMode>()

/** Every literal of [DuckDBAccessMode]. */
object DuckDBAccessModes {
    inline val UNDEFINED: DuckDBAccessMode get() = DuckDBAccessMode(0.0)
    inline val AUTOMATIC: DuckDBAccessMode get() = DuckDBAccessMode(1.0)
    inline val READ_ONLY: DuckDBAccessMode get() = DuckDBAccessMode(2.0)
    inline val READ_WRITE: DuckDBAccessMode get() = DuckDBAccessMode(3.0)
}

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [DuckDBDataProtocols]. */
inline fun DuckDBDataProtocol(value: Double): DuckDBDataProtocol =
    value.unsafeCast<DuckDBDataProtocol>()

/** Every literal of [DuckDBDataProtocol]. */
object DuckDBDataProtocols {
    inline val BUFFER: DuckDBDataProtocol get() = DuckDBDataProtocol(0.0)
    inline val NODE_FS: DuckDBDataProtocol get() = DuckDBDataProtocol(1.0)
    inline val BROWSER_FILEREADER: DuckDBDataProtocol get() = DuckDBDataProtocol(2.0)
    inline val BROWSER_FSACCESS: DuckDBDataProtocol get() = DuckDBDataProtocol(3.0)
    inline val HTTP: DuckDBDataProtocol get() = DuckDBDataProtocol(4.0)
    inline val S3: DuckDBDataProtocol get() = DuckDBDataProtocol(5.0)
}

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [FileFlagValues]. */
inline fun FileFlags(value: Double): FileFlags = value.unsafeCast<FileFlags>()

/** Every literal of [FileFlags]; the values are bit flags. */
object FileFlagValues {
    inline val READ: FileFlags get() = FileFlags(1.0)
    inline val WRITE: FileFlags get() = FileFlags(2.0)
    inline val DIRECT_IO: FileFlags get() = FileFlags(4.0)
    inline val FILE_CREATE: FileFlags get() = FileFlags(8.0)
    inline val FILE_CREATE_NEW: FileFlags get() = FileFlags(16.0)
    inline val APPEND: FileFlags get() = FileFlags(32.0)
    inline val PRIVATE: FileFlags get() = FileFlags(64.0)
    inline val NULL_IF_NOT_EXISTS: FileFlags get() = FileFlags(128.0)
    inline val PARALLEL_ACCESS: FileFlags get() = FileFlags(256.0)
    inline val EXCLUSIVE_CREATE: FileFlags get() = FileFlags(512.0)
    inline val NULL_IF_EXISTS: FileFlags get() = FileFlags(1024.0)
}

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [TokenTypes]. */
inline fun TokenType(value: Double): TokenType = value.unsafeCast<TokenType>()

/** Every literal of [TokenType]. */
object TokenTypes {
    inline val IDENTIFIER: TokenType get() = TokenType(0.0)
    inline val NUMERIC_CONSTANT: TokenType get() = TokenType(1.0)
    inline val STRING_CONSTANT: TokenType get() = TokenType(2.0)
    inline val OPERATOR: TokenType get() = TokenType(3.0)
    inline val KEYWORD: TokenType get() = TokenType(4.0)
    inline val COMMENT: TokenType get() = TokenType(5.0)
}

/** Wraps a string coming from JavaScript. Prefer the constants in [JSONTableShapes]. */
inline fun JSONTableShape(value: String): JSONTableShape = value.unsafeCast<JSONTableShape>()

/** The literals of [JSONTableShape]. */
object JSONTableShapes {
    /** Each array element is one row object. */
    inline val ROW_ARRAY: JSONTableShape get() = JSONTableShape("row-array")

    /** The object maps column names to arrays of values. */
    inline val COLUMN_OBJECT: JSONTableShape get() = JSONTableShape("column-object")
}

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [StatusCodes]. */
inline fun StatusCode(value: Double): StatusCode = value.unsafeCast<StatusCode>()

/** Every literal of [StatusCode]. */
object StatusCodes {
    inline val SUCCESS: StatusCode get() = StatusCode(0.0)
    inline val MAX_ARROW_ERROR: StatusCode get() = StatusCode(255.0)
    inline val DUCKDB_WASM_RETRY: StatusCode get() = StatusCode(256.0)
}
