@file:Suppress("FunctionName")

package io.github.shijianjs.ktjs.duckdb.arrow

import kotlin.js.unsafeCast

/*
 * The upstream enums of `apache-arrow`. They are TypeScript numeric enums: at runtime a value is
 * a plain number and the enum object only provides the name→number lookup. Kotlin models the value
 * type as a sealed external interface over that number, so a member typed `Type` accepts nothing
 * but one of the constants below (or a value wrapped from JavaScript), while an `enum class` would
 * have produced a Kotlin object the library would not recognize.
 */

/** Upstream `enum Type`: the logical Arrow data type identifier. */
sealed external interface Type

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [Types]. */
inline fun Type(value: Double): Type = value.unsafeCast<Type>()

/** Every literal of [Type], with the value the upstream enum assigns it. */
object Types {
    inline val NONE: Type get() = Type(0.0)
    inline val Null: Type get() = Type(1.0)
    inline val Int: Type get() = Type(2.0)
    inline val Float: Type get() = Type(3.0)
    inline val Binary: Type get() = Type(4.0)
    inline val Utf8: Type get() = Type(5.0)
    inline val Bool: Type get() = Type(6.0)
    inline val Decimal: Type get() = Type(7.0)
    inline val Date: Type get() = Type(8.0)
    inline val Time: Type get() = Type(9.0)
    inline val Timestamp: Type get() = Type(10.0)
    inline val Interval: Type get() = Type(11.0)
    inline val List: Type get() = Type(12.0)
    inline val Struct: Type get() = Type(13.0)
    inline val Union: Type get() = Type(14.0)
    inline val FixedSizeBinary: Type get() = Type(15.0)
    inline val FixedSizeList: Type get() = Type(16.0)
    inline val Map: Type get() = Type(17.0)
    inline val Duration: Type get() = Type(18.0)
    inline val LargeBinary: Type get() = Type(19.0)
    inline val LargeUtf8: Type get() = Type(20.0)
    inline val LargeList: Type get() = Type(21.0)
    inline val BinaryView: Type get() = Type(23.0)
    inline val Utf8View: Type get() = Type(24.0)
    inline val Dictionary: Type get() = Type(-1.0)
    inline val Int8: Type get() = Type(-2.0)
    inline val Int16: Type get() = Type(-3.0)
    inline val Int32: Type get() = Type(-4.0)
    inline val Int64: Type get() = Type(-5.0)
    inline val Uint8: Type get() = Type(-6.0)
    inline val Uint16: Type get() = Type(-7.0)
    inline val Uint32: Type get() = Type(-8.0)
    inline val Uint64: Type get() = Type(-9.0)
    inline val Float16: Type get() = Type(-10.0)
    inline val Float32: Type get() = Type(-11.0)
    inline val Float64: Type get() = Type(-12.0)
    inline val DateDay: Type get() = Type(-13.0)
    inline val DateMillisecond: Type get() = Type(-14.0)
    inline val TimestampSecond: Type get() = Type(-15.0)
    inline val TimestampMillisecond: Type get() = Type(-16.0)
    inline val TimestampMicrosecond: Type get() = Type(-17.0)
    inline val TimestampNanosecond: Type get() = Type(-18.0)
    inline val TimeSecond: Type get() = Type(-19.0)
    inline val TimeMillisecond: Type get() = Type(-20.0)
    inline val TimeMicrosecond: Type get() = Type(-21.0)
    inline val TimeNanosecond: Type get() = Type(-22.0)
    inline val DenseUnion: Type get() = Type(-23.0)
    inline val SparseUnion: Type get() = Type(-24.0)
    inline val IntervalDayTime: Type get() = Type(-25.0)
    inline val IntervalYearMonth: Type get() = Type(-26.0)
    inline val DurationSecond: Type get() = Type(-27.0)
    inline val DurationMillisecond: Type get() = Type(-28.0)
    inline val DurationMicrosecond: Type get() = Type(-29.0)
    inline val DurationNanosecond: Type get() = Type(-30.0)
    inline val IntervalMonthDayNano: Type get() = Type(-31.0)
}

/** Upstream `enum BufferType`: the slot a [Data] buffer occupies. */
sealed external interface BufferType

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [BufferTypes]. */
inline fun BufferType(value: Double): BufferType = value.unsafeCast<BufferType>()

/** Every literal of [BufferType]. */
object BufferTypes {
    /** Value offsets of a list / variable-length type (or the type ids of a dense union). */
    inline val OFFSET: BufferType get() = BufferType(0.0)

    /** The actual values. */
    inline val DATA: BufferType get() = BufferType(1.0)

    /** The validity (null) bitmap. */
    inline val VALIDITY: BufferType get() = BufferType(2.0)

