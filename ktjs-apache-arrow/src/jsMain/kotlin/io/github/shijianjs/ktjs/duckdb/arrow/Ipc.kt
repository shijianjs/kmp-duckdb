@file:JsModule("apache-arrow")

package io.github.shijianjs.ktjs.duckdb.arrow

import kotlin.js.JsAny
import kotlin.js.JsName
import kotlin.js.Promise

/** Upstream's `'file' | 'stream'`: which Arrow IPC container [tableToIPC] writes. */
sealed external interface IPCWriteType

/**
 * Upstream's `tableToIPC`: serializes a table to Arrow IPC bytes.
 *
 * The stream container is upstream's default, so the two-argument overload states the container
 * explicitly rather than leaving it out.
 */
external fun tableToIPC(table: Table): ArrowBytes

/** Upstream's `tableToIPC(table, type, compressionType?)`. */
external fun tableToIPC(table: Table, type: IPCWriteType, compressionType: CompressionType?): ArrowBytes

/**
 * Upstream's `tableFromIPC`: reads a table from Arrow IPC bytes.
 *
 * Upstream also accepts a `Response`, a `Blob`, a Node readable stream and promises thereof; those
 * arrive as plain JS values and are declared as the promise-returning [tableFromIPCAsync] instead.
 */
external fun tableFromIPC(source: ArrowBytes): Table

/** Upstream's `tableFromIPC(JSON)`: reads a table from Arrow's JSON representation. */
external fun tableFromIPC(source: String): Table

/**
 * Upstream's `tableFromIPC` for the asynchronous sources (`Response`, `Blob`, readable streams,
 * promises of bytes).
 */
@JsName("tableFromIPC")
external fun tableFromIPCAsync(source: JsAny): Promise<Table>
