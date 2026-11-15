plugins {
    kotlin("multiplatform")
}

// Wrapper version = {upstream npm version}-kt.{revision}. See the versioning section in AGENTS.md.
@Suppress("UNCHECKED_CAST")
val npmVersions = rootProject.extra["npmVersions"] as Map<String, String>
@Suppress("UNCHECKED_CAST")
val wrapperRevisions = rootProject.extra["wrapperRevisions"] as Map<String, Int>
val upstreamVersion = npmVersions.getValue("@duckdb/node-api")
val wrapperRevision = wrapperRevisions.getValue(project.name)
// Empty in a release build, `-SNAPSHOT` in a working tree: see the note in the root build script.
@Suppress("UNCHECKED_CAST")
val versionSuffix = rootProject.extra["versionSuffix"] as String
version = "$upstreamVersion-kt.$wrapperRevision$versionSuffix"
description = "Kotlin/JS wrapper for the @duckdb/node-api package."

kotlin {
    js {
        // Node only: `@duckdb/node-api` loads a native addon through `@duckdb/node-bindings`, so the
        // wrapper cannot run in a browser. No `useEsModules()` either: the package is CommonJS and
        // Kotlin/JS's CommonJS output can `require` it directly.
        nodejs()

        compilerOptions {
            target = "es2015"
        }

        binaries.library()
    }

    compilerOptions {
        // See the DCE note in AGENTS.md: a wrapper library has no `@JsExport` roots.
        freeCompilerArgs.add("-Xir-dce=false")
    }

    sourceSets {
        jsMain.dependencies {
            implementation(npm("@duckdb/node-api", upstreamVersion))

            // Result rows, columns and values are typed by this module's own declarations; the
            // Arrow types are not part of the Node client's surface.
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
