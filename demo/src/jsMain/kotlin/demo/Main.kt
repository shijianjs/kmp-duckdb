package demo

import io.github.shijianjs.ktjs.duckdb.arrow.Table
import io.github.shijianjs.ktjs.duckdb.arrow.tableFromArrays
import io.github.shijianjs.ktjs.duckdb.arrow.tableFromIPC
import io.github.shijianjs.ktjs.duckdb.arrow.tableToIPC
import io.github.shijianjs.ktjs.duckdb.wasm.AsyncDuckDB
import io.github.shijianjs.ktjs.duckdb.wasm.DuckDBBundleFiles
import io.github.shijianjs.ktjs.duckdb.wasm.DuckDBBundles
import io.github.shijianjs.ktjs.duckdb.wasm.DuckDBConfig
import io.github.shijianjs.ktjs.duckdb.wasm.LogLevels
import io.github.shijianjs.ktjs.duckdb.wasm.consoleLogger
import io.github.shijianjs.ktjs.duckdb.wasm.selectBundle
import js.buffer.ArrayBufferLike
import js.objects.unsafeJso
import js.typedarrays.Int32Array
import web.cssom.ClassName
import web.dom.document
import web.events.EventHandler
import web.html.HTMLButtonElement
import web.html.HTMLElement
import web.html.HTMLTableElement
import web.html.HTMLTextAreaElement
import web.html.HtmlTagName
import web.navigator.navigator
import web.workers.Worker
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.unsafeCast

private const val PROJECT_NAME = "kmp-duckdb"

/**
 * Entry point of the demo page.
 *
 * Two cards: a live DuckDB WASM console whose results are Arrow tables read through the Arrow
 * wrapper, and an Arrow card that builds a table in Kotlin, serializes it to Arrow IPC and parses
 * it back. Every label is registered for translation, so the language toggle rewrites the page in
 * place instead of reloading it and losing the query result.
 */
