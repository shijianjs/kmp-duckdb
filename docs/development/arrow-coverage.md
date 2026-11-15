# Arrow coverage

## 来源与范围

- 来源：`apache-arrow` 21.2.0（本地克隆 `S:\workspace\github\apache\arrow-js`，包的公开入口
  `index.ts` → `src/Arrow.node.ts` → `src/Arrow.ts`）。
- 核对日期：2026-11-15。
- 范围：`src/Arrow.ts` 顶层 `export` 的每一个条目（类型、值、模块级函数）。每一项记作一个清单条目；
  上游把若干同类类型写在同一条 `export` 里（如 `Int8 … Uint64`）时合并为一条，因为它们在 Kotlin 侧同一个
  文件、同一批测试覆盖。
- 不纳入统计：`lib/` 构建产物、内部 `fb/` 生成的 flatbuffer 代码（除 `MessageHeader` / `CompressionType`
  两个枚举）、`perf/`、`test/`。

## 统计

| 状态 | 数量 |
| --- | ---: |
| 总计 | 50 |
| 已完成 | 39 |
| 明确不做 | 1 |
| 待完成 | 10 |

## 明确不做

| 名称 | 排除理由 |
| --- | --- |
| `util` 命名空间（`bn`、`int`、`bit`、`math`、`buffer`、`vector`、`pretty`、`interval`） | 这些是 Arrow 自己用来在内部处理 BN / 位图 / 字符串格式化的辅助函数，公开导出只是为了让同包内多个模块共享；消费方既不会调用它们，也没有稳定的语义承诺。包装出来只会扩大 API 面积而不增加可用性。 |

## 待完成

### 构造器子类（30 个）

`BoolBuilder`、`NullBuilder`、`DateBuilder`/`DateDayBuilder`/`DateMillisecondBuilder`、`DecimalBuilder`、
`DictionaryBuilder`、`FixedSizeBinaryBuilder`、`FloatBuilder`/`Float16Builder`/`Float32Builder`/`Float64Builder`、
`IntBuilder` 与 8 个宽度子类、`TimeBuilder` 与 4 个子类、`TimestampBuilder` 与 4 个子类、`IntervalBuilder` 与
3 个子类、`DurationBuilder` 与 4 个子类、`Utf8Builder`、`Utf8ViewBuilder`、`LargeUtf8Builder`、`BinaryBuilder`、
`BinaryViewBuilder`、`LargeBinaryBuilder`、`ListBuilder`、`LargeListBuilder`、`FixedSizeListBuilder`、
`MapBuilder`、`StructBuilder`、`UnionBuilder`/`SparseUnionBuilder`/`DenseUnionBuilder`。

- [ ] 每个子类的公开成员按 `.d.ts` 补齐，并通过 `makeBuilder` 之外的方式构造一次验证。
- 说明：`makeBuilder`、`vectorFromArray`、`tableFromJSON` 已可用，DuckDB 场景不需要这些具体类；
  它们是「下一轮」而不是「不做」，所以计为待完成。

### 流与 IPC

- [ ] `ByteStream`、`AsyncByteStream`、`AsyncByteQueue`
- [ ] `RecordBatchReader`、`RecordBatchFileReader`、`RecordBatchStreamReader`、
      `AsyncRecordBatchFileReader`、`AsyncRecordBatchStreamReader`
- [ ] `RecordBatchWriter`、`RecordBatchFileWriter`、`RecordBatchStreamWriter`、`RecordBatchJSONWriter`
- [ ] `Message`、`MessageReader`、`AsyncMessageReader`、`JSONMessageReader`
- [ ] `compressionRegistry`（`Codec` 注册）
- [ ] `compareSchemas`、`compareFields`、`compareTypes`（`util/typecomparator`）

### 访问器

- [ ] `Visitor` 及各类型的 visit 签名

## 已完成（要点）

枚举 10 组（`Type`、`BufferType`、`DateUnit`、`TimeUnit`、`Precision`、`UnionMode`、`IntervalUnit`、
`MetadataVersion`、`MessageHeader`、`CompressionType`，数值逐条核对）；`DataType` 与全部 41 个具体类型；
`Field` / `Schema`；`Data` / `makeData`；`Vector` / `makeVector`；`MapRow` / `StructRow`；`RecordBatch`；
`Table` / `makeTable` / `tableFromArrays`；`Builder` / `makeBuilder` / `BuilderOptions`；
`vectorFromArray` / `tableFromJSON`；7 个 `isArrow*` 类型守卫；`tableToIPC` / `tableFromIPC`（含
`tableFromIPCAsync`）。

验证：`:ktjs-apache-arrow:jsBrowserTest` 10 个用例全部通过（Chromium），覆盖枚举数值、类型宽度与单位、
schema 成员、`makeData` + `Vector` 取值与切片、`tableFromArrays` 读取与 IPC 往返、类型守卫。

## 建模备注

- `Type` 等数值枚举在 Kotlin 侧是 `sealed external interface` + `Double` 工厂 + 常量对象；`Int32()` 这类类型的
  `typeId` 是共享的 `Type.Int`（上游 `Int_` 的构造行为），宽度只在 `bitWidth` 上。这条写进了 KDoc 与测试注释，
  避免后来者以为是 wrapper 的 bug。
