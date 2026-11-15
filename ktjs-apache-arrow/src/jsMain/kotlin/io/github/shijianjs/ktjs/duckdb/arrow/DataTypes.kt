@file:Suppress("FunctionName", "ClassName")

@file:JsModule("apache-arrow")

package io.github.shijianjs.ktjs.duckdb.arrow

import js.objects.Record
import js.typedarrays.Int32Array
import kotlin.js.JsAny
import kotlin.js.JsName

/*
 * The Arrow data type hierarchy. Upstream's classes are generic over the physical layout
 * (`DataType<Type.Int32, ...>`); Kotlin cannot express that phantom-type machinery, so each class
 * here is the same class with its concrete runtime members. The generic parameters only affect
 * typing of values, which Kotlin gets from [Vector.get] overloads on the concrete vector instead.
 */

/**
 * Upstream `abstract class DataType`, the logical type of a column.
 *
 * Instances are created through the subclasses ([Int32], [Utf8], [TimestampMillisecond] …), which is
 * what upstream does as well; this base class has no public constructor.
 */
external open class DataType {
    /** The upstream [Type] identifier of this type. */
    val typeId: Type

    /** Child fields of a nested type; `null` for the scalar types. */
    val children: Array<Field>?

    /** Upstream's `toString`: a readable type expression such as `Int32` or `List<Int32>`. */
    override fun toString(): String
}

/** Upstream `class Null`: a column with no physical storage. */
external class Null : DataType

/** Upstream `class Bool`: one bit per value, LSB bit-packed. */
external class Bool : DataType

/**
 * Upstream `interface/class Int`, exported as `Int`.
 *
 * Renamed in Kotlin because `Int` is a Kotlin primitive type; the JS name is unchanged, so the
 * class imported from the package is still `Int`.
 */
@JsName("Int")
external open class IntType : DataType {
    /** `true` for the signed variants ([Int8], [Int16], [Int32], [Int64]). */
    val isSigned: Boolean

    /** 8, 16, 32 or 64. */
    val bitWidth: Double
}

/** Upstream `class Int8`. */
external class Int8 : IntType

/** Upstream `class Int16`. */
external class Int16 : IntType

/** Upstream `class Int32`. */
external class Int32 : IntType

/** Upstream `class Int64`. */
external class Int64 : IntType

/** Upstream `class Uint8`. */
external class Uint8 : IntType

/** Upstream `class Uint16`. */
external class Uint16 : IntType

/** Upstream `class Uint32`. */
external class Uint32 : IntType

/** Upstream `class Uint64`. */
external class Uint64 : IntType

/**
 * Upstream `class Float`, the base of the floating point widths.
 *
 * Renamed in Kotlin because `Float` is a Kotlin primitive type; the JS name is unchanged.
 */
@JsName("Float")
external open class FloatType : DataType {
    /** [Precisions.HALF], [Precisions.SINGLE] or [Precisions.DOUBLE]. */
    val precision: Precision
}

/** Upstream `class Float16`. */
external class Float16 : FloatType

/** Upstream `class Float32`. */
external class Float32 : FloatType

/** Upstream `class Float64`. */
external class Float64 : FloatType

/** Upstream `class Binary`: variable-length bytes, 32-bit offsets. */
external class Binary : DataType

/** Upstream `class BinaryView`: variable-length bytes backed by inline-or-referenced views. */
external class BinaryView : DataType

/** Upstream `class LargeBinary`: variable-length bytes, 64-bit offsets. */
external class LargeBinary : DataType

/** Upstream `class Utf8`: UTF-8 strings, 32-bit offsets. */
external class Utf8 : DataType

/** Upstream `class Utf8View`: UTF-8 strings backed by inline-or-referenced views. */
external class Utf8View : DataType

/** Upstream `class LargeUtf8`: UTF-8 strings, 64-bit offsets. */
external class LargeUtf8 : DataType

