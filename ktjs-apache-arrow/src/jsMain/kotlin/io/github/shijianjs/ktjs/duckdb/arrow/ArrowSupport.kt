package io.github.shijianjs.ktjs.duckdb.arrow

import js.collections.JsMap
import js.typedarrays.TypedArray
import js.typedarrays.Uint8Array
import kotlin.js.unsafeCast

/*
 * Kotlin-side helpers of the Arrow wrapper.
 *
 * They live outside the `@JsModule("apache-arrow")` files on purpose: Kotlin allows nothing but
 * external declarations in a file that carries that annotation, and inline factories, constant
 * objects and type aliases are ordinary Kotlin declarations.
 */

/**
 * Upstream's `TypedArray`: any JS typed array.
 *
 * Upstream's own type is `TypedArray` from `@types/node`, where the four type parameters describe the
 * element and buffer layout; no caller of this wrapper names them, so the alias fixes them to star
 * projections and keeps the member typed.
 */
typealias ArrowTypedArray = TypedArray<*, *, *, *>

/** Upstream's `Uint8Array`: the byte-level buffer of the IPC functions. */
typealias ArrowBytes = Uint8Array<*>

/** Upstream's `DataType.isNull`. */
val DataType.isNull: Boolean get() = typeId == Types.Null

/** Upstream's `DataType.isInt`: any signed or unsigned integer width. */
val DataType.isInt: Boolean get() = typeId == Types.Int

/** Upstream's `DataType.isFloat`: any floating point width. */
val DataType.isFloat: Boolean get() = typeId == Types.Float

/** Upstream's `DataType.isBinary`. */
val DataType.isBinary: Boolean get() = typeId == Types.Binary

/** Upstream's `DataType.isUtf8`. */
val DataType.isUtf8: Boolean get() = typeId == Types.Utf8

/** Upstream's `DataType.isBool`. */
val DataType.isBool: Boolean get() = typeId == Types.Bool

/** Upstream's `DataType.isDecimal`. */
val DataType.isDecimal: Boolean get() = typeId == Types.Decimal

/** Upstream's `DataType.isDate`. */
val DataType.isDate: Boolean get() = typeId == Types.Date

/** Upstream's `DataType.isTime`. */
val DataType.isTime: Boolean get() = typeId == Types.Time

/** Upstream's `DataType.isTimestamp`. */
val DataType.isTimestamp: Boolean get() = typeId == Types.Timestamp

/** Upstream's `DataType.isInterval`. */
val DataType.isInterval: Boolean get() = typeId == Types.Interval

/** Upstream's `DataType.isList`. */
val DataType.isList: Boolean get() = typeId == Types.List

/** Upstream's `DataType.isStruct`. */
val DataType.isStruct: Boolean get() = typeId == Types.Struct

/** Upstream's `DataType.isUnion`. */
val DataType.isUnion: Boolean get() = typeId == Types.Union

/** Upstream's `DataType.isFixedSizeBinary`. */
val DataType.isFixedSizeBinary: Boolean get() = typeId == Types.FixedSizeBinary

/** Upstream's `DataType.isFixedSizeList`. */
val DataType.isFixedSizeList: Boolean get() = typeId == Types.FixedSizeList

/** Upstream's `DataType.isMap`. */
val DataType.isMap: Boolean get() = typeId == Types.Map

/** Upstream's `DataType.isDictionary`. */
val DataType.isDictionary: Boolean get() = typeId == Types.Dictionary

/** Upstream's `DataType.isDenseUnion`. */
val DataType.isDenseUnion: Boolean get() = typeId == Types.DenseUnion

/** Upstream's `DataType.isSparseUnion`. */
val DataType.isSparseUnion: Boolean get() = typeId == Types.SparseUnion

/** Wraps a string coming from JavaScript. Prefer the constants in [IPCTypes]. */
inline fun IPCWriteType(value: String): IPCWriteType = value.unsafeCast<IPCWriteType>()

/** The literals of [IPCWriteType]. */
object IPCTypes {
    /** A file container: one schema, then the batches, closed by a footer. */
    inline val file: IPCWriteType get() = IPCWriteType("file")

    /** A stream container: a schema message followed by batch messages. */
    inline val stream: IPCWriteType get() = IPCWriteType("stream")
}

/**
 * Upstream's `Field.new(name, type, nullable?, metadata?)`, in the positional form.
 *
 * Upstream also accepts a props object; the two end up in the same constructor, so this is the
 * typed shorthand for the common case.
 */
fun field(name: String, type: DataType, nullable: Boolean, metadata: JsMap<String, String>): Field =
    Field(name, type, nullable, metadata)

/** Upstream's `Field.new(name, type)`: a non-nullable field with no metadata. */
fun field(name: String, type: DataType): Field = Field(name, type)

/** Upstream's `new Schema(fields)`: a schema from its fields, with empty metadata. */
fun schema(fields: Array<Field>): Schema = Schema(fields)
