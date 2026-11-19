package demo.duckdb

import io.github.shijianjs.ktjs.duckdb.wasm.AsyncDuckDB
import io.github.shijianjs.ktjs.duckdb.wasm.DuckDBBundleFiles
import io.github.shijianjs.ktjs.duckdb.wasm.DuckDBBundles
import io.github.shijianjs.ktjs.duckdb.wasm.DuckDBConfig
import io.github.shijianjs.ktjs.duckdb.wasm.LogLevels
import io.github.shijianjs.ktjs.duckdb.wasm.consoleLogger
import io.github.shijianjs.ktjs.duckdb.wasm.selectBundle
import js.objects.unsafeJso
import kotlin.js.Promise
import web.dom.document
import web.workers.Worker

/** The sample query: it joins the CSV registered at boot with a generated series. */
const val SAMPLE_SQL: String = """-- A registered CSV joined with a generated series
SELECT
    p.name,
    p.city,
    p.age,
    r AS n,
    r * r AS squared
FROM 'people.csv' p, range(0, 4) t(r)
ORDER BY p.age DESC, n"""

/** The CSV registered as `people.csv` before the first query. */
private const val SAMPLE_CSV: String = """name,city,age
Alice,Hangzhou,31
Bob,Shanghai,24
Carol,Shenzhen,45"""

/**
 * Starts DuckDB WASM from the files next to the page, then opens an in-memory database.
 *
 * The bundle is chosen by [selectBundle] from the platform's WASM features; both the module and the
 * worker script are served from `duckdb/`, which the `duckdbAssets` Gradle task fills from the npm
 * package, so nothing here depends on a CDN.
 *
 * The database outlives the page that started it: the layout caches pages per route, so switching to
 * another page and back keeps the same `AsyncDuckDB` and the same results.
 */
fun bootDuckDb(onReady: (AsyncDuckDB) -> Unit, onFailure: (String) -> Unit) {
    val bundles = unsafeJso<DuckDBBundles> {
        mvp = bundleFiles("duckdb-mvp.wasm", "duckdb-browser-mvp.worker.js")
        eh = bundleFiles("duckdb-eh.wasm", "duckdb-browser-eh.worker.js")
    }

    selectBundle(bundles).thenDo({ bundle ->
        val workerUrl = bundle.mainWorker ?: assetUrl("duckdb/duckdb-browser-mvp.worker.js")
        val db = AsyncDuckDB(consoleLogger(LogLevels.WARNING), Worker(workerUrl))
        db.instantiate(bundle.mainModule, bundle.pthreadWorker).thenDo({
            db.open(unsafeJso<DuckDBConfig> { path = ":memory:" }).thenDo({
                db.registerFileText("people.csv", SAMPLE_CSV).thenDo({
                    onReady(db)
                }, { onFailure(it.toString()) })
            }, { onFailure(it.toString()) })
        }, { onFailure(it.toString()) })
    }, { onFailure(it.toString()) })
}

/**
 * Resolves [relative] against the directory the page was served from.
 *
 * `document.baseURI` cannot be used here: the shell routes with the hash, so on `#/console` it reads
 * `…/#/console` and string concatenation would glue the path onto the fragment — the worker then
 * fetches the page itself and dies on the HTML. `location` splits into origin and path, and the last
 * segment of the path is dropped, which is exactly the directory holding `duckdb/`.
 */
private fun assetUrl(relative: String): String {
    val location = document.location
    val path = location.pathname
    val directory = path.substring(0, path.lastIndexOf('/') + 1)
    return location.origin + directory + relative
}

private fun bundleFiles(module: String, worker: String): DuckDBBundleFiles = unsafeJso {
    mainModule = assetUrl("duckdb/$module")
    mainWorker = assetUrl("duckdb/$worker")
}

/** Runs [action] when the promise resolves, [onError] when it rejects, and ignores the result. */
fun <T> Promise<T>.thenDo(action: (T) -> Unit, onError: (Throwable) -> Unit) {
    then({ value -> action(value) }, { failure -> onError(failure) })
}
