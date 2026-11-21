package demo.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import com.microsoft.playwright.options.WaitUntilState
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junitpioneer.jupiter.RetryingTest

/**
 * Smoke checks for the demo: it must load the bundle, run the shared shell, run a real DuckDB WASM
 * query, round-trip an Arrow table and switch languages without reloading, with no console errors or
 * failed requests.
 *
 * The shell lives in a shadow tree, so the demo's own nodes are selected through `#demo` and the page
 * element the shell mounts — never through the document root.
 */
@UsePlaywright(DemoOptions::class)
class DemoPageTest {
    @RetryingTest(maxAttempts = 2)
    fun `page renders the shell and the overview`(page: Page) {
        openPage(page)

        assertTrue(page.title().contains("kmp-duckdb"), "title: ${page.title()}")

        // The logo is copied in from the site's static files by the `brandAssets` task.
        assertTrue(page.locator("#demo img.layout__logo").isVisible(), "the shell serves the project logo")
        assertEquals("Overview", page.locator("#demo duckdb-overview-page h1").first().textContent())

        // The drawer lists the three routes; the item carries the route id the router reads.
        assertEquals(3, page.locator("#demo vaadin-side-nav-item[data-route]").count())
    }

    @RetryingTest(maxAttempts = 2)
    fun `duckdb wasm runs the sample query and renders an arrow table`(page: Page) {
        openPage(page)
        openConsole(page)

        // The status line turns into the ready message once the worker, the wasm module and the
        // registered CSV are all in place.
        page.waitForSelector("#demo duckdb-console-page .readout:has-text(\"DuckDB is ready\")")

        val grid = page.locator("#demo duckdb-console-page table.arrow-table")
        page.waitForSelector("#demo duckdb-console-page table.arrow-table")
        assertEquals(5, grid.locator("thead th").count(), "name, city, age, n, squared")
        assertEquals(12, grid.locator("tbody tr").count(), "3 people × 4 generated rows")
        assertTrue(
            grid.locator("tbody tr").first().textContent()!!.contains("Carol"),
            "the oldest person comes first: ${grid.locator("tbody tr").first().textContent()}",
        )
        assertTrue(
            grid.locator("thead th").first().textContent()!!.contains("name:"),
            "header cells show the Arrow field type",
        )
    }

    @RetryingTest(maxAttempts = 2)
    fun `the language switch rewrites the page in place`(page: Page) {
        openPage(page)
        openConsole(page)

        page.waitForSelector("#demo duckdb-console-page .readout:has-text(\"DuckDB is ready\")")
        page.waitForSelector("#demo duckdb-console-page table.arrow-table")

        switchLanguage(page, "zh-Hans")
        assertEquals("SQL 控制台", page.locator("#demo duckdb-console-page h1").first().textContent())

        // The result is data: switching the language must not re-run the query or clear the table.
        assertTrue(page.locator("#demo duckdb-console-page table.arrow-table").isVisible())
        assertEquals(12, page.locator("#demo duckdb-console-page table.arrow-table tbody tr").count())
    }

    @RetryingTest(maxAttempts = 2)
    fun `arrow page builds and round trips a table`(page: Page) {
        openPage(page)
        page.locator("#demo vaadin-side-nav-item[data-route='arrow']").first().click()

        page.waitForSelector("#demo duckdb-arrow-page .readout")
        val status = page.locator("#demo duckdb-arrow-page .readout").first()
        val text = status.textContent()!!
        assertTrue(text.contains("4 rows"), "status: $text")
        assertTrue(text.contains("1 column"), "status: $text")
        assertTrue(text.contains("schema preserved: id"), "status: $text")

        val grid = page.locator("#demo duckdb-arrow-page table.arrow-table")
        assertEquals(1, grid.locator("thead th").count())
        assertEquals(4, grid.locator("tbody tr").count())
        assertEquals("10", grid.locator("tbody tr").first().locator("td").first().textContent())
    }

    @RetryingTest(maxAttempts = 2)
    fun `page logs no console errors or failed requests`(page: Page) {
        val consoleErrors = mutableListOf<String>()
        val failedRequests = mutableListOf<String>()
        page.onConsoleMessage { message ->
            if (message.type() == "error") consoleErrors += message.text()
        }
        page.onRequestFailed { request ->
            failedRequests += "${request.url()} ${request.failure()}"
        }

        openPage(page)
        openConsole(page)
        // Let the worker boot, fetch the wasm module and run the sample query before judging.
        page.waitForSelector("#demo duckdb-console-page table.arrow-table")

        assertTrue(consoleErrors.isEmpty(), "console errors: $consoleErrors")
        assertTrue(failedRequests.isEmpty(), "failed requests: $failedRequests")
    }

    private fun openPage(page: Page) {
        page.navigate("/", Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
    }

    /** The console is the second route; clicking its menu item is also a check of the router. */
    private fun openConsole(page: Page) {
        page.locator("#demo vaadin-side-nav-item[data-route='console']").first().click()
    }

    /** Opens the layout's language menu and picks [code]. */
    private fun switchLanguage(page: Page, code: String) {
        page.locator("#demo [data-menu='lang']").first().click()
        page.locator("#demo [data-action='lang:$code']").first().click()
    }
}
