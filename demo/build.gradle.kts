plugins {
    kotlin("multiplatform")
}

// The brand image lives with the site's static files and is copied in here, so the demo and the
// documentation cannot drift apart. `srcDir(task)` only contributes the directory, not the
// dependency, hence the explicit wiring below.
val brandAssets = tasks.register<Copy>("brandAssets") {
    from(rootProject.file("docs/src/jsMain/resources/static/img")) {
        // Keep the same `img/` layout the site uses, so both pages reference `img/<logo>.svg`.
        into("img")
    }
    into(layout.buildDirectory.dir("generated/brand"))
}

// The WASM module and its worker script are taken from the installed npm package instead of a CDN:
// the demo page has to work offline and behind a firewalled network, and a jsDelivr URL would make
// the e2e suite depend on the network. The files land in `duckdb/`, which is the prefix the page
// uses when it instantiates the module and starts the worker.
val duckdbAssets = tasks.register<Copy>("duckdbAssets") {
    dependsOn(":kotlinNpmInstall")
    from(rootProject.layout.buildDirectory.dir("js/node_modules/@duckdb/duckdb-wasm/dist")) {
        include("duckdb-mvp.wasm", "duckdb-browser-mvp.worker.js")
        include("duckdb-eh.wasm", "duckdb-browser-eh.worker.js")
    }
    into(layout.buildDirectory.dir("generated/duckdb-assets/duckdb"))
}

kotlin {
    js {
        useEsModules()

        // The demo is a standalone page, so the JS target needs an executable webpack bundle.
        binaries.executable()
        browser()
    }

    // The JVM target carries only the Playwright end-to-end suite in `src/jvmTest`; the demo itself
    // stays a JS page.
    jvm()

    sourceSets {
        jsMain {
            resources.srcDir(brandAssets)
            // The `duckdb/` directory itself has to be inside the resources root, so this points at
            // the parent the task copies into.
            resources.srcDir(layout.buildDirectory.dir("generated/duckdb-assets"))
        }

        jsMain.dependencies {
            implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.1")
            // The published modules the demo demonstrates, one line each.
            implementation(project(":ktjs-duckdb-wasm"))
        }

        jvmTest.dependencies {
            implementation("com.microsoft.playwright:playwright:1.61.0")
            implementation("cn.hutool:hutool-all:5.8.48")
            implementation("org.junit.jupiter:junit-jupiter:6.1.3")
            implementation("org.junit-pioneer:junit-pioneer:2.3.0")
            implementation(kotlin("test-junit5"))
            runtimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
        }
    }
}

// `srcDir(task)` contributes the directory but not the task dependency; resource processing is what
// reads that directory, so the edge belongs there.
tasks.named("jsProcessResources") {
    dependsOn(brandAssets, duckdbAssets)
}

// ---------------------------------------------------------------------------------------------
// End-to-end suite
//
// The suite lives in `src/jvmTest`, so the demo stays one KMP module instead of carrying a second
// Gradle project. It uses Playwright's official JUnit 5 integration plus Hutool's SimpleServer for
// the static bundle, and reuses the Chromium that `kotlinInstallPlaywrightBrowsers` installs for
// the Kotlin/JS browser tests (hence the pinned Playwright version in the dependencies).
// `jvmTest` itself is disabled to keep `build`/`check` browser-free; this dedicated task runs the
// same test compilation on demand.
// ---------------------------------------------------------------------------------------------
val jvmE2eCompilation = kotlin.targets.getByName("jvm").compilations.getByName("test")

tasks.register<Test>("e2eTest") {
    group = "verification"
    description = "Runs the Kotlin/JVM Playwright suite against the bundled demo page."
    useJUnitPlatform()
    dependsOn("jsBrowserDistribution")
    // The Chromium is installed for the Kotlin/JS browser tests of the wrapper module, and this suite
    // reuses it. A derived project points this at its own first published module.
    dependsOn(":ktjs-duckdb-wasm:kotlinInstallPlaywrightBrowsers")

    testClassesDirs = jvmE2eCompilation.output.classesDirs
    classpath = files(jvmE2eCompilation.output.allOutputs, jvmE2eCompilation.runtimeDependencyFiles)
    systemProperty("demo.dist", layout.buildDirectory.dir("dist/js/productionExecutable").get().asFile.absolutePath)
    systemProperty("e2e.output", layout.buildDirectory.dir("e2e-results").get().asFile.absolutePath)
    systemProperty("junit.jupiter.execution.timeout.default", "60 s")

    outputs.upToDateWhen { false }
}

tasks.named<Test>("jvmTest") { enabled = false }
