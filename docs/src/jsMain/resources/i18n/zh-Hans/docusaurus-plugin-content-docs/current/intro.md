---
id: intro
title: kmp-duckdb
sidebar_position: 1
---

# kmp-duckdb

`kmp-duckdb` 用 Kotlin/JS 的、受编译器检查的声明封装了三个上游 npm 包，让 Kotlin 项目可以直接使用
DuckDB 并把查询结果当 Apache Arrow 表读取，而不必退回到手写 JavaScript 互操作。

| 模块 | 对应上游 npm 包 | 覆盖内容 |
| --- | --- | --- |
| `ktjs-apache-arrow` | [`apache-arrow`](https://www.npmjs.com/package/apache-arrow) | 数据类型、`Data`、`Vector`、`RecordBatch`、`Table`、schema、builder 以及 IPC 序列化函数 |
| `ktjs-duckdb-wasm` | [`@duckdb/duckdb-wasm`](https://www.npmjs.com/package/@duckdb/duckdb-wasm) | `AsyncDuckDB`、连接、预编译语句、日志、配置、注册文件与 insert 选项 |
| `ktjs-duckdb-node-neo` | [`@duckdb/node-api`](https://www.npmjs.com/package/@duckdb/node-api) | `DuckDBInstance`、`DuckDBConnection`、预编译语句与结果读取器 |

三个模块遵循同一条规则：上游写了类型，Kotlin 侧就是编译器会检查的类型。`Type` 枚举成员不是 `Double`，
`'file' | 'stream'` 参数不是 `String`，列向量按值类型而不是按 `JsAny` 建模。

## 为什么分成三个模块

三个上游包运行环境不同、发布节奏也不同：

- `apache-arrow` 是纯 TypeScript，浏览器、worker 和 Node 里都能跑；
- `@duckdb/duckdb-wasm` 需要浏览器 worker 与 `.wasm` 二进制，只在页面或 worker 中有意义；
- `@duckdb/node-api` 加载 Node 原生插件，只在服务端运行。

拆开之后，浏览器页面不会拉进原生插件及其类型声明，Node 服务也不会拉进它无法实例化的 `.wasm`。
`ktjs-duckdb-wasm` 与 `ktjs-duckdb-node-neo` 都依赖 `ktjs-apache-arrow`，因为两者都把结果以 Arrow 表返回
——arrow 模块是另外两个模块的共同词汇表。

## 接下来

- **[快速开始](../guide/getting-started)** —— 把模块加进项目，跑第一条查询。
- **[Apache Arrow](../guide/apache-arrow)** —— 表怎么构造、读取与序列化。
- **[DuckDB WASM](../guide/duckdb-wasm)** —— 在浏览器页面里实例化数据库。
- **[DuckDB Node](../guide/duckdb-node)** —— Node 原生客户端。
- **[在线演示](https://shijianjs.github.io/kmp-duckdb/demo/)** —— 同样的代码在页面里运行。
- **[API 参考](https://shijianjs.github.io/kmp-duckdb/api/)** —— Dokka 生成的页面。

## 版本

模块版本由被封装的上游版本派生：`ktjs-apache-arrow:21.2.0-kt.1` 封装的是 `apache-arrow@21.2.0`，
`-kt.N` 表示同一上游版本下 wrapper 的修订次数。每个模块的修订号记录在仓库的
`wrapper-revisions.properties` 中。