/** Upstream `class Decimal`. */
external class Decimal : DataType {
    /** Number of digits after the decimal point; negative values shift left. */
    val scale: Double

    /** Number of significant digits. */
    val precision: Double

    /** Storage width in bits: 128 (default) or 256. */
    val bitWidth: Double

    /** @param scale the digits after the decimal point @param precision the significant digits @param bitWidth 128 or 256 */
    constructor(scale: Double, precision: Double, bitWidth: Double)

    /** @param scale the digits after the decimal point @param precision the significant digits; the width is 128 */
    constructor(scale: Double, precision: Double)
}

/** Upstream `interface/class Date_`, the base of the date widths. */
external open class Date_ : DataType {
    /** [DateUnits.DAY] or [DateUnits.MILLISECOND]. */
    val unit: DateUnit
}

/** Upstream `class DateDay`: days since the Unix epoch, as an `Int32Array`. */
external class DateDay : Date_

/** Upstream `class DateMillisecond`: milliseconds since the Unix epoch, as a `Float64Array`. */
external class DateMillisecond : Date_

/** Upstream `class Time`, the base of the time-of-day widths. */
external open class Time : DataType {
    /** The [TimeUnit] of this type. */
    val unit: TimeUnit

    /** 32 for second/millisecond, 64 for microsecond/nanosecond. */
    val bitWidth: Double
}

/** Upstream `class TimeSecond`. */
external class TimeSecond : Time

/** Upstream `class TimeMillisecond`. */
external class TimeMillisecond : Time

/** Upstream `class TimeMicrosecond`. */
external class TimeMicrosecond : Time

/** Upstream `class TimeNanosecond`. */
external class TimeNanosecond : Time

/** Upstream `class Timestamp`, the base of the timestamp widths. */
external open class Timestamp : DataType {
    /** The [TimeUnit] of this type. */
    val unit: TimeUnit

    /** The IANA time zone the values are expressed in, if any. */
    val timezone: String?
}

/** Upstream `class TimestampSecond`. */
external class TimestampSecond : Timestamp {
    /** The values are UTC. */
    constructor()

    /** @param timezone the IANA time zone the values are expressed in */
    constructor(timezone: String?)
}

/** Upstream `class TimestampMillisecond`. */
external class TimestampMillisecond : Timestamp {
    /** The values are UTC. */
    constructor()

    /** @param timezone the IANA time zone the values are expressed in */
    constructor(timezone: String?)
}

/** Upstream `class TimestampMicrosecond`. */
external class TimestampMicrosecond : Timestamp {
    /** The values are UTC. */
    constructor()

    /** @param timezone the IANA time zone the values are expressed in */
    constructor(timezone: String?)
}

/** Upstream `class TimestampNanosecond`. */
external class TimestampNanosecond : Timestamp {
    /** The values are UTC. */
    constructor()

    /** @param timezone the IANA time zone the values are expressed in */
    constructor(timezone: String?)
}

/** Upstream `class Interval`, the base of the interval layouts. */
external open class Interval : DataType {
    /** The [IntervalUnit] of this type. */
    val unit: IntervalUnit
}

/** Upstream `class IntervalDayTime`. */
external class IntervalDayTime : Interval

/** Upstream `class IntervalYearMonth`. */
external class IntervalYearMonth : Interval

/** Upstream `class IntervalMonthDayNano`. */
external class IntervalMonthDayNano : Interval

/** Upstream `class Duration`, the base of the duration widths. */
external open class Duration : DataType {
    /** The [TimeUnit] of this type. */
    val unit: TimeUnit
}

/** Upstream `class DurationSecond`. */
external class DurationSecond : Duration

/** Upstream `class DurationMillisecond`. */
external class DurationMillisecond : Duration

/** Upstream `class DurationMicrosecond`. */
external class DurationMicrosecond : Duration

/** Upstream `class DurationNanosecond`. */
external class DurationNanosecond : Duration

