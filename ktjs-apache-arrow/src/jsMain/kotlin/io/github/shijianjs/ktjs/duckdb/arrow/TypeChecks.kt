@file:JsModule("apache-arrow")

package io.github.shijianjs.ktjs.duckdb.arrow

import kotlin.js.JsAny

/*
 * Upstream's `util/typecheck.ts` guards. They accept `any`, and they recognize instances created by
 * a *different* copy of apache-arrow (the check reads a well-known symbol rather than instanceof),
 * which is why a wrapper exposes them instead of implementing the checks on Kotlin side.
 */

/** Upstream's `isArrowSchema`: whether the value is an Arrow schema, from any copy of the library. */
external fun isArrowSchema(value: JsAny?): Boolean

/** Upstream's `isArrowField`: whether the value is an Arrow field, from any copy of the library. */
external fun isArrowField(value: JsAny?): Boolean

/** Upstream's `isArrowDataType`: whether the value is an Arrow data type. */
external fun isArrowDataType(value: JsAny?): Boolean

/** Upstream's `isArrowData`: whether the value is an Arrow data chunk. */
external fun isArrowData(value: JsAny?): Boolean

/** Upstream's `isArrowVector`: whether the value is an Arrow vector. */
external fun isArrowVector(value: JsAny?): Boolean

/** Upstream's `isArrowRecordBatch`: whether the value is an Arrow record batch. */
external fun isArrowRecordBatch(value: JsAny?): Boolean

/** Upstream's `isArrowTable`: whether the value is an Arrow table. */
external fun isArrowTable(value: JsAny?): Boolean
