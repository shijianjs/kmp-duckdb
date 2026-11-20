package demo.page.console

import io.github.shijianjs.ktjs.dom.element.invoke

/** Every string this page shows, in one language. */
internal class ConsoleTexts(
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
