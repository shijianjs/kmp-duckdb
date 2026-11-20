package demo.page.arrow

import io.github.shijianjs.ktjs.demo.page.DemoPage
import demo.arrow.ArrowTable
import io.github.shijianjs.ktjs.demo.i18n.Binder
import io.github.shijianjs.ktjs.demo.i18n.Lang
import io.github.shijianjs.ktjs.demo.i18n.Langs
import io.github.shijianjs.ktjs.demo.i18n.Translations
import io.github.shijianjs.ktjs.demo.i18n.texts
import io.github.shijianjs.ktjs.demo.page.Blocks.codeText
import io.github.shijianjs.ktjs.demo.page.SectionBlock
import io.github.shijianjs.ktjs.demo.page.Readout
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.ktjs.duckdb.arrow.Table
import io.github.shijianjs.ktjs.duckdb.arrow.tableFromArrays
import io.github.shijianjs.ktjs.duckdb.arrow.tableFromIPC
import io.github.shijianjs.ktjs.duckdb.arrow.tableToIPC
import js.buffer.ArrayBufferLike
import js.objects.unsafeJso
import js.typedarrays.Int32Array

/** The columns object [tableFromArrays] accepts. */
private external interface IdColumn {
    var id: Int32Array<ArrayBufferLike>
}

private val arrowEnglish: ArrowTexts = ArrowTexts(
    title = "Arrow round trip",
    summary = "A table built in Kotlin, serialized to Arrow IPC and parsed back — all three steps through " +
        "the wrapper.",
    roundTripHeading = "Build, serialize, read back",
    roundTripHint = "tableFromArrays builds the table from a typed array, tableToIPC serializes it and " +
        "tableFromIPC reads it again. The status line reports what came back, including the field name " +
        "that survived the trip.",
)

private val arrowChinese: ArrowTexts = ArrowTexts(
    title = "Arrow 表往返",
    summary = "在 Kotlin 里构造一张表、序列化成 Arrow IPC，再解析回来 —— 三步都走封装。",
    roundTripHeading = "构造、序列化、读回",
    roundTripHint = "tableFromArrays 用 typed array 构造表，tableToIPC 序列化，tableFromIPC 再读回。" +
        "状态行报告读回来的是什么，包括往返之后仍然保留的字段名。",
)

private val arrowTexts: Translations<ArrowTexts> = texts(
    Langs.EN to arrowEnglish,
    Langs.ZH_HANS to arrowChinese,
)

/**
 * The Arrow page needs no worker and no network: it builds the table, serializes it and reads it
 * back while the page is constructed, so the table is there before the route is even shown.
 */
internal class ArrowPage : DemoPage() {

    private val texts = Binder(arrowTexts)

    private val status = Readout.NAME { show() }

    private val result: ArrowTable = ArrowTable.NAME()

    init {

        val block = SectionBlock.NAME { show(arrowEnglish.roundTripHeading, arrowEnglish.roundTripHint) }
        texts.bind(block.heading) { it.roundTripHeading }
        texts.bind(block.hint) { it.roundTripHint }
        block.body.append(
            status.root,
            result,
            codeText("tableFromArrays → tableToIPC → tableFromIPC"),
        )
        body.append(block.root)

        val table = roundTrip()
        result.show(table)
        // The status text describes data, so it is set once and survives a language switch.
        status.set(
            "${table.numRows.toInt()} rows · ${table.numCols.toInt()} column · " +
                "${tableToIPC(table).length} IPC bytes · schema preserved: ${table.schema.fields[0].name}",
        )
    }

    override fun applyTranslation(lang: Lang) {
        val current = arrowTexts[lang]
        setTitle(current.title)
        setSummary(current.summary)
        texts.apply(lang)
    }

    /** Builds the table, serializes it and parses the bytes back; returns the parsed table. */
    private fun roundTrip(): Table {
        val ids = Int32Array<ArrayBufferLike>(4).apply {
            this[0] = 10
            this[1] = 20
            this[2] = 30
            this[3] = 40
        }
        val built = tableFromArrays(unsafeJso<IdColumn> { id = ids })
        return tableFromIPC(tableToIPC(built))
    }

    companion object : CustomElementReferenceAdapter<ArrowPage>(ArrowPage::class.js, "duckdb-arrow-page")
}
