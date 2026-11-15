@file:JsModule("apache-arrow")

package io.github.shijianjs.ktjs.duckdb.arrow

import kotlin.js.JsAny

/**
 * Upstream `class Table`: a set of [RecordBatch]es sharing one [Schema].
 *
 * The upstream constructor accepts batches, column vectors, column data, a `[Schema, batches]` pair
 * or nothing; Kotlin overloads mirror the forms that are actually distinguishable. The table from a
 * DuckDB query arrives already built, so most callers read rather than construct.
 */
external class Table {
    /** The schema shared by every batch. */
    val schema: Schema

    /** The batches, in row order. */
    val batches: Array<RecordBatch>

    /** The struct data of every batch. */
    val data: Array<Data>

    /** The number of columns. */
    val numCols: Double

    /** The number of rows across all batches. */
    val numRows: Double

    /** The number of null rows; `-1` when it has not been computed. */
    val nullCount: Double

    /** The number of rows. */
    val length: Double

    /** Upstream's `get`: the row at `index` (an empty table answers `null`). */
    fun get(index: Double): StructRow?

    /** Like [get], resolving the batch offsets. */
    fun at(index: Double): StructRow?

    /** Whether the row at `index` is valid. */
    fun isValid(index: Double): Boolean

    /** The index of the first row equal to `element`, or `-1`. */
    fun indexOf(element: StructRow, offset: Double): Double

    /** Every row of the table. */
    fun toArray(): Array<StructRow?>

    /** A table holding this table's rows followed by the others'. */
    fun concat(vararg others: Table): Table

    /** A table of the rows in `[begin, end)`. */
    fun slice(begin: Double?, end: Double?): Table

    /** The column vector with the given field name. */
    fun getChild(name: String): Vector<JsAny>?

    /** The column vector at the given field index; `null` when the index is out of range. */
    fun getChildAt(index: Double): Vector<JsAny>?

    /** A table with only the named columns. */
    fun select(columnNames: Array<String>): Table

    /** A table with only the columns at the given indices. */
    fun selectAt(columnIndices: Array<Double>): Table

    /** Upstream's `assign`: `this` with `other`'s columns appended (same-name columns are replaced). */
    fun assign(other: Table): Table

    /** Upstream's `toString`: the schema followed by the rows. */
    override fun toString(): String

    /** An empty table with an empty schema. */
    constructor()

    /** @param schema the schema @param batches the batches that hold the rows */
    constructor(schema: Schema, batches: Array<RecordBatch>)

    /** @param schema the schema; the table gets no rows */
    constructor(schema: Schema)

    /** @param batches the batches; the schema is taken from the first one */
    constructor(batches: Array<RecordBatch>)
}

/** Upstream's `makeTable(input)`: builds a table from an object of typed arrays. */
external fun makeTable(input: JsAny): Table

/** Upstream's `tableFromArrays(input)`: builds a table from typed arrays or plain JS arrays. */
external fun tableFromArrays(input: JsAny): Table
