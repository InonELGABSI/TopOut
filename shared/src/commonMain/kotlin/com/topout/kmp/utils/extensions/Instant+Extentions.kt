package com.topout.kmp.utils.extensions

import kotlinx.datetime.*

/** Formats an `Instant` like “2025-11-05 11:20:45”. */
fun Instant.asSessionTitle(zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val dt = toLocalDateTime(zone)
    fun Int.p2() = toString().padStart(2, '0')
    return "${dt.year}-${dt.monthNumber.p2()}-${dt.dayOfMonth.p2()} " +
            "${dt.hour.p2()}:${dt.minute.p2()}:${dt.second.p2()}"
}
