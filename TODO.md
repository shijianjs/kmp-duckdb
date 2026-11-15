# TODO

## kmp-duckdb：Kotlin/JS wrapper + 演示页 + 文档站

为 DuckDB 与 Apache Arrow 的三个 npm 包提供 Kotlin/JS wrapper，并配上中英双语的演示页与文档站。
本阶段只做 JS 目标。

- 总计：114
- 已完成：67
- 明确不做：7
- 待完成：40

详细清单：[coverage.md](docs/development/coverage.md)
分模块清单：[arrow-coverage.md](docs/development/arrow-coverage.md)、
[duckdb-wasm-coverage.md](docs/development/duckdb-wasm-coverage.md)、
[node-api-coverage.md](docs/development/node-api-coverage.md)
技术取向：[technical-decisions.md](docs/development/technical-decisions.md)

### 当前待办

- [ ] 首次发布：修订号已就位，等 API 稳定后按 `release.yml` 走 hub 注册。
- [ ] `ktjs-duckdb-wasm` 的独立运行时测试（`selectBundle` 分支、OPFS、`copyFileTo*`）。
- [ ] Arrow 待完成的 10 项（构造器子类、IPC reader/writer、`Visitor`、`compressionRegistry` 等），
      见 [arrow-coverage.md](docs/development/arrow-coverage.md#待完成)。
- [ ] Node 待完成的 28 项（`DuckDBAppender`、`DuckDBValue` 包装类型、自定义函数 API 等），
      见 [node-api-coverage.md](docs/development/node-api-coverage.md#待完成)。

### 已验证

- 本地：`:ktjs-apache-arrow:jsBrowserTest` 10 passed、`:ktjs-duckdb-node-neo:jsNodeTest` 3 passed、
  `:demo:e2eTest` 5 passed、`:docs:docusaurusBuild` 两个 locale 通过、`pagesSite` 通过。
- GitHub Actions：`ci.yml` 全绿；`pages.yml` 已发布 <https://shijianjs.github.io/kmp-duckdb/>（文档在根、
  demo 在 `/demo/`、API 在 `/api/`，中文在 `/zh-Hans/`）。

### 注意事项

- 三个模块的版本由「上游 npm 版本 + 每模块修订号」推导，只发布 `-kt.N`；改 `package.json` 或
  `wrapper-revisions.properties` 后必须刷新 `kotlin-js-store/package-lock.json`（官方源下跑
  `kotlinUpgradePackageLock`），否则 CI 在 `:kotlinStorePackageLock` 失败。
- `ktjs-duckdb-wasm` 没有独立 `jsTest`，运行时行为由 demo 的 e2e 覆盖（真的加载 wasm、起 worker、跑 SQL）。
  若要补测试，优先覆盖 `selectBundle` 分支、OPFS 与 `copyFileTo*`。
- 排除项都有具体理由（Arrow 的 `util` 命名空间、wasm 的 `parallel`、node 的转换器与版本常量等），不要再
  把它们重新讨论成「待完成」；确实要纳入时先改清单再动手。
- demo 与文档站的所有可见文案都必须中英成对；文档站新增页面要同时在 `Config.kt` 的 `sidebars()` 里登记。
