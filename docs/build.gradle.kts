plugins {
    kotlin("multiplatform")
    id("io.github.shijianjs.ktjs.docusaurus")
}

/**
 * `docs` is this project's documentation site: the Markdown lives in `src/jsMain/resources`, the
 * configuration is Kotlin, and `./gradlew :docs:docusaurusBuild` turns both into the static site that
 * `pagesSite` copies to the root of the Pages deployment.
 *
 * The site is plain Docusaurus — no custom elements, so no browser bundle and no client module: the
 * module is also loaded by Node when the site is prerendered, and keeping it DOM-free keeps that pass
 * simple.
 */
kotlin {
    js {
        useEsModules()
        nodejs()
        binaries.library()
    }

    compilerOptions {
        // `kotlin.js.JsAny` is the type of every upstream member declared as `unknown`.
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }

    sourceSets {
        jsMain.dependencies {
            // The wrapper brings its own `@docusaurus/*` npm dependencies: the klib records the
            // versions it was built against and Kotlin/JS installs them in the npm workspace.
            implementation(
                "io.github.shijianjs:ktjs-docusaurus:${providers.gradleProperty("ktjs-docusaurus.version").get()}",
            )

            // Resolved *by name* from the generated configuration, so npm has to install it next to
            // the site: no Kotlin code imports it and the wrapper does not bring it along. The
            // version is the upstream `@docusaurus/*` lockstep anchor, also pinned in the root
            // `package.json` so Dependabot keeps it in step with the wrapper's Docusaurus version.
            implementation(npm("@docusaurus/theme-mermaid", "3.10.2"))
        }
    }
}

docusaurus {
    // Generates `sidebars.mjs` next to the site configuration from the exported `sidebars` function.
    sidebarsFunction = "sidebars"

    // Dev 默认中文启动：docusaurusStart 不带参数也能起中文站点；构建不受影响。
    // 单次运行仍可用 --locale en / -Pdocusaurus.locale=en 覆盖。
    startArguments = listOf("--locale", "zh-Hans")
}
