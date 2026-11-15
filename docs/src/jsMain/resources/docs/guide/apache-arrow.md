---
id: apache-arrow
title: Apache Arrow
sidebar_position: 2
---

`ktjs-apache-arrow` declares the public surface of `apache-arrow@21.2.0`. Arrow is the data format
DuckDB hands its results back in, so this is the module every result-reading path goes through.

## Building a table

```kotlin
import io.github.shijianjs.ktjs.duckdb.arrow.tableFromArrays
import js.buffer.ArrayBufferLike
import js.objects.unsafeJso
import js.typedarrays.Int32Array

external interface IdColumn {
    var id: Int32Array<ArrayBufferLike>
}

val ids = Int32Array<ArrayBufferLike>(3)
ids[0] = 10
ids[1] = 20
ids[2] = 30

val table = tableFromArrays(unsafeJso<IdColumn> { id = ids })
```

`tableFromArrays` takes a JS object of columns; each column is a typed array or a plain JS array.
Upstream's parameter type is `Record<string, T[] | TypedArray>`, and the wrapper keeps it as a typed
`JsAny` argument plus a caller-declared interface, so the column names are checked.

## Reading a table

```kotlin
val fields = table.schema.fields          // name, type, nullable, metadata
val column = table.getChildAt(0.0)        // Vector<JsAny>?, one typed column
val value = column?.get(1.0)              // 20.0
```

Rows can also be read as structs: `table.get(0.0)` gives a `StructRow`, whose fields are accessed by
name.

## Serializing

```kotlin
import io.github.shijianjs.ktjs.duckdb.arrow.IPCTypes
import io.github.shijianjs.ktjs.duckdb.arrow.tableFromIPC
import io.github.shijianjs.ktjs.duckdb.arrow.tableToIPC

val bytes = tableToIPC(table)                     // upstream's default: a stream container
val fileBytes = tableToIPC(table, IPCTypes.file, null)  // a file container, no compression
val restored = tableFromIPC(bytes)
```

`tableFromIPC` has three Kotlin entry points: bytes (`ArrowBytes`), Arrow's JSON representation
(`String`), and `tableFromIPCAsync` (upstream's own name, via `@JsName`) for a `Response`, a blob, a
readable stream or a promise of any of those.

## Types, not numbers

The Arrow enums are numeric TypeScript enums, and the wrapper models each as a value type plus a
constants object, so a member that expects a `Type` cannot be handed an arbitrary number:

```kotlin
Int32().typeId                   // Types.Int32, not -4.0
Utf8().isUtf8                    // extension property mirroring upstream's isUtf8()
Timestamp(TimeUnits.MILLISECOND) // the unit is checked, not passed as a number
```

The same holds for the IPC container (`IPCWriteType` with `IPCTypes.file` / `IPCTypes.stream`) and for
the buffer slots of `Data` (`BufferType`).

## What is not modelled

The wrapper covers what a DuckDB consumer needs: data types, `Field`/`Schema`, `Data`, `Vector`,
`RecordBatch`, `Table`, the table-level builders, the type guards and the IPC functions. The
remaining upstream surface — the per-type builders (`IntBuilder`, …), visitors, the message and
record-batch readers/writers, `compressionRegistry` and the `util` namespace — is tracked in
`docs/development/arrow-coverage.md`, so the gap is visible instead of implied.
