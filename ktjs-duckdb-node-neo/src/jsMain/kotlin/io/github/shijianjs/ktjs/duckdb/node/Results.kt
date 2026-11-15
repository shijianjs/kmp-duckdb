package io.github.shijianjs.ktjs.duckdb.node

import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.unsafeCast

/*
 * Types of the Node client's results.
 *
 * This file is not a `@JsModule` file: none of these classes is constructed or imported by this
 * wrapper — they arrive as return values of the module's own functions — and Kotlin allows nothing
 * but external declarations in a file that carries the annotation, while the value aliases below are
 * ordinary declarations.
 *
 * Upstream `DuckDBValue` is a large union — bigint, boolean, number, string, plus the wrappers for
 * dates, times, decimals, intervals, lists, structs and maps. Every one of those is a JS primitive or
 * a JS object with a documented shape, so Kotlin sees them as [JsAny]; the concrete helper types
 * (`DuckDBDateValue`, …) are tracked as pending items in `docs/development/node-api-coverage.md`.
 */

/** Upstream `DuckDBValue`: one cell of a result. */
typealias DuckDBValue = JsAny

/** Upstream `JS`: a value converted to its plain JavaScript form. */
typealias JS = JsAny

/** Upstream `Json`: a value converted to JSON. */
typealias Json = JsAny

/** Upstream `DuckDBType`: the resolved type of a column, parameter or appender slot. */
external interface DuckDBType {
    /** The numeric type id, as the bindings report it. */
    val typeId: DuckDBTypeId

    /** Upstream's `toString`: the SQL type name, e.g. `INTEGER` or `DECIMAL(18,3)`. */
    override fun toString(): String
}

/** Upstream `DuckDBTypeId`: the numeric identifier of a DuckDB type. */
sealed external interface DuckDBTypeId

/** Wraps a numeric value coming from JavaScript. */
inline fun DuckDBTypeId(value: Double): DuckDBTypeId = value.unsafeCast<DuckDBTypeId>()

/** Upstream `DuckDBLogicalType`: the type without its physical width. */
external interface DuckDBLogicalType {
    /** Upstream's `toString`. */
    override fun toString(): String
}

/**
 * Upstream `class DuckDBResult`: a result handle before it is read.
 *
 * Most callers go through [DuckDBResultReader] (i.e. `runAndReadAll`) instead; a result left open
 * holds its table lock until [closeSync].
 */
external open class DuckDBResult {
    /** The number of columns of the result. */
    val columnCount: Double

    /** The name of the column at `columnIndex`. */
    fun columnName(columnIndex: Double): String

    /** The column names, in order. */
    fun columnNames(): Array<String>

    /** The column names with duplicates disambiguated, as the object-shaped accessors use them. */
    fun deduplicatedColumnNames(): Array<String>

    /** The resolved type of the column at `columnIndex`. */
    fun columnType(columnIndex: Double): DuckDBType

    /** The logical type of the column at `columnIndex`. */
    fun columnLogicalType(columnIndex: Double): DuckDBLogicalType

    /** Frees the result synchronously. */
    fun closeSync(): Unit
}

/** Upstream `class DuckDBMaterializedResult`: a result whose chunks are already in memory. */
external class DuckDBMaterializedResult : DuckDBResult {
    /** The number of rows. */
    val rowCount: Double

    /** The number of data chunks. */
    val chunkCount: Double
}

/**
 * Upstream `class DuckDBResultReader`: reads a [DuckDBResult] chunk by chunk.
 *
 * [readAll] must run before the accessors return data; [done] tells whether every chunk has been
 * consumed.
 */
external class DuckDBResultReader {
    /** The number of columns. */
    val columnCount: Double

    /** The number of rows read so far. */
    val currentRowCount: Double

    /** Whether every chunk has been read. */
    val done: Boolean

    /** The number of rows changed by a statement that modifies data. */
    val rowsChanged: Double

    /** The name of the column at `columnIndex`. */
    fun columnName(columnIndex: Double): String

    /** The column names, in order. */
    fun columnNames(): Array<String>

    /** The column names with duplicates disambiguated. */
    fun deduplicatedColumnNames(): Array<String>

    /** The resolved type of the column at `columnIndex`. */
    fun columnType(columnIndex: Double): DuckDBType

    /** Reads the next chunk; [done] is set once every chunk has been read. */
    fun readAll(): Promise<JsAny?>

    /** Reads until `targetRowCount` rows are buffered (or the result is exhausted). */
    fun readUntil(targetRowCount: Double): Promise<JsAny?>

    /** The value at `columnIndex` / `rowIndex`; the row must already be read. */
    fun value(columnIndex: Double, rowIndex: Double): DuckDBValue

    /** Every row, as arrays of values. */
    fun getRows(): Array<Array<DuckDBValue>>

    /** Every row, as objects keyed by column name. */
    fun getRowObjects(): Array<JsAny>

    /** Every column, as arrays of values. */
    fun getColumns(): Array<Array<DuckDBValue>>

    /** Every column, keyed by column name. */
    fun getColumnsObject(): JsAny

    /** Every row converted to plain JavaScript values. */
    fun getRowsJS(): Array<Array<JS>>

    /** Every row converted to JSON. */
    fun getRowsJson(): Array<Array<Json>>

    /** Every row object converted to JSON. */
    fun getRowObjectsJson(): Array<Json>

    /** The column types as JSON. */
    fun columnTypesJson(): Json

    /** Frees the underlying result synchronously. */
    fun closeSync(): Unit
}