fun main() {
    val page = document.createElement(HtmlTagName.div).apply { className = ClassName("page") }
    val translations = Translations(if (navigator.language.startsWith("zh")) Lang.ZH else Lang.EN)

    // region Header and navigation

    val header = document.createElement(HtmlTagName.header).apply { className = ClassName("header") }
    val logo = document.createElement(HtmlTagName.img).apply {
        className = ClassName("logo")
        setAttribute("src", "img/kmp-duckdb-logo.svg")
        setAttribute("alt", "$PROJECT_NAME logo")
    }
    val heading = document.createElement(HtmlTagName.h1)
    translations.set(heading, "$PROJECT_NAME demo", "$PROJECT_NAME 演示")
    val subtitle = document.createElement(HtmlTagName.p).apply { className = ClassName("subtitle") }
    translations.set(
        subtitle,
        "Kotlin/JS wrappers for DuckDB WASM, the DuckDB Node client and Apache Arrow.",
        "面向 DuckDB WASM、DuckDB Node 客户端与 Apache Arrow 的 Kotlin/JS 封装。",
    )

    val languageButton = document.createElement(HtmlTagName.button).unsafeCast<HTMLButtonElement>()
        .apply {
            className = ClassName("lang-toggle")
            textContent = translations.alternateName()
            onclick = EventHandler {
                translations.toggle()
                textContent = translations.alternateName()
            }
        }

    header.append(logo, heading, subtitle, languageButton)

    val nav = document.createElement(HtmlTagName.nav).apply { className = ClassName("nav") }
    nav.append(
        // The demo lives at `<site>/demo/`, so both links are absolute: a relative `api/` would
        // resolve to `<site>/demo/api/`, which does not exist.
        navLink(translations, "https://shijianjs.github.io/kmp-duckdb/", "Documentation", "文档"),
        navLink(translations, "https://shijianjs.github.io/kmp-duckdb/api/", "API reference", "API 参考"),
        navLink(translations, "https://github.com/shijianjs/kmp-duckdb", "GitHub", "GitHub"),
    )

    // endregion

    // region DuckDB WASM card

    val duckCard = card("duckdb")
    val duckTitle = document.createElement(HtmlTagName.h2)
    translations.set(duckTitle, "DuckDB WASM · SQL console", "DuckDB WASM · SQL 控制台")
    val duckHint = document.createElement(HtmlTagName.p).apply { className = ClassName("hint") }
    translations.set(
        duckHint,
        "The database runs in a web worker next to this page and results come back as Apache Arrow " +
            "tables. The sample file `people.csv` is registered with `registerFileText` at boot.",
        "数据库运行在同页面的 web worker 中，查询结果以 Apache Arrow 表返回。" +
            "示例文件 `people.csv` 在启动时由 `registerFileText` 注册。",
    )
    val duckStatus = document.createElement(HtmlTagName.p).apply { className = ClassName("status") }
    translations.set(duckStatus, "Loading the WASM module …", "正在加载 WASM 模块 …")

    val sql = document.createElement(HtmlTagName.textarea).unsafeCast<HTMLTextAreaElement>().apply {
        className = ClassName("sql")
        rows = 5
        value = SAMPLE_SQL
    }

    val runButton = document.createElement(HtmlTagName.button).unsafeCast<HTMLButtonElement>().apply {
        className = ClassName("run")
        disabled = true
    }
    translations.set(runButton, "Run", "执行")

    val error = document.createElement(HtmlTagName.p).apply { className = ClassName("error") }
    val result = document.createElement(HtmlTagName.div).apply { className = ClassName("result") }

    duckCard.append(duckTitle, duckHint, duckStatus, sql, runButton, error, result)

    // endregion

    // region Arrow card

    val arrowCard = card("arrow")
    val arrowTitle = document.createElement(HtmlTagName.h2)
    translations.set(arrowTitle, "Apache Arrow · table round trip", "Apache Arrow · 表往返")
    val arrowHint = document.createElement(HtmlTagName.p).apply { className = ClassName("hint") }
    translations.set(
        arrowHint,
        "`tableFromArrays` builds a table from a typed array, `tableToIPC` serializes it and " +
            "`tableFromIPC` reads it back — all three through the Arrow wrapper.",
        "`tableFromArrays` 用 typed array 构造表，`tableToIPC` 序列化，`tableFromIPC` 再读回，" +
            "三步都走 Arrow 封装。",
    )
    val arrowStatus = document.createElement(HtmlTagName.p).apply { className = ClassName("status") }
    val arrowResult = document.createElement(HtmlTagName.div).apply { className = ClassName("result") }
    arrowCard.append(arrowTitle, arrowHint, arrowStatus, arrowResult)

    // endregion

    page.append(header, nav, duckCard, arrowCard)
    document.body.append(page)
    translations.apply()

    // The Arrow card needs no network and no worker, so it renders immediately.
    renderArrowCard(arrowStatus, arrowResult)

    bootDuckDb(
        onReady = { db ->
            translations.set(
                duckStatus,
                "DuckDB is ready. Run the query to see an Arrow table.",
                "DuckDB 已就绪，点击执行即可看到 Arrow 表。",
            )
            translations.apply()
            runButton.disabled = false

            runButton.onclick = EventHandler {
                runSql(db, sql.value, result, error)
            }
            // Run the sample once so the card is never empty.
            runSql(db, sql.value, result, error)
        },
        onFailure = { reason ->
            translations.set(duckStatus, "DuckDB failed to start: $reason", "DuckDB 启动失败：$reason")
            translations.apply()
        },
    )
}

/** The sample query: it joins the CSV registered at boot with a generated series. */
private const val SAMPLE_SQL = """-- A registered CSV joined with a generated series
SELECT
    p.name,
    p.city,
    p.age,
    r AS n,
    r * r AS squared
FROM 'people.csv' p, range(0, 4) t(r)
ORDER BY p.age DESC, n"""

/** The CSV registered as `people.csv` before the first query. */
private const val SAMPLE_CSV = """name,city,age
Alice,Hangzhou,31
Bob,Shanghai,24
Carol,Shenzhen,45"""

private fun card(id: String): HTMLElement = document.createElement(HtmlTagName.section).apply {
    className = ClassName("card")
    setAttribute("id", id)
}

private fun navLink(translations: Translations, href: String, en: String, zh: String): HTMLElement =
    document.createElement(HtmlTagName.a).apply {
        className = ClassName("nav-link")
        setAttribute("href", href)
        translations.set(this, en, zh)
    }

// region DuckDB

/**
 * Starts DuckDB WASM from the files next to the page, then opens an in-memory database.
 *
 * The bundle is chosen by [selectBundle] from the platform's WASM features; both the module and the
 * worker script are served from `duckdb/`, which the `duckdbAssets` Gradle task fills from the npm
 * package, so nothing here depends on a CDN.
 */
