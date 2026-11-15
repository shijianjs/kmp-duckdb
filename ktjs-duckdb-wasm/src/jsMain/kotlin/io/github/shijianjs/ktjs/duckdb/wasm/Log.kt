@file:JsModule("@duckdb/duckdb-wasm")

package io.github.shijianjs.ktjs.duckdb.wasm

import kotlin.js.JsAny
import kotlin.js.unsafeCast

/*
 * Upstream's logging surface. The level/topic/event/origin enums are numeric TypeScript enums, so the
 * value types are declared here and their numeric literals — together with the wrapping factories —
 * live in `WasmSupport.kt`, because a `@JsModule` file may hold external declarations only.
 */

/** Upstream `enum LogLevel`: the severity a log sink filters on. */
sealed external interface LogLevel

/** Upstream `enum LogTopic`: what a log entry is about. */
sealed external interface LogTopic

/** Upstream `enum LogEvent`: the phase a log entry reports. */
sealed external interface LogEvent

/** Upstream `enum LogOrigin`: which layer produced a log entry. */
sealed external interface LogOrigin

/**
 * Upstream `type LogEntry<O, T, E, V>`: one log record.
 *
 * The payload type depends on the entry variant upstream, so [value] is opaque; read it by property.
 */
external interface LogEntry {
    /** When the entry was produced. */
    val timestamp: kotlin.js.Date

    /** The severity, from [LogLevels]. */
    val level: LogLevel

    /** Which layer produced the entry, from [LogOrigins]. */
    val origin: LogOrigin

    /** What the entry is about, from [LogTopics]. */
    val topic: LogTopic

    /** The phase, from [LogEvents]. */
    val event: LogEvent

    /** The payload of the variant; upstream types it per entry variant. */
    val value: JsAny?
}

/**
 * Upstream `interface Logger`: where duckdb-wasm sends its log entries.
 *
 * Declared as a function property rather than a method, because that is how a Kotlin-built sink is
 * passed in: `unsafeJso<Logger> { log = { entry -> … } }`.
 */
external interface Logger {
    /** Receives one entry. */
    var log: (LogEntry) -> Unit
}

/**
 * Upstream `class VoidLogger`: a sink that drops every entry.
 *
 * The class is not declared as `: Logger` because Kotlin requires an external class to restate the
 * members it inherits from an interface, which an external class cannot do; `voidLogger()` in
 * `WasmSupport.kt` hands back the instance already converted to [Logger].
 */
external class VoidLogger {
    /** Creates the sink. */
    constructor()
}

/** Upstream `class ConsoleLogger`: a sink that writes to the console above a level. */
external class ConsoleLogger {
    /** @param level entries below this [LogLevel] are dropped */
    constructor(level: LogLevel)
}

/** Upstream `getLogLevelLabel`: the tabular name of a level (`INFO`, …). */
external fun getLogLevelLabel(level: LogLevel): String

/** Upstream `getLogEventLabel`: the tabular name of an event (`OK`, …). */
external fun getLogEventLabel(event: LogEvent): String

/** Upstream `getLogTopicLabel`: the tabular name of a topic (`QUERY`, …). */
external fun getLogTopicLabel(topic: LogTopic): String

/** Upstream `getLogOriginLabel`: the tabular name of an origin (`WEB WORKER`, …). */
external fun getLogOriginLabel(origin: LogOrigin): String
