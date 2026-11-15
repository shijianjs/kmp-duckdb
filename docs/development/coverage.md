# Coverage

三个 wrapper 模块的 API 覆盖、演示页与文档站的状态，以及验证结果。

## 来源与范围

- Arrow：`apache-arrow` 21.2.0，清单见 [`arrow-coverage.md`](arrow-coverage.md)（50 项）。
- DuckDB WASM：`@duckdb/duckdb-wasm` 1.32.0，清单见 [`duckdb-wasm-coverage.md`](duckdb-wasm-coverage.md)（7 项）。
- DuckDB Node：`@duckdb/node-api` 2.0.0-r.1，清单见 [`node-api-coverage.md`](node-api-coverage.md)（45 项）。
- 项目级事项（演示页、文档、CI、发布、Pages）：见本文件的「项目事项」一节（12 项）。
- 核对日期：2026-11-15。

## 统计

| 状态 | 数量 |
| --- | ---: |
| 总计 | 114 |
| 已完成 | 67 |
| 明确不做 | 7 |
| 待完成 | 40 |

分模块：

| 范围 | 总计 | 已完成 | 明确不做 | 待完成 |
| --- | ---: | ---: | ---: | ---: |
| Arrow | 50 | 39 | 1 | 10 |
| DuckDB WASM | 7 | 6 | 1 | 0 |
| DuckDB Node | 45 | 12 | 5 | 28 |
| 项目事项 | 12 | 10 | 0 | 2 |

WASM 的 7 项按「上游 `packages/duckdb-wasm/src/index.ts` 导出的子模块」计数：`bindings`、`log`、`parallel`、
`status`、`platform`、`version`、`worker`。其中 `parallel` 排除（只导出内部线程池配置常量，语义已由
`DuckDBConfig.maximumThreads` 覆盖）；其余 6 项已声明并通过 `compileKotlinJs`。

**该模块目前没有运行时测试**：`bindings` 的绝大多数成员是浏览器侧的 Promise API，写测试需要引入 wasm
二进制与 worker，成本与收益不成比例。实际可用性由 demo 的 e2e 套件覆盖——它真的启动 worker、加载
`duckdb-mvp.wasm`、执行 SQL 并读回 Arrow 表。这一点写在 TODO 的注意事项里，计入已完成但验证方式不同。

## 项目事项

已完成：

1. 从模板派生仓库（`settings.gradle.kts`、根 `build.gradle.kts`、`package.json`、`wrapper-revisions.properties`、
   Dependabot、Gradle wrapper）。
2. 三个 wrapper 模块的声明与 KDoc，全部通过 `compileKotlinJs`。
3. Arrow 浏览器测试（10 个用例）。
4. Node 测试（3 个用例，真实 Node 运行时）。
5. demo 页面（中英文就地切换、DuckDB WASM 控制台、Arrow 往返）。
6. demo e2e 套件（5 个用例，Playwright + 打包产物）。
7. 文档站（中英双语，`docusaurusBuild` 两个 locale 均通过）。
8. README（根与三个模块，各中英两份）、`AGENTS.md`、`TODO.md`、`docs/development/*`。
9. `pagesSite`：根目录文档、`/demo/`、`/api/`（Dokka 聚合）三处产物齐全，本地与 Actions 均通过。
10. GitHub 仓库、Pages（`build_type=workflow`）与首次 CI 运行（Ubuntu + JDK 21 下 `build`、`pagesSite`、
    `:demo:e2eTest` 全绿）。

待完成：

- [ ] 首次发布（`release.yml` → hub），本阶段不触发：仓库刚建，等 API 稳定后再发。
- [ ] `ktjs-duckdb-wasm` 的独立运行时测试（见上文说明，优先级低于其余项）。

## 验证记录

| 命令 | 结果 |
| --- | --- |
| `:ktjs-apache-arrow:jsBrowserTest` | 10 passed（Chromium） |
| `:ktjs-duckdb-node-neo:jsNodeTest` | 3 passed（Node） |
| `:demo:e2eTest` | 5 passed（Playwright / Chromium，打包产物） |
| `:docs:docusaurusBuild` | en + zh-Hans 均 SUCCESS |
| `pagesSite` | 本地 SUCCESS；`build/pages` 下 `index.html`、`demo/`（含 `duckdb/*.wasm`）、`api/` 齐全 |
| `ci.yml`（GitHub Actions，Ubuntu + JDK 21） | success：`build`、`pagesSite`、`:demo:e2eTest` |
| `pages.yml`（GitHub Actions） | success：已发布到 <https://shijianjs.github.io/kmp-duckdb/> |
| `compileKotlinJs`（三模块 + demo） | OK |

## 统计口径

每个清单条目只属于一个状态，`总数 = 已完成 + 明确不做 + 待完成`。上游「把一整类类型写在同一条 export 里」
时合并为一条（如 Arrow 的 `Int8 … Uint64`）；某一类里的具体子类若需要逐个建模，则在专项文档里拆成子项。
排除项必须给出具体理由，不能用「暂时不做」代替。
