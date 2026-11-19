package demo.page.overview

import io.github.shijianjs.ktjs.demo.page.DemoPage
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import web.components.CustomElementReference
import web.dom.TagName
import demo.API_URL
import demo.DOCS_URL
import demo.SOURCE_URL
import io.github.shijianjs.ktjs.demo.i18n.Binder
import io.github.shijianjs.ktjs.demo.i18n.Lang
import io.github.shijianjs.ktjs.demo.i18n.Langs
import io.github.shijianjs.ktjs.demo.i18n.Translations
import io.github.shijianjs.ktjs.demo.i18n.texts
import io.github.shijianjs.ktjs.demo.page.anchor
import io.github.shijianjs.ktjs.demo.page.paragraph
import io.github.shijianjs.ktjs.demo.page.sectionBlock
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.element.unaryPlus
import web.html.HtmlTagName

/** Every string this page shows, in one language. */
private class OverviewTexts(
    val title: String,
    val summary: String,
    val docsLink: String,
    val sourceLink: String,
    val apiLink: String,
    val purposeHeading: String,
    val body: String,
    val bullets: List<String>,
    val pagesHeading: String,
    val pagesBody: String,
)

private val overviewEnglish: OverviewTexts = OverviewTexts(
    title = "Overview",
    summary = "Kotlin/JS wrappers for DuckDB WASM, the DuckDB Node client and Apache Arrow.",
    docsLink = "Documentation",
    sourceLink = "Source repository",
    apiLink = "API reference",
    purposeHeading = "What it wraps",
    body = "DuckDB ships as a WASM module in the browser and as a native binding in Node, and both speak " +
        "Apache Arrow. These wrappers keep that: the wasm module gets a typed Kotlin API, the Node client " +
        "keeps its async surface, and Arrow tables are read through their schema and column vectors " +
        "instead of being turned into untyped objects.",
    bullets = listOf(
        "AsyncDuckDB, selectBundle — the WASM module, a worker and a bundle chosen from the platform.",
        "Connection.query — a statement in, an Arrow Table out.",
        "tableFromArrays / tableToIPC / tableFromIPC — build, serialize and parse a table.",
    ),
    pagesHeading = "What the pages show",
    pagesBody = "The console starts DuckDB in a worker, registers a CSV and runs a query against it. The " +
        "Arrow page does no I/O at all: it builds a table in Kotlin, serializes it and reads it back, " +
        "which is the pattern the docs recommend for consuming results.",
)

private val overviewChinese: OverviewTexts = OverviewTexts(
    title = "概览",
    summary = "面向 DuckDB WASM、DuckDB Node 客户端与 Apache Arrow 的 Kotlin/JS 封装。",
    docsLink = "使用文档",
    sourceLink = "源码仓库",
    apiLink = "API 文档",
    purposeHeading = "它封装了什么",
    body = "DuckDB 在浏览器里是一份 WASM 模块、在 Node 里是原生绑定，两者都用 Apache Arrow 传结果。" +
        "这几层封装保留这一点：wasm 模块有带类型的 Kotlin API，Node 客户端保留异步接口，" +
        "Arrow 表则通过 schema 与列向量读取，而不是先转成无类型的对象。",
    bullets = listOf(
        "AsyncDuckDB、selectBundle —— WASM 模块、worker，以及按平台特性选出的 bundle。",
        "Connection.query —— 传进一条语句，拿回一张 Arrow 表。",
        "tableFromArrays / tableToIPC / tableFromIPC —— 构造、序列化、解析一张表。",
    ),
    pagesHeading = "这些页面在演示什么",
    pagesBody = "控制台页在 worker 里启动 DuckDB、注册一个 CSV 并对它执行查询；" +
        "Arrow 页不做任何 I/O：在 Kotlin 里构造表、序列化、再读回来，这正是文档推荐的消费方式。",
)

private val overviewTexts: Translations<OverviewTexts> = texts(
    Langs.EN to overviewEnglish,
    Langs.ZH_HANS to overviewChinese,
)

/** Landing page: what the wrappers cover, and which page demonstrates which part. */
internal class OverviewPage : DemoPage() {

    private val texts = Binder(overviewTexts)


    init {
        addLink(texts.bind(anchor("", DOCS_URL, external = true), { it.docsLink }))
        addLink(texts.bind(anchor("", API_URL, external = true), { it.apiLink }))
        addLink(texts.bind(anchor("", SOURCE_URL, external = true), { it.sourceLink }))

        val purpose = sectionBlock(overviewEnglish.purposeHeading, null)
        texts.bind(purpose.title) { it.purposeHeading }
        // One bound <li> per bullet, so a language switch rewrites each line in place.
        val bullets = HtmlTagName.ul {
            List(3) { index -> texts.bind(+HtmlTagName.li()) { it.bullets.getOrNull(index).orEmpty() } }
        }
        purpose.body.append(texts.bind(paragraph(), { it.body }), bullets)

        val pages = sectionBlock(overviewEnglish.pagesHeading, null)
        texts.bind(pages.title) { it.pagesHeading }
        pages.body.append(texts.bind(paragraph(), { it.pagesBody }))

        body.append(purpose.root, pages.root)
    }

    override fun applyTranslation(lang: Lang) {
        val current = overviewTexts[lang]
        setTitle(current.title)
        setSummary(current.summary)
        texts.apply(lang)
    }

    companion object : CustomElementReference<OverviewPage>(OverviewPage::class.js) {
        val NAME: TagName<OverviewPage> = TagName("duckdb-overview-page")

        fun define() {
            defineIfMissing(NAME, this)
        }
    }
}

/** Creates a defined `<duckdb-overview-page>`. */
internal fun newOverviewPage(): OverviewPage {
    OverviewPage.define()
    return OverviewPage.NAME()
}