private fun bootDuckDb(onReady: (AsyncDuckDB) -> Unit, onFailure: (String) -> Unit) {
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
 * Resolves [relative] against the page, not against the caller's script.
 *
 * The worker fetches the `.wasm` module from inside the worker, where a bare relative URL resolves
 * against the worker script's directory; an absolute URL is correct in both contexts and keeps
 * working when the page is served from a sub-path such as `/demo/`.
 */
private fun assetUrl(relative: String): String = document.baseURI + relative

private fun bundleFiles(module: String, worker: String): DuckDBBundleFiles = unsafeJso {
    mainModule = assetUrl("duckdb/$module")
    mainWorker = assetUrl("duckdb/$worker")
}

private fun runSql(db: AsyncDuckDB, statement: String, out: HTMLElement, error: HTMLElement) {
    error.textContent = ""
    db.connect().thenDo({ connection ->
        connection.query(statement).thenDo({ table ->
            renderTable(table, out)
            connection.close()
        }, { failure ->
            error.textContent = failure.toString()
        })
    }, { failure ->
        error.textContent = failure.toString()
    })
}

// endregion

// region Arrow

private fun renderArrowCard(status: HTMLElement, out: HTMLElement) {
    val ids = Int32Array<ArrayBufferLike>(4).apply {
        this[0] = 10
        this[1] = 20
        this[2] = 30
        this[3] = 40
    }
    val table = tableFromArrays(unsafeJso<IdColumn> { id = ids })
    val bytes = tableToIPC(table)
    val restored = tableFromIPC(bytes)

    status.textContent = "${table.numRows.toInt()} rows · ${table.numCols.toInt()} column · " +
        "${bytes.length} IPC bytes · schema preserved: ${restored.schema.fields[0].name}"
    renderTable(table, out)
}

/** The columns object [tableFromArrays] accepts in [renderArrowCard]. */
external interface IdColumn {
    var id: Int32Array<ArrayBufferLike>
}

// endregion

/**
 * Renders a [Table] as an HTML table, reading each cell through its column vector.
 *
 * This is the pattern the docs recommend for Kotlin consumers: the schema gives the column names,
 * `getChildAt` gives the column vector and `get(row)` gives the value.
 */
private fun renderTable(table: Table, out: HTMLElement) {
    out.textContent = null
    val html = document.createElement(HtmlTagName.table).unsafeCast<HTMLTableElement>()
    html.className = ClassName("grid")

    val head = document.createElement(HtmlTagName.thead)
    val headRow = document.createElement(HtmlTagName.tr)
    val fields = table.schema.fields
    for (index in 0 until fields.size) {
        val cell = document.createElement(HtmlTagName.th).apply {
            textContent = "${fields[index].name}: ${fields[index].type}"
        }
        headRow.append(cell)
    }
    head.append(headRow)

    val body = document.createElement(HtmlTagName.tbody)
    val rows = table.numRows.toInt()
    for (row in 0 until rows) {
        val rowElement = document.createElement(HtmlTagName.tr)
        for (column in 0 until fields.size) {
            val vector = table.getChildAt(column.toDouble())
            val cell = document.createElement(HtmlTagName.td).apply {
                textContent = formatValue(vector?.get(row.toDouble()))
            }
            rowElement.append(cell)
        }
        body.append(rowElement)
    }

    html.append(head, body)
    out.append(html)
}

private fun formatValue(value: JsAny?): String = when {
    value == null -> "NULL"
    else -> value.toString()
}

/** Runs [action] when the promise resolves, [onError] when it rejects, and ignores the result. */
private fun <T> Promise<T>.thenDo(action: (T) -> Unit, onError: (Throwable) -> Unit) {
    then({ value -> action(value) }, { failure -> onError(failure) })
}

// region Language

private enum class Lang { EN, ZH }

/**
 * Keeps the elements whose text is translated and rewrites them in place when the language flips.
 *
 * Registering the translations instead of reading `data-en` / `data-zh` attributes keeps the page
 * correct even for text that changes at runtime, such as a status line reporting a failure.
 */
private class Translations(private var lang: Lang) {
    private val entries = mutableListOf<Triple<HTMLElement, String, String>>()

    /** Registers `element` with both texts; a later call for the same element replaces the pair. */
    fun set(element: HTMLElement, en: String, zh: String) {
        val index = entries.indexOfFirst { it.first == element }
        if (index >= 0) {
            entries[index] = Triple(element, en, zh)
        } else {
            entries += Triple(element, en, zh)
        }
        element.textContent = text(en, zh)
    }

    fun toggle() {
        lang = if (lang == Lang.ZH) Lang.EN else Lang.ZH
        apply()
    }

    fun apply() {
        entries.forEach { (element, en, zh) ->
            element.textContent = text(en, zh)
        }
    }

    /** The label of the button that flips the language. */
    fun alternateName(): String = if (lang == Lang.ZH) "English" else "中文"

    private fun text(en: String, zh: String): String = if (lang == Lang.ZH) zh else en
}

// endregion
