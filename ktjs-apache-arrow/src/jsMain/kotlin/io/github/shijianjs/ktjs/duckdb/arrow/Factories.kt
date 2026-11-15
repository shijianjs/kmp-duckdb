@file:JsModule("apache-arrow")

package io.github.shijianjs.ktjs.duckdb.arrow

import kotlin.js.JsAny

/** Upstream's `BuilderOptions`: what every builder is constructed from. */
external interface BuilderOptions {
    /** The type of the values the builder appends. */
    var type: DataType

    /** The values that mean "null" for this builder; upstream defaults to `[null]`. */
    var nullValues: Array<JsAny>?
}

/**
 * Upstream's `BuilderOptions` plus the row emission contract of upstream's `builderThroughIterable`
 * and `builderThroughAsyncIterable` (not modelled yet, see `docs/development/arrow-coverage.md`).
 */
external interface IterableBuilderOptions : BuilderOptions {
    /** How many values to accumulate before emitting a batch; upstream defaults to 1 for the first batch. */
    var highWaterMark: Double?
    /** How many values make up one value of this builder (1 for scalar types). */
    var queueingStrategy: JsAny?
}

/**
 * Upstream `abstract class Builder`: accumulates values and flushes them into [Data].
 *
 * Concrete builders are obtained from [makeBuilder] rather than constructed directly; the type is
 * not generic in Kotlin, so [append] / [set] take the JS value the concrete builder expects.
 */
external open class Builder {
    /** The type of the values this builder produces. */
    val type: DataType

    /** The number of values appended so far. */
    val length: Double

    /** Whether [finish] has been called. */
    val finished: Boolean

    /** The number of bytes between two values. */
    val stride: Double

    /** The child builders of a nested type. */
    val children: Array<Builder>

    /** The number of null values appended. */
    val nullCount: Double

    /** The number of child builders. */
    val numChildren: Double

    /** The byte length of the values accumulated so far. */
    val byteLength: Double

    /** The reserved value capacity. */
    val reservedLength: Double

    /** The reserved byte capacity. */
    val reservedByteLength: Double

    /** The value offsets buffer, or `null`. */
    val valueOffsets: JsAny?

    /** The values buffer, or `null`. */
    val values: JsAny?

    /** The validity bitmap, or `null`. */
    val nullBitmap: JsAny?

    /** The type ids buffer of a union builder, or `null`. */
    val typeIds: JsAny?

    /** Appends one value; `null` (or one of the builder's null values) appends a null. */
    fun append(value: JsAny?): Builder

    /** Whether `value` would be appended as valid rather than as a null. */
    fun isValid(value: JsAny?): Boolean

    /** Writes one value at `index`, growing the buffers if needed. */
    fun set(index: Double, value: JsAny?): Builder

    /** Marks the value at `index` valid or null. */
    fun setValid(index: Double, valid: Boolean): Builder

    /** Adds a child builder of a nested type. */
    fun addChild(child: Builder, name: String): Builder

    /** The child builder at `index`, or `null`. */
    fun getChildAt(index: Double): Builder?

    /** Flushes the accumulated values into [Data] and clears the builder. */
    fun flush(): Data

    /** Finishes the builder: no further appends are accepted. */
    fun finish(): Builder

    /** Drops every accumulated value. */
    fun clear(): Builder

    /** Upstream's `toString`. */
    override fun toString(): String
}

/** Upstream's `makeBuilder(options)`: the concrete builder for `options.type`. */
external fun makeBuilder(options: BuilderOptions): Builder

/** Upstream's `vectorFromArray(values, type?)`: builds a vector from a JS array (or typed array). */
external fun vectorFromArray(values: JsAny, type: DataType?): Vector<JsAny>

/** Upstream's `tableFromJSON(array)`: builds a table from an array of plain JS row objects. */
external fun tableFromJSON(array: Array<JsAny>): Table
