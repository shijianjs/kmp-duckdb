package demo.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import com.microsoft.playwright.options.WaitUntilState
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junitpioneer.jupiter.RetryingTest

/**
 * Smoke checks for the demo page: it must load the bundle, apply its stylesheet, run a real DuckDB
 * WASM query, round-trip an Arrow table and switch languages without reloading, all with no console
 * errors or failed requests.
 */
@UsePlaywright(DemoOptions::class)
class DemoPageTest {
    @RetryingTest(maxAttempts = 2)
    fun `page renders the project heading and styles`(page: Page) {
        openPage(page)

        assertTrue(page.title().contains("kmp-duckdb"), "title: ${page.title()}")
        assertEquals("kmp-duckdb demo", page.locator("h1").textContent())

        // The logo is copied in from the site's static files by the `brandAssets` task.
        assertTrue(page.locator("img.logo").isVisible(), "the demo serves the project logo")

        val display = page.locator("#duckdb").evaluate("(node) => getComputedStyle(node).display") as String
        assertTrue(display != "contents", "the stylesheet applies to .card (display = $display)")
    }

    @RetryingTest(maxAttempts = 2)
    fun `the language toggle rewrites the page in place`(page: Page) {
        openPage(page)

        val toggle = page.locator(".lang-toggle")
        assertEquals("中文", toggle.textContent())

        toggle.click()
        assertEquals("kmp-duckdb 演示", page.locator("h1").textContent())
        assertEquals("English", toggle.textContent())

        // The DuckDB card keeps whatever it rendered: the toggle is a text swap, not a reload.
        page.locator("#duckdb table.grid").waitFor()
        toggle.click()
        assertEquals("kmp-duckdb demo", page.locator("h1").textContent())
        assertTrue(page.locator("#duckdb table.grid").isVisible())
    }

    @RetryingTest(maxAttempts = 2)
    fun `duckdb wasm runs the sample query and renders an arrow table`(page: Page) {
        openPage(page)

        // The status line turns into the ready message once the worker, the wasm module and the
        // registered CSV are all in place.
        page.locator("#duckdb .status").getByText("DuckDB is ready").waitFor()

        assertEquals("", page.locator("#duckdb .error").textContent())

        val grid = page.locator("#duckdb table.grid")
        grid.waitFor()
        assertEquals(5, grid.locator("thead th").count(), "name, city, age, n, squared")
        assertEquals(12, grid.locator("tbody tr").count(), "3 people × 4 generated rows")
        assertTrue(
            grid.locator("tbody tr").first().textContent().contains("Carol"),
            "the oldest person comes first: ${grid.locator("tbody tr").first().textContent()}",
        )
        assertTrue(
            grid.locator("thead th").first().textContent().contains("name:"),
            "header cells show the Arrow field type",
        )
    }

    @RetryingTest(maxAttempts = 2)
    fun `arrow card builds and round trips a table`(page: Page) {
        openPage(page)

        val status = page.locator("#arrow .status")
        assertTrue(status.textContent().contains("4 rows"), "status: ${status.textContent()}")
        assertTrue(status.textContent().contains("1 column"), "status: ${status.textContent()}")
        assertTrue(status.textContent().contains("schema preserved: id"), "status: ${status.textContent()}")

        val grid = page.locator("#arrow table.grid")
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
        // Let the worker boot, fetch the wasm module and run the sample query before judging.
        page.locator("#duckdb table.grid").waitFor()

        assertTrue(consoleErrors.isEmpty(), "console errors: $consoleErrors")
        assertTrue(failedRequests.isEmpty(), "failed requests: $failedRequests")
    }

    private fun openPage(page: Page) {
        page.navigate("/", Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
    }
}
