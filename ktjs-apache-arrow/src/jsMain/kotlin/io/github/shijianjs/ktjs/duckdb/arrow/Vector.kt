@file:JsModule("apache-arrow")

package io.github.shijianjs.ktjs.duckdb.arrow

import kotlin.js.JsAny

/**
 * Upstream `class Vector`, a column of one type, backed by one or more [Data] chunks.
 *
 * Upstream is generic over the *data type* (`Vector<Int32>`) and derives the value type from it.
 * Kotlin has no way to express that mapping, so this declaration takes the **value type** instead:
 * a `Vector<Double>` is an `Int32`/`Float64` column, a `Vector<String>` a `Utf8` column, a
 * `Vector<StructRow>` a struct column. The type argument is checked by the Kotlin compiler only —
 * at runtime it is the same JS object.
 */
external open class Vector<T> {
    /** The type of the values in this vector. */
    val type: DataType

    /** Whether any value may be null. */
    val nullable: Boolean

    /** The number of null values in this vector. */
    val nullCount: Double

    /** The number of values in this vector. */
    val length: Double

    /** The number of bytes between two values. */
    val stride: Double

    /** The number of child vectors of a nested type. */
    val numChildren: Double

    /** The underlying [Data] chunks. */
    val data: Array<Data>

    /** The byte length of this vector. */
    val byteLength: Double

    /** Upstream's `VectorName`, e.g. `Int32Vector`. */
    val VectorName: String

    /** Whether the value at `index` is not null. */
    fun isValid(index: Double): Boolean

    /** The value at `index`, or `null` when the slot is null. */
    fun get(index: Double): T?

    /** Like [get], but resolves chunk offsets; with Arrow tables the two are equivalent. */
    fun at(index: Double): T?

    /** Upstream's `set`: writes a value into the vector's own buffers (in place). */
    fun set(index: Double, value: T?)

    /** The index of the first occurrence of `element` at or after `offset`, or `-1`. */
    fun indexOf(element: T, offset: Double): Double

    /** Whether `element` occurs at or after `offset`. */
    fun includes(element: T, offset: Double): Boolean

    /** Upstream's `toArray`: the values as a JS array of the vector's physical layout. */
    fun toArray(): Array<T?>

    /** Upstream's `toJSON`: the values as a (null-containing) array. */
    fun toJSON(): Array<T?>

    /** A new vector holding this vector's values followed by the others'. */
    fun concat(vararg others: Vector<T>): Vector<T>

    /** A view over the values in `[begin, end)`. */
    fun slice(begin: Double?, end: Double?): Vector<T>

    /** The child vector of a nested type, by field name. */
    fun getChild(name: String): Vector<JsAny>?

    /** The child vector of a nested type, by field index; `null` when there is no such child. */
    fun getChildAt(index: Double): Vector<JsAny>?

    /** Whether upstream cached lookups for this vector. */
    val isMemoized: Boolean

    /** A copy of this vector that caches the values it has already read. */
    fun memoize(): MemoizedVector<T>

    /** The same values without the upstream memoization wrapper. */
    fun unmemoize(): Vector<T>

    /** Upstream's `toString`. */
    override fun toString(): String

    /** @param input the data chunks this vector is made of */
    constructor(input: Array<Data>)
}

/**
 * Upstream `class MemoizedVector`: a [Vector] that caches the values read so far.
 *
 * Returned by [Vector.memoize]; it behaves as a vector and exposes the wrapped one as [vector].
 */
external class MemoizedVector<T>(vector: Vector<T>) : Vector<T> {
    /** The vector this memoized view wraps. */
    val vector: Vector<T>
}

/** Upstream's `makeVector(data)`: builds a vector from the shape of whatever it is given. */
external fun makeVector(data: JsAny): Vector<JsAny>
