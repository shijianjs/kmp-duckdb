@file:JsModule("apache-arrow")

package io.github.shijianjs.ktjs.duckdb.arrow

import kotlin.js.JsAny

/**
 * Upstream `class StructRow`: one row of a struct-typed column.
 *
 * Upstream returns a proxy whose string keys are the field names (`row.name`), which Kotlin cannot
 * type statically for a runtime-known schema. Read the row through [toJSON] / [toArray], or read a
 * column through [Table.getChild] instead — the latter is the typed path.
 */
external class StructRow {
    /** The row's values keyed by field name, as a plain JS object. */
    fun toJSON(): JsAny

    /** The row's values in field order. */
    fun toArray(): Array<JsAny>

    /** Upstream's `toString`: `{ name: value, … }`. */
    override fun toString(): String
}

/**
 * Upstream `class MapRow`, one entry of a map-typed column.
 *
 * Iterating it yields `[key, value]` pairs; [toJSON] returns the whole map as a JS object.
 */
external class MapRow {
    /** The entry's key. */
    val key: JsAny?

    /** The entry's value. */
    val value: JsAny?

    /** The map as a plain JS object. */
    fun toJSON(): JsAny

    /** Upstream's `toString`. */
    override fun toString(): String
}
