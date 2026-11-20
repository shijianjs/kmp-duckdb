package demo.page.console

import io.github.shijianjs.ktjs.demo.page.DemoPage
import io.github.shijianjs.ktjs.demo.page.SectionBlock
import io.github.shijianjs.ktjs.demo.page.Readout
import demo.arrow.ArrowTable
import demo.duckdb.SAMPLE_SQL
import demo.duckdb.bootDuckDb
import demo.duckdb.thenDo
import io.github.shijianjs.ktjs.demo.i18n.Binder
import io.github.shijianjs.ktjs.demo.i18n.Lang
import io.github.shijianjs.ktjs.demo.i18n.Langs
import io.github.shijianjs.ktjs.demo.i18n.Translations
import io.github.shijianjs.ktjs.demo.i18n.texts
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.ktjs.duckdb.wasm.AsyncDuckDB
import io.github.shijianjs.ktjs.vaadin.button.createButton
import io.github.shijianjs.ktjs.vaadin.textarea.createTextArea
import web.dom.clickEvent
import web.events.EventHandler
import web.html.HTMLElement

/** Every string this page shows, in one language. */
private class ConsoleTexts(
    val title: String,
    val summary: String,
    val consoleHeading: String,
    val consoleHint: String,
    val sqlLabel: String,
    val runLabel: String,
    val loading: String,
    val ready: String,
    val failed: String,
)

private val consoleEnglish: ConsoleTexts = ConsoleTexts(
    title = "SQL console",
    summary = "The database runs in a web worker next to this page; results come back as Apache Arrow " +
        "tables, which the wrapper reads column by column.",
    consoleHeading = "DuckDB WASM",
    consoleHint = "The sample file people.csv is registered with registerFileText at boot, so the query " +
        "can join it straight away. Both the module and the worker script are served from duckdb/, next " +
        "to this page — nothing is fetched from a CDN.",
    sqlLabel = "SQL",
    runLabel = "Run",
    loading = "Loading the WASM module …",
    ready = "DuckDB is ready. Run the query to see an Arrow table.",
    failed = "DuckDB failed to start: ",
)

private val consoleChinese: ConsoleTexts = ConsoleTexts(
    title = "SQL 控制台",
    summary = "数据库运行在同页面的 web worker 里，结果以 Apache Arrow 表返回，再由封装逐列读出来。",
    consoleHeading = "DuckDB WASM",
    consoleHint = "示例文件 people.csv 在启动时由 registerFileText 注册，所以查询可以直接 join 它。" +
        "模块与 worker 脚本都从本页旁边的 duckdb/ 目录取，不依赖任何 CDN。",
    sqlLabel = "SQL",
    runLabel = "执行",
    loading = "正在加载 WASM 模块 …",
    ready = "DuckDB 已就绪，点击执行即可看到 Arrow 表。",
    failed = "DuckDB 启动失败：",
)

private val consoleTexts: Translations<ConsoleTexts> = texts(
    Langs.EN to consoleEnglish,
    Langs.ZH_HANS to consoleChinese,
)

/**
 * The live console: a text area, a run button and an Arrow table.
 *
 * It is one of the few pages whose state matters — the database, the statement in the text area and
 * the last result all survive a trip to another route, because the layout caches the page instance
 * instead of rebuilding it.
 */
internal class ConsolePage : DemoPage() {

    private val texts = Binder(consoleTexts)

    private val status = Readout.NAME { show(consoleEnglish.loading) }

    private val sql = createTextArea {
        label = consoleEnglish.sqlLabel
        value = SAMPLE_SQL
        minRows = 6.0
    }

    private val run = createButton {
        textContent = consoleEnglish.runLabel
        disabled = true
    }

    private val result: ArrowTable = ArrowTable.NAME()

    /** The language the page currently shows; the status line is derived from it, not from a binder. */
    /** The language the console is shown in; `lang` is an attribute of `HTMLElement`. */
    private var shownIn: Lang = Langs.EN

    private var ready: Boolean = false

    private var failure: String? = null


    init {

        val block = SectionBlock.NAME { show(consoleEnglish.consoleHeading, consoleEnglish.consoleHint) }
        texts.bind(block.heading) { it.consoleHeading }
        texts.bind(block.hint) { it.consoleHint }
        block.body.append(sql, run, status.root, result)
        body.append(block.root)

        bootDuckDb(
            onReady = { db ->
                ready = true
                refreshStatus()
                run.disabled = false
                run.onclick = EventHandler { runQuery(db, sql.value) }
                // Run the sample once, so the page never shows an empty result area.
                runQuery(db, sql.value)
            },
            onFailure = { reason ->
                failure = reason
                refreshStatus()
            },
        )
    }

    override fun applyTranslation(lang: Lang) {
        shownIn = lang
        val current = consoleTexts[shownIn]
        setTitle(current.title)
        setSummary(current.summary)
        texts.apply(lang)
        // The label and the button text are properties of components, not text of a node, so they are
        // assigned here instead of going through the binder.
        sql.label = current.sqlLabel
        run.textContent = current.runLabel
        refreshStatus()
    }

    /** The status line reports state — loading, ready or failed — so it is rebuilt from that state. */
    private fun refreshStatus() {
        val current = consoleTexts[shownIn]
        status.set(
            when {
                failure != null -> current.failed + failure
                ready -> current.ready
                else -> current.loading
            },
        )
    }

    private fun runQuery(db: AsyncDuckDB, statement: String) {
        db.connect().thenDo({ connection ->
            connection.query(statement).thenDo({ table ->
                result.show(table)
                connection.close()
            }, { error ->
                result.showError(error.toString())
            })
        }, { error ->
            result.showError(error.toString())
        })
    }

    companion object : CustomElementReferenceAdapter<ConsolePage>(ConsolePage::class.js, "duckdb-console-page")
}

