package io.github.shijianjs.ktjs.duckdb.node

import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Runs against the real native client: the wrapper's whole value is that a Kotlin call reaches
 * DuckDB's native bindings, which a browser or a mock cannot prove.
 */
class NodeApiTest {
    @Test
    fun opensAnInMemoryDatabaseAndReadsRows() = run {
        DuckDBNodeApi.DuckDBInstance.create(":memory:")
            .then { instance ->
                instance.connect().then { connection ->
                    connection.runAndReadAll(
                        "SELECT i, i * 2 AS doubled, 'row-' || i AS label " +
                            "FROM range(0, 5) t(i) ORDER BY i",
                    ).then { reader ->
                        assertEquals(3.0, reader.columnCount)
                        assertEquals(5.0, reader.currentRowCount)
                        assertTrue(reader.done)

                        val names = reader.columnNames()
                        assertEquals("i", names[0])
                        assertEquals("doubled", names[1])
                        assertEquals("label", names[2])

                        val rows = reader.getRows()
                        assertEquals(5, rows.size)
                        assertEquals("4", rows[2][1].toString())
                        assertEquals("row-3", rows[3][2].toString())

                        reader.closeSync()
                        connection.closeSync()
                        instance.closeSync()
                    }
                }
            }
            .asDynamic()
    }

    @Test
    fun bindsParametersAndReadsTheProjection() = run {
        DuckDBNodeApi.DuckDBInstance.create(":memory:")
            .then { instance ->
                instance.connect().then { connection ->
                    connection.runAndReadAll("SELECT ?::INTEGER + 1 AS answer", arrayOf(41))
                        .then { reader ->
                            assertEquals(1.0, reader.columnCount)
                            assertEquals("42", reader.value(0.0, 0.0).toString())
                            reader.closeSync()
                            connection.closeSync()
                            instance.closeSync()
                        }
                }
            }
            .asDynamic()
    }

    @Test
    fun preparedStatementRunsTwiceWithDifferentBindings() = run {
        DuckDBNodeApi.DuckDBInstance.create(":memory:")
            .then { instance ->
                instance.connect().then { connection ->
                    connection.prepare("SELECT ?::VARCHAR AS echoed")
                        .then { statement ->
                            statement.bind(arrayOf("first"), null)
                            statement.runAndReadAll()
                                .then { first ->
                                    assertEquals("first", first.value(0.0, 0.0).toString())
                                    statement.bind(arrayOf("second"), null)
                                    statement.runAndReadAll().then { second ->
                                        assertEquals("second", second.value(0.0, 0.0).toString())
                                        first.closeSync()
                                        second.closeSync()
                                        statement.destroySync()
                                        connection.closeSync()
                                        instance.closeSync()
                                    }
                                }
                        }
                }
            }
            .asDynamic()
    }
}

/** Awaits a promise chain by returning its last promise, so the Kotlin test returns `JsAny`. */
private fun Promise<*>.asDynamic(): JsAny = this as JsAny
