package demo

import demo.page.arrow.ArrowPage
import demo.page.console.ConsolePage
import demo.page.overview.OverviewPage
import io.github.shijianjs.ktjs.demo.nav.DemoMenu
import io.github.shijianjs.ktjs.demo.nav.Entry
import io.github.shijianjs.ktjs.demo.nav.Group
import io.github.shijianjs.ktjs.dom.element.invoke

/** The upstream packages this demo runs against; the navbar shows it under the site name. */
const val SITE_VERSION: String = "duckdb-wasm 1.x + arrow 21"

/** The documentation site. */
const val DOCS_URL: String = "https://shijianjs.github.io/kmp-duckdb/"

/** Dokka reference assembled by `pagesSite` next to the demo. */
const val API_URL: String = "https://shijianjs.github.io/kmp-duckdb/api/"

/** Source repository, linked from the navbar's link menu. */
const val SOURCE_URL: String = "https://github.com/shijianjs/kmp-duckdb"

internal val START: Group = Group("start", "lucide:sparkles")

internal val WASM: Group = Group("wasm", "lucide:database")

/**
 * The site: an overview, the live console and the Arrow round trip.
 *
 * Adding a page means adding one entry here and its titles in [siteTexts].
 */
fun siteMenu(): DemoMenu = DemoMenu(
    listOf(
        Entry("overview", START) { OverviewPage.NAME() },
        Entry("console", WASM) { ConsolePage.NAME() },
        Entry("arrow", WASM) { ArrowPage.NAME() },
    ),
)
