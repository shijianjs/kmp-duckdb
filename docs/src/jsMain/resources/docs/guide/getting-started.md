---
id: getting-started
title: Getting started
sidebar_position: 1
---

The modules are published to Maven Central and are plain Kotlin/JS libraries: they declare the
upstream npm package as a dependency, so Kotlin/JS installs it for you.

## Add the dependency

```kotlin title="build.gradle.kts"
plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        browser()
    }

    sourceSets {
        jsMain.dependencies {
            // Arrow is the shared vocabulary of the other two modules.
            implementation("io.github.shijianjs:ktjs-apache-arrow:21.2.0-kt.1")
            // Either the browser build …
            implementation("io.github.shijianjs:ktjs-duckdb-wasm:1.32.0-kt.1")
            // … or the native Node client; they do not conflict, but a page has no use for the node one.
            implementation("io.github.shijianjs:ktjs-duckdb-node-neo:2.0.0-r.1-kt.1")
        }
    }
}
```

`mavenCentral()` in `repositories { }` is enough; nothing here is hosted elsewhere. The npm side
(`apache-arrow`, `@duckdb/duckdb-wasm`, `@duckdb/node-api`) is pulled in by the klib metadata, and a
corporate npm mirror can be configured in the usual way (`npm_config_registry` or `.npmrc`).

## A first browser query

Every method returns a JS `Promise`, so the chain below is the whole flow: instantiate the module,
open the database, connect, query, and read the Arrow table that comes back.

```kotlin
import io.github.shijianjs.ktjs.duckdb.wasm.AsyncDuckDB
import io.github.shijianjs.ktjs.duckdb.wasm.DuckDBConfig
import io.github.shijianjs.ktjs.duckdb.wasm.LogLevels
import io.github.shijianjs.ktjs.duckdb.wasm.consoleLogger
import js.objects.unsafeJso
import web.workers.Worker

fun runQuery() {
    // The worker script and the .wasm module are the two files the demo serves from `duckdb/`.
    // Resolve them against the page: inside the worker a bare relative URL would resolve against the
    // worker's own directory instead.
    val base = document.baseURI
    val worker = Worker(base + "duckdb/duckdb-browser-mvp.worker.js")
    val db = AsyncDuckDB(consoleLogger(LogLevels.WARNING), worker)

    db.instantiate(base + "duckdb/duckdb-mvp.wasm")
        .then { db.open(unsafeJso<DuckDBConfig> { path = ":memory:" }) }
        .then { db.connect() }
        .then { connection ->
            connection.query("SELECT 42 AS answer").then { table ->
                // `table` is an Arrow Table: read it through the Arrow module.
                val answer = table.getChildAt(0.0)?.get(0.0)
                println("answer = $answer")
                connection.close()
            }
        }
}
```

`query()` resolves to an Arrow `Table` whose columns are typed vectors: `getChildAt` gives one column,
`get(row)` gives the value in that column.

## A first Node query

```kotlin
import io.github.shijianjs.ktjs.duckdb.node.DuckDBNodeApi

fun count() {
    DuckDBNodeApi.DuckDBInstance.create(null) // `:memory:`
        .then { instance ->
            instance.connect().then { connection ->
                connection.runAndReadAll("SELECT count(*) FROM range(0, 1000)").then { reader ->
                    val rows = reader.value(0.0, 0.0).unsafeCast<Int>()
                    println("rows = $rows")
                    reader.closeSync()
                    connection.closeSync()
                    instance.closeSync()
                }
            }
        }
}
```

Node's client returns values as native `BigInt` / `number` / `string`, not as Arrow, which is why the
node module does not depend on the Arrow one for results — only on the shared type vocabulary when it
needs it.

## Serving the WASM files

`@duckdb/duckdb-wasm` ships `duckdb-mvp.wasm` (or `duckdb-eh.wasm`) and the matching worker script in
its `dist/`. Copy them into the page's static files and point `instantiate` and `new Worker` at them;
`getJsDelivrBundles()` is the alternative when a CDN is acceptable. The demo module in the repository
does the copy in Gradle, which is the pattern to copy:

```kotlin title="build.gradle.kts"
val duckdbAssets = tasks.register<Copy>("duckdbAssets") {
    dependsOn(":kotlinNpmInstall")
    from(rootProject.layout.buildDirectory.dir("js/node_modules/@duckdb/duckdb-wasm/dist")) {
        include("duckdb-mvp.wasm", "duckdb-browser-mvp.worker.js")
        include("duckdb-eh.wasm", "duckdb-browser-eh.worker.js")
    }
    // `duckdb/` is part of the destination, because the page references `duckdb/<file>`.
    into(layout.buildDirectory.dir("generated/duckdb-assets/duckdb"))
}

kotlin.js.sourceSets.jsMain.resources.srcDir(layout.buildDirectory.dir("generated/duckdb-assets"))
tasks.named("jsProcessResources") { dependsOn(duckdbAssets) }
```

For the threaded (`coi`) bundle the page must also be cross-origin isolated (COOP/COEP headers); the
`mvp` bundle works everywhere.

## Conventions the declarations follow

- A value type upstream models as a numeric enum is a `sealed external interface` over `Double` plus a
  constants object (`Types.Int32`, `LogLevels.WARNING`), never a Kotlin `enum class`.
- A string literal union is a `sealed external interface` plus an `Xxxs` constants object
  (`IPCTypes.file`, `JSONTableShapes.ROW_ARRAY`).
- Option objects are built with `js.objects.unsafeJso { … }`, the same way kotlin-wrappers builds DOM
  options.
- A member upstream types as `unknown` stays `JsAny` and says so in its KDoc; nothing else is widened.