    /** The type ids of a union. */
    inline val TYPE: BufferType get() = BufferType(3.0)
}

/** Upstream `enum DateUnit`. */
sealed external interface DateUnit

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [DateUnits]. */
inline fun DateUnit(value: Double): DateUnit = value.unsafeCast<DateUnit>()

/** Every literal of [DateUnit]. */
object DateUnits {
    inline val DAY: DateUnit get() = DateUnit(0.0)
    inline val MILLISECOND: DateUnit get() = DateUnit(1.0)
}

/** Upstream `enum TimeUnit`, shared by time, timestamp and duration types. */
sealed external interface TimeUnit

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [TimeUnits]. */
inline fun TimeUnit(value: Double): TimeUnit = value.unsafeCast<TimeUnit>()

/** Every literal of [TimeUnit]. */
object TimeUnits {
    inline val SECOND: TimeUnit get() = TimeUnit(0.0)
    inline val MILLISECOND: TimeUnit get() = TimeUnit(1.0)
    inline val MICROSECOND: TimeUnit get() = TimeUnit(2.0)
    inline val NANOSECOND: TimeUnit get() = TimeUnit(3.0)
}

/** Upstream `enum IntervalUnit`. */
sealed external interface IntervalUnit

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [IntervalUnits]. */
inline fun IntervalUnit(value: Double): IntervalUnit = value.unsafeCast<IntervalUnit>()

/** Every literal of [IntervalUnit]. */
object IntervalUnits {
    inline val YEAR_MONTH: IntervalUnit get() = IntervalUnit(0.0)
    inline val DAY_TIME: IntervalUnit get() = IntervalUnit(1.0)
    inline val MONTH_DAY_NANO: IntervalUnit get() = IntervalUnit(2.0)
}

/** Upstream `enum Precision`: the width of a floating point value. */
sealed external interface Precision

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [Precisions]. */
inline fun Precision(value: Double): Precision = value.unsafeCast<Precision>()

/** Every literal of [Precision]. */
object Precisions {
    inline val HALF: Precision get() = Precision(0.0)
    inline val SINGLE: Precision get() = Precision(1.0)
    inline val DOUBLE: Precision get() = Precision(2.0)
}

/** Upstream `enum UnionMode`. */
sealed external interface UnionMode

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [UnionModes]. */
inline fun UnionMode(value: Double): UnionMode = value.unsafeCast<UnionMode>()

/** Every literal of [UnionMode]. */
object UnionModes {
    inline val Sparse: UnionMode get() = UnionMode(0.0)
    inline val Dense: UnionMode get() = UnionMode(1.0)
}

/** Upstream `enum MetadataVersion`: the Arrow IPC format version of a schema. */
sealed external interface MetadataVersion

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [MetadataVersions]. */
inline fun MetadataVersion(value: Double): MetadataVersion = value.unsafeCast<MetadataVersion>()

/** Every literal of [MetadataVersion]. */
object MetadataVersions {
    inline val V1: MetadataVersion get() = MetadataVersion(0.0)
    inline val V2: MetadataVersion get() = MetadataVersion(1.0)
    inline val V3: MetadataVersion get() = MetadataVersion(2.0)
    inline val V4: MetadataVersion get() = MetadataVersion(3.0)
    inline val V5: MetadataVersion get() = MetadataVersion(4.0)
}

/** Upstream `enum MessageHeader`: the kind of an IPC message. */
sealed external interface MessageHeader

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [MessageHeaders]. */
inline fun MessageHeader(value: Double): MessageHeader = value.unsafeCast<MessageHeader>()

/** Every literal of [MessageHeader]. */
object MessageHeaders {
    inline val NONE: MessageHeader get() = MessageHeader(0.0)
    inline val Schema: MessageHeader get() = MessageHeader(1.0)
    inline val DictionaryBatch: MessageHeader get() = MessageHeader(2.0)
    inline val RecordBatch: MessageHeader get() = MessageHeader(3.0)
    inline val Tensor: MessageHeader get() = MessageHeader(4.0)
    inline val SparseTensor: MessageHeader get() = MessageHeader(5.0)
}

/** Upstream `enum CompressionType`: the codec of a compressed IPC body. */
sealed external interface CompressionType

/** Wraps a numeric value coming from JavaScript. Prefer the constants in [CompressionTypes]. */
inline fun CompressionType(value: Double): CompressionType = value.unsafeCast<CompressionType>()

/** Every literal of [CompressionType]. */
object CompressionTypes {
    inline val LZ4_FRAME: CompressionType get() = CompressionType(0.0)
    inline val ZSTD: CompressionType get() = CompressionType(1.0)
}