/**
 * Upstream `class List`, exported as `List`.
 *
 * Renamed in Kotlin because `List` is a Kotlin collection type; the JS name is unchanged.
 */
@JsName("List")
external open class ListType : DataType {
    /** The element field; the list is always a single-child nested type. */
    val valueField: Field

    /** The element type, i.e. `valueField.type`. */
    val valueType: DataType

    /** @param child the element field */
    constructor(child: Field)
}

/** Upstream `class LargeList`: like [ListType] but with 64-bit value offsets. */
external open class LargeList : DataType {
    /** The element field. */
    val valueField: Field

    /** The element type. */
    val valueType: DataType

    /** @param child the element field */
    constructor(child: Field)
}

/** Upstream `class Struct`: a nested type with named children. */
external open class Struct : DataType {
    /** @param children the fields of the struct */
    constructor(children: Array<Field>)
}

/** Upstream `interface/class Union`, the base of the union layouts. */
external open class Union : DataType {
    /** [UnionModes.Sparse] or [UnionModes.Dense]. */
    val mode: UnionMode

    /** The type id assigned to each child, in child order. */
    val typeIds: Int32Array<*>

    /** Maps a type id to the index of its child field. */
    val typeIdToChildIndex: Record<JsAny, Double>
}

/** Upstream `class DenseUnion`: each value carries a type id, children are contiguous. */
external class DenseUnion : Union {
    /** @param typeIds one type id per child @param children the child fields */
    constructor(typeIds: Array<Double>, children: Array<Field>)
}

/** Upstream `class SparseUnion`: children are aligned with the parent's length. */
external class SparseUnion : Union {
    /** @param typeIds one type id per child @param children the child fields */
    constructor(typeIds: Array<Double>, children: Array<Field>)
}

/** Upstream `class FixedSizeBinary`: every value has the same byte width. */
external class FixedSizeBinary : DataType {
    /** The byte width of each value. */
    val byteWidth: Double

    /** @param byteWidth the byte width of each value */
    constructor(byteWidth: Double)
}

/** Upstream `class FixedSizeList`: every value has the same number of elements. */
external class FixedSizeList : DataType {
    /** The element count of each value. */
    val listSize: Double

    /** The element field. */
    val valueField: Field

    /** The element type. */
    val valueType: DataType

    /** @param listSize the element count of each value @param child the element field */
    constructor(listSize: Double, child: Field)
}

/** Upstream `class Map_`: a list of `{key, value}` structs. */
external class Map_ : DataType {
    /** Whether the upstream producer guarantees sorted keys. */
    val keysSorted: Boolean

    /** The key type of the entries struct. */
    val keyType: DataType

    /** The value type of the entries struct. */
    val valueType: DataType

    /** The `struct<key, value>` type of the entries child. */
    val childType: Struct

    /** @param entries the `struct<key, value>` entries field @param keysSorted whether keys are sorted */
    constructor(entries: Field, keysSorted: Boolean)

    /** @param entries the `struct<key, value>` entries field; keys are not guaranteed sorted */
    constructor(entries: Field)
}

/** Upstream `class Dictionary`: values are indices into a dictionary of values. */
external class Dictionary : DataType {
    /** The dictionary id, used to group batches that share a dictionary. */
    val id: Double

    /** The index type, one of the integer types. */
    val indices: IntType

    /** The dictionary value type. */
    val dictionary: DataType

    /** Whether the dictionary order is meaningful. */
    val isOrdered: Boolean

    /** The dictionary value type; upstream's `valueType`. */
    val valueType: DataType

    /** @param dictionary the value type @param indices the index type @param id the dictionary id @param isOrdered whether the order is meaningful */
    constructor(dictionary: DataType, indices: IntType, id: Double?, isOrdered: Boolean?)

    /** @param dictionary the value type @param indices the index type; upstream assigns the id */
    constructor(dictionary: DataType, indices: IntType)
}

