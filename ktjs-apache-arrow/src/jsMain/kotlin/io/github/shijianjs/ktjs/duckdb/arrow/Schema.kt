@file:JsModule("apache-arrow")

package io.github.shijianjs.ktjs.duckdb.arrow

import js.collections.JsMap

/** Upstream `class Field`: the name, type and optionality of one column. */
external class Field {
    /** The upstream [Type] identifier of [type]. */
    val typeId: Type

    /** The column name. */
    val name: String

    /** The column type. */
    val type: DataType

    /** Whether the column may contain nulls. */
    val nullable: Boolean

    /** Field-level key/value metadata; empty when the upstream did not carry any. */
    val metadata: JsMap<String, String>

    /** Upstream's `toString`: `name: type`. */
    override fun toString(): String

    /** @param name the column name @param type the column type @param nullable whether the column may contain nulls @param metadata field-level metadata */
    constructor(name: String, type: DataType, nullable: Boolean, metadata: JsMap<String, String>)

    /** @param name the column name @param type the column type; the column is non-nullable */
    constructor(name: String, type: DataType)

    /** @param name the column name @param type the column type @param nullable whether the column may contain nulls */
    constructor(name: String, type: DataType, nullable: Boolean)

    /** Upstream `field.clone(name?, type?, nullable?, metadata?)`. */
    fun clone(name: String, type: DataType, nullable: Boolean, metadata: JsMap<String, String>): Field
}

/**
 * Upstream `class Schema`: an ordered collection of [Field]s, plus schema-level metadata.
 */
external class Schema {
    /** The columns of this schema. */
    val fields: Array<Field>

    /** The column names, in field order. */
    val names: Array<String>

    /** Schema-level key/value metadata; empty when the upstream did not carry any. */
    val metadata: JsMap<String, String>

    /** Dictionaries used by dictionary-encoded columns, keyed by dictionary id. */
    val dictionaries: JsMap<Double, DataType>

    /** The Arrow IPC metadata version this schema was read at. */
    val metadataVersion: MetadataVersion

    /** Upstream's `toString`: a readable `Schema<{ 0: a: Int32, … }>` expression. */
    override fun toString(): String

    /** @param fields the columns @param metadata schema-level metadata */
    constructor(fields: Array<Field>, metadata: JsMap<String, String>)

    /** @param fields the columns; metadata is empty */
    constructor(fields: Array<Field>)

    /** Upstream's `select`: a schema with only the named fields, in the order given. */
    fun select(fieldNames: Array<String>): Schema

    /** Upstream's `selectAt`: a schema with only the fields at the given indices. */
    fun selectAt(fieldIndices: Array<Double>): Schema

    /** Upstream's `assign`: `this` with `other`'s fields appended (same-name fields are replaced). */
    fun assign(other: Schema): Schema
}
