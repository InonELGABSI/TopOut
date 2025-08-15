// shared/src/commonMain/kotlin/com/topout/kmp/utils/extensions/SessionMetricsExtensions.kt
package com.topout.kmp.utils.extensions

import com.topout.kmp.models.TrackPoint
import kotlin.math.abs

/**
 * Sum of all positive altitude deltas.
 */
fun List<TrackPoint>.totalAscent(): Double {
    var gain = 0.0
    for (i in 1 until size) {
        val prev = this[i - 1].altitude
        val curr = this[i].altitude
        if (prev != null && curr != null && curr > prev) {
            gain += (curr - prev)
        }
    }
    return gain
}

/**
 * Sum of all negative altitude deltas (as a positive number).
 */
fun List<TrackPoint>.totalDescent(): Double {
    var loss = 0.0
    for (i in 1 until size) {
        val prev = this[i - 1].altitude
        val curr = this[i].altitude
        if (prev != null && curr != null && curr < prev) {
            loss += abs(curr - prev)
        }
    }
    return loss
}

/** Highest altitude seen (or 0.0 if none). */
fun List<TrackPoint>.maxAltitude(): Double =
    mapNotNull { it.altitude }.maxOrNull() ?: 0.0

/** Lowest altitude seen (or 0.0 if none). */
fun List<TrackPoint>.minAltitude(): Double =
    mapNotNull { it.altitude }.minOrNull() ?: 0.0

/**
 * Average of the per-point vertical speeds (vVertical).
 * Returns 0.0 if the list is empty.
 */
fun List<TrackPoint>.avgVerticalSpeed(): Double =
    if (isEmpty()) 0.0
    else map { it.avgVertical }.average()

fun List<TrackPoint>.avgHorizontalSpeed(): Double =
    if (isEmpty()) 0.0
    else map { it.avgHorizontal }.average()