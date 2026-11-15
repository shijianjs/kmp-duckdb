# ktjs-apache-arrow

[`apache-arrow`](https://www.npmjs.com/package/apache-arrow) 21.2.0 的 Kotlin/JS 声明：数据类型、schema、
向量、表以及 Arrow IPC 入口。发布坐标为 `io.github.shijianjs:ktjs-apache-arrow:21.2.0-kt.1`。

[English](README.md) | [简体中文](README.zh-CN.md)

- [文档](https://shijianjs.github.io/kmp-duckdb/zh-Hans/guide/apache-arrow)
- [API 参考](https://shijianjs.github.io/kmp-duckdb/api/)

```kotlin
val table = tableFromArrays(unsafeJso<IdColumn> { id = ids })
val second = table.getChild("id")?.get(1.0)
val restored = tableFromIPC(tableToIPC(table))
```

已覆盖：数值枚举（`Type`、`BufferType`、`DateUnit`、`TimeUnit`、`Precision`、`UnionMode`、`IntervalUnit`、
`MetadataVersion`、`MessageHeader`、`CompressionType`）、全部 `DataType` 子类、`Field` / `Schema`、
`Data` / `makeData`、`Vector` / `makeVector`、行读取器、`RecordBatch`、`Table` / `makeTable` /
`tableFromArrays`、构造入口（`makeBuilder`、`vectorFromArray`、`tableFromJSON`）、`isArrow*` 类型守卫，
以及 IPC 函数（`tableToIPC`、`tableFromIPC`）。

本轮未覆盖：各类型的 builder 类、`Visitor`、message 与 record batch 的读写器、`ByteStream`、
`compressionRegistry` 以及 `util` 辅助函数。完整清单与理由在
[`docs/development/arrow-coverage.md`](../docs/development/arrow-coverage.md)。

声明严格照抄上游 `.d.ts`：上游写成联合、字符串字面量或枚举的成员，在这里不会退化成 `String` / `Double` /
`JsAny`。
