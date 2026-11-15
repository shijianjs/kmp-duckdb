@file:JsModule("apache-arrow")

package io.github.shijianjs.ktjs.duckdb.arrow

import kotlin.js.JsAny
import kotlin.js.JsName

/**
 * Upstream `Buffers<T>`: the four buffers a [Data] can carry, keyed by the numeric [BufferType].
 *
 * Callers rarely build one by hand — [makeData] takes the same buffers as named properties — but
 * [Data.buffers] hands one back, and it is what upstream's `new Data(type, offset, length, nullCount,
 * buffers)` argument expects.
 */
external interface Buffers {
    /** Buffer slot 0: value offsets of a list / variable-length type, or the type ids of a dense union. */
    @JsName("0")
    val valueOffsets: ArrowTypedArray?

    /** Buffer slot 1: the values. */
    @JsName("1")
    val data: ArrowTypedArray?

    /** Buffer slot 2: the validity bitmap. */
    @JsName("2")
    val nullBitmap: ArrowTypedArray?

    /** Buffer slot 3: the type ids of a (sparse) union. */
    @JsName("3")
    val typeIds: ArrowTypedArray?
}

/**
 * Upstream `DataProps`: the named-buffer form [makeData] accepts.
 *
 * Every nested type has its own required members upstream (`valueOffsets` for lists, `children` for
 * structs, `child` for lists of one child …); this one interface carries all of them as optional so
 * one Kotlin type covers the union of upstream's per-type props.
 */
external interface DataProps {
    /** The type of the data. */
    var type: DataType

    /** The number of values; required for the scalar types. */
    var length: Double?

    /** The offset into the buffers; `0` when omitted. */
    var offset: Double?

    /** The number of null values; `-1` (unknown) when omitted and a null bitmap is present. */
    var nullCount: Double?

    /** The validity bitmap. */
    var nullBitmap: ArrowTypedArray?

    /** The values buffer. */
    var data: ArrowTypedArray?

    /** The value offsets buffer of a list / variable-length type. */
    var valueOffsets: ArrowTypedArray?

    /** The type ids buffer of a union. */
    var typeIds: ArrowTypedArray?

    /** The children of a struct / union. */
    var children: Array<Data>?

    /** The single child of a list / map / fixed-size list. */
    var child: Data?

    /** The dictionary values of a dictionary-encoded column. */
    var dictionary: Vector<JsAny>?

    /** Additional buffers referenced by `BinaryView` / `Utf8View`. */
    var variadicBuffers: Array<ArrowBytes>?
}

/**
 * Upstream `class Data`: one chunk of a column's physical storage.
 *
 * A [Vector] wraps a list of these; most callers get them from Arrow or from [makeData] rather than
 * constructing them by hand.
 */
external class Data {
    /** The type of this data. */
    val type: DataType

    /** The type id of [type]. */
    val typeId: Type

    /** The number of values in this data. */
    val length: Double

    /** The offset into the buffers where this data starts. */
    val offset: Double

    /** The byte stride of one value. */
    val stride: Double

    /** Child data of a nested type. */
    val children: Array<Data>

    /** The dictionary values, for a dictionary-encoded column. */
    val dictionary: Vector<JsAny>?

    /** The values buffer, typed by the column's physical layout. */
    val values: ArrowTypedArray?

    /** The type ids buffer of a union. */
    val typeIds: ArrowTypedArray?

    /** The validity bitmap; `null` when the column has no nulls. */
    val nullBitmap: ArrowTypedArray?

    /** The value offsets buffer of a list / variable-length type. */
    val valueOffsets: ArrowTypedArray?

    /** Additional buffers referenced by `BinaryView` / `Utf8View`. */
    val variadicBuffers: Array<ArrowBytes>

    /** Whether any value can be null. */
    val nullable: Boolean

    /** The number of null values; `-1` when it has not been computed. */
    val nullCount: Double

    /** The byte length of the values this data covers. */
    val byteLength: Double

    /** Upstream's `buffers`: the four buffers keyed by [BufferType]. */
    val buffers: Buffers

    /** @param type the column type @param offset the offset into the buffers @param length the value count */
    constructor(type: DataType, offset: Double, length: Double)

    /** @param type the column type @param offset the offset into the buffers @param length the value count @param nullCount the null count */
    constructor(type: DataType, offset: Double, length: Double, nullCount: Double)

    /** @param type the column type @param offset the offset into the buffers @param length the value count @param nullCount the null count @param buffers the buffers @param children the children of a nested type */
    constructor(
        type: DataType,
        offset: Double,
        length: Double,
        nullCount: Double?,
        buffers: Buffers?,
        children: Array<Data>,
    )

    /** Upstream's `getValid`: whether the value at `index` is not null. */
    fun getValid(index: Double): Boolean

    /** Upstream's `setValid`: marks the value at `index` valid or null, and grows the bitmap if needed. */
    fun setValid(index: Double, value: Boolean): Boolean

    /** Upstream's `slice`: a view of `length` values starting at `offset`. */
    fun slice(offset: Double, length: Double): Data

    /** Upstream's `clone(props)`: a copy with the given members replaced. */
    fun clone(
        type: DataType?,
        offset: Double?,
        length: Double?,
        nullCount: Double?,
        buffers: Buffers?,
        children: Array<Data>?,
    ): Data

    /** Upstream's `toString`. */
    override fun toString(): String
}

/**
 * Upstream's `makeData(props)`: the ergonomic constructor for [Data].
 *
 * Upstream normalizes the buffers per type and derives the missing `length` / `nullCount`; building
 * props with `unsafeJso<DataProps> { … }` is the intended way to call it.
 */
external fun makeData(props: DataProps): Data
