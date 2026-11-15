import org.jetbrains.kotlin.gradle.ExperimentalJsTestDsl

plugins {
    kotlin("multiplatform")
}

// Wrapper version = {upstream npm version}-kt.{revision}. See the versioning section in AGENTS.md.
@Suppress("UNCHECKED_CAST")
val npmVersions = rootProject.extra["npmVersions"] as Map<String, String>
@Suppress("UNCHECKED_CAST")
val wrapperRevisions = rootProject.extra["wrapperRevisions"] as Map<String, Int>
val upstreamVersion = npmVersions.getValue("apache-arrow")
val wrapperRevision = wrapperRevisions.getValue(project.name)
// Empty in a release build, `-SNAPSHOT` in a working tree: see the note in the root build script.
@Suppress("UNCHECKED_CAST")
val versionSuffix = rootProject.extra["versionSuffix"] as String
version = "$upstreamVersion-kt.$wrapperRevision$versionSuffix"
description = "Kotlin/JS wrapper for the apache-arrow package."

kotlin {
    js {
        useEsModules()

        compilerOptions {
            // ES2015 is the output both consumers and the browser test bundle agree on.
            target = "es2015"
        }

        // Arrow is a data library with no DOM dependency, but the wrapper's 64-bit integer and
        // BigInt interop are exercised in a real JS engine; Chromium is the runtime the wrapper is
        // used in, and it is the same browser the demo targets.
        browser {
            @OptIn(ExperimentalJsTestDsl::class)
            test {
                chromium()
            }
        }

        binaries.library()
    }

    compilerOptions {
        // Kotlin/JS dead-code-eliminates a library's JS output down to the `@JsExport` roots, and a
        // wrapper module has none: the whole module would compile to an empty file, so any consumer
        // that imports it at runtime gets `undefined`. Keep the public API; a consumer's bundler
        // still shakes whatever it does not use. See the DCE note in AGENTS.md.
        freeCompilerArgs.add("-Xir-dce=false")
    }

    sourceSets {
        jsMain.dependencies {
            implementation(npm("apache-arrow", upstreamVersion))

            // `js.objects.unsafeJso` builds the props objects Arrow's factories take
            // (`DataProps`, `BuilderOptions`), and `js.collections.JsMap` is the type of the
            // metadata maps the library hands back. Both live in kotlin-wrappers' `kotlin-js`.
            api("org.jetbrains.kotlin-wrappers:kotlin-js:2026.11.1")
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
