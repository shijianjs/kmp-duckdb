# DuckDB Node API coverage

## 来源与范围

- 来源：`@duckdb/node-api` 2.0.0-r.1（本地克隆 `S:\workspace\github\duckdb\duckdb-node-neo`，
  包的公开入口 `api/src/index.ts` → `api/src/duckdb.ts`）。
- 核对日期：2026-11-15。
- 范围：`api/src/duckdb.ts` 里 `export * from './…'` 的每一个模块，共 45 项。
- 不纳入统计：`bindings/`（C 层绑定）、`tools/`、构建配置。

## 统计

| 状态 | 数量 |
| --- | ---: |
| 总计 | 45 |
| 已完成 | 12 |
| 明确不做 | 5 |
| 待完成 | 28 |

## 已完成

`DuckDBInstance`、`DuckDBConnection`、`DuckDBResult`、`DuckDBMaterializedResult`、`DuckDBResultReader`、
`DuckDBPreparedStatement`、`DuckDBType`、`DuckDBTypeId`、`DuckDBLogicalType`、`values`（值类型别名）、
`sql`（SQL 语句类型别名）、`enums`（读取与语句用的枚举字面量）。

验证：`:ktjs-duckdb-node-neo:jsNodeTest` 3 个用例在真实 Node 上通过——内存库读取 `range(0, 5)` 的行与列名、
`SELECT ?::INTEGER + 1` 绑定参数、同一预处理语句两次不同绑定。

## 明确不做

| 名称 | 排除理由 |
| --- | --- |
| `configurationOptionDescriptions` | 上游导出的是「配置项名称 → 说明文字」的静态字典，给 CLI 打帮助信息用；Kotlin 使用方要写配置项时直接写字符串常量即可，包一层不增加类型安全。 |
| `DuckDBValueConverter` / `DuckDBValueConverters` / `JSDuckDBValueConverter` / `JsonDuckDBValueConverter` / `ToDuckDBValueConverter(s)` / `JSToDuckDBValueConverter` | 这些是自定义类型转换器的注册机制，用于把自定义 JS 类映射成 DuckDB 值。本阶段的值取舍是「原生 JS 值 + `JsAny`」，引入转换器需要同时建模它的泛型映射表（`JSTypeForTypeId` 一族），成本远高于收益。 |
| `JSInputTypeForTypeId` / `JSTypeForTypeId` / `JsonTypeForTypeId` | 上游用它们做编译期类型映射（「某 TypeId 对应哪种 JS 输入类型」）。Kotlin 侧无法表达按类型 id 的条件类型，`JsAny` + KDoc 说明才是诚实的表达；单独包装这些别名没有调用点。 |
| `readValue` | 单值读取辅助，已被 `DuckDBResultReader.value` 覆盖。 |
| `version` | 上游导出的是版本字符串常量，Kotlin 侧一行 `const val` 就够；等到有人真的需要按版本分支时再补。 |

## 待完成

### 批量写入与数据结构

- [ ] `DuckDBAppender`（30+ 个 `append*` 方法：`appendBoolean`、`appendTinyInt` … `appendDefault`、`endRow`）
- [ ] `DuckDBDataChunk`、`DuckDBDataChunkWriter`（按 chunk 读写）
- [ ] `DuckDBVector`（原生向量访问）

### 待定结果与集合

- [ ] `DuckDBPendingResult`（长查询的增量读取）
- [ ] `DuckDBExtractedStatements`、`DuckDBPreparedStatementCollection`（语句缓存）
- [ ] `DuckDBInstanceCache`（进程级实例缓存，`DuckDBInstance.fromCache` 已声明静态侧，缓存对象本身未建模）
- [ ] `DuckDBClientContext`（会话上下文）

### 自定义函数

- [ ] `DuckDBScalarFunction`、`DuckDBScalarFunctionInfo`、`DuckDBScalarFunctionBindInfo`、
      `DuckDBScalarFunctionInitInfo`
- [ ] `DuckDBTableFunction`、`DuckDBTableFunctionInfo`、`DuckDBTableFunctionBindInfo`、
      `DuckDBTableFunctionInitInfo`

### 值包装类型

- [ ] `DuckDBValue` 的完整联合（`DuckDBListValue`、`DuckDBStructValue`、`DuckDBMapValue`、`DuckDBArrayValue`、
      `DuckDBDecimalValue`、`DuckDBBlobValue`、`DuckDBDateValue`、`DuckDBTimeValue`、`DuckDBTimestampValue`、
      `DuckDBIntervalValue`、`DuckDBHugeIntValue`、`DuckDBUUIDValue`、`DuckDBBitValue` 等）
- 说明：读取侧统一用原生 JS 值 + `JsAny`（`getRows` / `getRowObjects`），写入侧用 `bind(value: JsAny)`。
  把这些包装类补全意味着同时给每个类型一套 `get…()` 访问器，属下一轮工作。

`DuckDBTypeId` 的派生值类型（每种类型一个）本轮只声明了 id 本身与工厂，具体类型参数化留待值包装类型一起做。

## 建模备注

- `@duckdb/node-api` 是 CommonJS 包，上游导出对象在运行时组装，Node 的 ESM 静态分析看不到具名导出。
  封装导入 default 导出（`@JsName("default") external val duckdbModule`）并声明其形状，再用
  `object DuckDBNodeApi` 转发；类值放 `external interface`（Kotlin 的 `external class` 不能带 companion）。
- `DuckDBInstance.create(null)` 在上游会因参数校验抛 `TypeError: A string was expected`；内存库要写
  `":memory:"`。这条写进了 KDoc 与测试。
