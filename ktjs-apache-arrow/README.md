# ktjs-apache-arrow

Kotlin/JS declarations for [`apache-arrow`](https://www.npmjs.com/package/apache-arrow) 21.2.0: data
types, schemas, vectors, tables and the Arrow IPC entry points. Pubished as
`io.github.shijianjs:ktjs-apache-arrow:21.2.0-kt.1`.

[English](README.md) | [简体中文](README.zh-CN.md)

- [Documentation](https://shijianjs.github.io/kmp-duckdb/guide/apache-arrow)
- [API reference](https://shijianjs.github.io/kmp-duckdb/api/)

```kotlin
val table = tableFromArrays(unsafeJso<IdColumn> { id = ids })
val doubled = table.getChild("id")?.get(1.0)
val restored = tableFromIPC(tableToIPC(table))
```

Covered: the numeric enums (`Type`, `BufferType`, `DateUnit`, `TimeUnit`, `Precision`, `UnionMode`,
`IntervalUnit`, `MetadataVersion`, `MessageHeader`, `CompressionType`), every `DataType` subclass,
`Field` / `Schema`, `Data` / `makeData`, `Vector` / `makeVector`, the row readers, `RecordBatch`,
`Table` / `makeTable` / `tableFromArrays`, the builder entry points (`makeBuilder`,
`vectorFromArray`, `tableFromJSON`), the `isArrow*` type guards and the IPC functions
(`tableToIPC`, `tableFromIPC`).

Not covered in this pass: the per-type builder classes, `Visitor`, the message / record-batch readers
and writers, `ByteStream`, `compressionRegistry` and the `util` helpers. The list with reasons is in
[`docs/development/arrow-coverage.md`](../docs/development/arrow-coverage.md).

The declarations follow the upstream `.d.ts` literally: a member typed as a union, a string literal
or an enum in TypeScript never degrades to `String` / `Double` / `JsAny` here.
