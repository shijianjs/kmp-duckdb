@file:JsModule("apache-arrow")

package io.github.shijianjs.ktjs.duckdb.arrow

/**
 * Upstream `class RecordBatch`: a horizontal slice of a table — one struct-typed [Data] chunk plus
 * the [Schema] that describes it.
 */
external class RecordBatch {
    /** The schema of this batch. */
    val schema: Schema

    /** The struct data holding the columns of this batch. */
    val data: Data

    /** The number of columns. */
    val numCols: Double

    /** The number of rows. */
    val numRows: Double

    /** @param schema the schema @param data the struct data; upstream synthesizes an empty chunk when omitted */
    constructor(schema: Schema, data: Data)

    /** Whether the row at `index` is valid (struct rows never carry a null bitmap, so this is `true`). */
    fun isValid(index: Double): Boolean

    /** The row at `index`. */
    fun get(index: Double): StructRow?

    /** Like [get], resolving the batch offset. */
    fun at(index: Double): StructRow?

    /** Upstream's `set`: writes a row into this batch's buffers. */
    fun set(index: Double, value: StructRow?)

    /** The index of the first row equal to `element`, or `-1`. */
    fun indexOf(element: StructRow, offset: Double): Double

    /** Every row of this batch. */
    fun toArray(): Array<StructRow?>

    /** A batch holding this batch's rows followed by the others'. */
    fun concat(vararg others: RecordBatch): RecordBatch

    /** A batch of the rows in `[begin, end)`. */
    fun slice(begin: Double?, end: Double?): RecordBatch

    /** The column vector with the given field name. */
    fun getChild(name: String): Vector<kotlin.js.JsAny>?

    /** The column vector at the given field index. */
    fun getChildAt(index: Double): Vector<kotlin.js.JsAny>?

    /** A batch with only the named columns. */
    fun select(columnNames: Array<String>): RecordBatch

    /** A batch with only the columns at the given indices. */
    fun selectAt(columnIndices: Array<Double>): RecordBatch

    /** Upstream's `toString`. */
    override fun toString(): String
}
