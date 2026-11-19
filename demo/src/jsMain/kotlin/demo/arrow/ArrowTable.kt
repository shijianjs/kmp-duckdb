package demo.arrow

import io.github.shijianjs.ktjs.demo.style.Palette
import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.duckdb.arrow.Table
import web.cssom.BorderCollapse
import web.cssom.LineStyle
import web.cssom.CSSStyleSheet
import web.cssom.ClassName
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.pct
import web.cssom.px
import web.html.HTMLTableElement
import web.html.HTMLElement
import web.html.HtmlTagName
import kotlin.js.JsAny

/**
 * The three class names a rendered Arrow table uses, and the rules that go with them.
 *
 * A `<table>` has no component that fits an arbitrary schema, so it is the one element these pages
 * build themselves; the rules stay in this file, and a page adopts the sheet through
 * `DemoPage.addSheet`, which keeps them inside the page's shadow tree.
 */
internal val TABLE: ClassName = ClassName("arrow-table")

internal val TABLE_HEAD: ClassName = ClassName("arrow-table__head")

internal val TABLE_CELL: ClassName = ClassName("arrow-table__cell")

internal val TABLE_SHEET: CSSStyleSheet = cssSheet {
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
 * Renders [table] into [out], reading each cell through its column vector.
 *
 * This is the pattern the docs recommend for Kotlin consumers: the schema gives the column names,
 * `getChildAt` gives the column vector and `get(row)` gives the value.
 */
fun renderTable(table: Table, out: HTMLElement) {
    out.replaceChildren()
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
    out.append(html)
}

private fun formatValue(value: JsAny?): String = when {
    value == null -> "NULL"
    else -> value.toString()
}
