@file:JsModule("@duckdb/duckdb-wasm")

package io.github.shijianjs.ktjs.duckdb.wasm

import web.workers.Worker
import kotlin.js.Promise

/** Upstream `interface DuckDBBundles`: where the WASM module and its worker live. */
external interface DuckDBBundles {
    /** The baseline bundle, usable on every platform. */
    var mvp: DuckDBBundleFiles

    /** The exception-handling bundle; requires `wasmExceptions` support. */
    var eh: DuckDBBundleFiles?

    /** The thread-enabled bundle; requires cross-origin isolation. */
    var coi: DuckDBCOIBundleFiles?
}

/** One `{ mainModule, mainWorker }` pair inside [DuckDBBundles]. */
external interface DuckDBBundleFiles {
    /** The URL of the `.wasm` module. */
    var mainModule: String

    /** The URL of the browser worker script. */
    var mainWorker: String
}

/** The `coi` entry of [DuckDBBundles]: a bundle that also needs a pthread worker. */
external interface DuckDBCOIBundleFiles : DuckDBBundleFiles {
    /** The URL of the worker script the WASM threads spawn. */
    var pthreadWorker: String
}

/** Upstream `interface DuckDBBundle`: the bundle [selectBundle] picked. */
external interface DuckDBBundle {
    /** The URL of the `.wasm` module. */
    var mainModule: String

    /** The URL of the browser worker script; `null` for a detached instance. */
    var mainWorker: String?

    /** The URL of the pthread worker, when the bundle is the `coi` one. */
    var pthreadWorker: String?
}

/** Upstream `interface PlatformFeatures`: what this platform supports, as detected by `wasm-feature-detect`. */
external interface PlatformFeatures {
    /** `BigInt64Array` exists. */
    var bigInt64Array: Boolean

    /** The page is cross-origin isolated, so `SharedArrayBuffer` is available. */
    var crossOriginIsolated: Boolean

    /** The platform supports WASM exception handling. */
    var wasmExceptions: Boolean

    /** The platform supports WASM SIMD. */
    var wasmSIMD: Boolean

    /** The platform supports the WASM bulk-memory instructions. */
    var wasmBulkMemory: Boolean

    /** The platform supports WASM threads. */
    var wasmThreads: Boolean
}

/**
 * Upstream `getJsDelivrBundles`: bundle URLs served from jsDelivr for the installed package version.
 *
 * Prefer registering the files from your own build (as the demo does) when the page has to work
 * offline or behind a firewalled network.
 */
external fun getJsDelivrBundles(): DuckDBBundles

/** Upstream `getPlatformFeatures`: detects the WASM features this platform supports. */
external fun getPlatformFeatures(): Promise<PlatformFeatures>

/** Upstream `selectBundle`: picks the fastest bundle the platform can run. */
external fun selectBundle(bundles: DuckDBBundles): Promise<DuckDBBundle>

/** Upstream `isNode`: whether this is a Node runtime rather than a browser. */
external fun isNode(): Boolean

/** Upstream `isFirefox`: whether the browser reports as Firefox. */
external fun isFirefox(): Boolean

/** Upstream `isSafari`: whether the browser reports as Safari. */
external fun isSafari(): Boolean

/** Upstream `createWorker`: fetches a worker script into a blob URL and constructs a worker from it. */
external fun createWorker(url: String): Promise<Worker>

/** Upstream `PACKAGE_NAME`: the npm package name of the loaded duckdb-wasm. */
external val PACKAGE_NAME: String

/** Upstream `PACKAGE_VERSION`: the npm package version of the loaded duckdb-wasm. */
external val PACKAGE_VERSION: String

/** Upstream `PACKAGE_VERSION_MAJOR`: the major part of [PACKAGE_VERSION]. */
external val PACKAGE_VERSION_MAJOR: String

/** Upstream `PACKAGE_VERSION_MINOR`: the minor part of [PACKAGE_VERSION]. */
external val PACKAGE_VERSION_MINOR: String

/** Upstream `PACKAGE_VERSION_PATCH`: the patch part of [PACKAGE_VERSION]. */
external val PACKAGE_VERSION_PATCH: String
