package demo.arrow

import io.github.shijianjs.ktjs.demo.style.Palette
import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.ktjs.duckdb.arrow.Table
import web.components.CustomElementReference
import web.cssom.BorderCollapse
import web.cssom.CSSStyleSheet
import web.cssom.ClassName
import web.cssom.LineStyle
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.pct
import web.cssom.px
import web.dom.TagName
import web.html.HTMLTableElement
import web.html.HtmlTagName
import kotlin.js.JsAny

/**
 * The three class names the rendered table uses, and the rules that go with them.
 *
 * A `<table>` has no component that fits an arbitrary schema, so it is the one shape these pages build
 * themselves; the rules stay in this file, and the element adopts them into its own shadow tree.
 */
private val TABLE: ClassName = ClassName("arrow-table")

private val TABLE_HEAD: ClassName = ClassName("arrow-table__head")

private val TABLE_CELL: ClassName = ClassName("arrow-table__cell")

private val TABLE_SHEET: CSSStyleSheet = cssSheet {
    TABLE {
        width = 100.pct
        borderCollapse = BorderCollapse.collapse
        fontSize = 13.px
    }
    TABLE_HEAD {
        padding = Padding(4.px, 8.px)
        textAlign = TextAlign.left
        borderBottomWidth = 1.px
        borderBottomStyle = LineStyle.solid
        borderBottomColor = Palette.border
    }
    TABLE_CELL {
        padding = Padding(4.px, 8.px)
        textAlign = TextAlign.left
        borderBottomWidth = 1.px
        borderBottomStyle = LineStyle.solid
        borderBottomColor = Palette.border
    }
}

/**
 * `<duckdb-arrow-table>`: the result area of the two query pages.
 *
 * A query answers with a table or with an error, so the element shows either: [show] renders the Arrow
 * table, [showError] writes the message. Reading each cell through its column vector is the pattern the
 * docs recommend for Kotlin consumers — the schema gives the column names, `getChildAt` gives the
 * column vector and `get(row)` gives the value.
 */
internal class ArrowTable : CustomElementAdapter() {
    /** The only style scope this element has. */
    private val surface = attachStyledShadow(TABLE_SHEET)

    /** Renders [table], replacing whatever the previous query left behind. */
    fun show(table: Table) {
        val html: HTMLTableElement = HtmlTagName.table { classList.add(TABLE) }

        val head = HtmlTagName.thead()
        val headRow = HtmlTagName.tr()
        val fields = table.schema.fields
        for (index in 0 until fields.size) {
            headRow.append(
                HtmlTagName.th {
                    classList.add(TABLE_HEAD)
                    textContent = "${fields[index].name}: ${fields[index].type}"
                },
            )
        }
        head.append(headRow)

        val body = HtmlTagName.tbody()
        val rows = table.numRows.toInt()
        for (row in 0 until rows) {
            val rowElement = HtmlTagName.tr()
            for (column in 0 until fields.size) {
                val vector = table.getChildAt(column.toDouble())
                rowElement.append(
                    HtmlTagName.td {
                        classList.add(TABLE_CELL)
                        textContent = formatValue(vector?.get(row.toDouble()))
                    },
                )
            }
            body.append(rowElement)
        }

        html.append(head, body)
        surface.replaceChildren(html)
    }

    /** Shows the message of a failed query in place of the table. */
    fun showError(message: String) {
        surface.replaceChildren(HtmlTagName.p { textContent = message })
    }

    companion object : CustomElementReference<ArrowTable>(ArrowTable::class.js) {
        val NAME: TagName<ArrowTable> = TagName("duckdb-arrow-table")

        fun define() {
            defineIfMissing(NAME, this)
        }
    }
}

/** Creates a defined `<duckdb-arrow-table>`. */
internal fun newArrowTable(): ArrowTable {
    ArrowTable.define()
    return ArrowTable.NAME()
}

private fun formatValue(value: JsAny?): String = when {
    value == null -> "NULL"
    else -> value.toString()
}
