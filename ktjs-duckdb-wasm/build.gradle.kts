import org.jetbrains.kotlin.gradle.ExperimentalJsTestDsl

plugins {
    kotlin("multiplatform")
}

// Wrapper version = {upstream npm version}-kt.{revision}. See the versioning section in AGENTS.md.
@Suppress("UNCHECKED_CAST")
val npmVersions = rootProject.extra["npmVersions"] as Map<String, String>
@Suppress("UNCHECKED_CAST")
val wrapperRevisions = rootProject.extra["wrapperRevisions"] as Map<String, Int>
val upstreamVersion = npmVersions.getValue("@duckdb/duckdb-wasm")
val wrapperRevision = wrapperRevisions.getValue(project.name)
// Empty in a release build, `-SNAPSHOT` in a working tree: see the note in the root build script.
@Suppress("UNCHECKED_CAST")
val versionSuffix = rootProject.extra["versionSuffix"] as String
version = "$upstreamVersion-kt.$wrapperRevision$versionSuffix"
description = "Kotlin/JS wrapper for the @duckdb/duckdb-wasm package."

kotlin {
    js {
        useEsModules()

        compilerOptions {
            target = "es2015"
        }

        // duckdb-wasm runs in the browser and needs a DOM (workers, fetch, streams).
        browser {
            @OptIn(ExperimentalJsTestDsl::class)
            test {
                chromium()
            }
        }

        binaries.library()
    }

    compilerOptions {
        // See the DCE note in AGENTS.md: a wrapper library has no `@JsExport` roots.
        freeCompilerArgs.add("-Xir-dce=false")
    }

    sourceSets {
        jsMain.dependencies {
            implementation(npm("@duckdb/duckdb-wasm", upstreamVersion))

            // Query results are `apache-arrow` tables, so the Arrow wrapper is part of this module's
            // public API surface: a consumer compiles against it through this module.
            api(project(":ktjs-apache-arrow"))

            // `AsyncDuckDB` takes a `Worker` in its constructor and hands one back: kotlin-browser's
            // `web.workers.Worker` is the type callers use.
            api(libs.kotlin.browser)
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
