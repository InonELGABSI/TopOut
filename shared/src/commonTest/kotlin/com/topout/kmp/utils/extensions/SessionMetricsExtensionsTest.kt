package com.topout.kmp.utils.extensions

import com.topout.kmp.models.TrackPoint
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionMetricsExtensionsTest {

    @Test
    fun sessionMetrics_calculations_withMixedAltitudes() {
        val points = listOf(
            TrackPoint(altitude = 100.0, avgVertical = 1.0, avgHorizontal = 2.0),
            TrackPoint(altitude = 105.0, avgVertical = 2.0, avgHorizontal = 4.0), // +5 ascent
            TrackPoint(altitude = 102.0, avgVertical = 3.0, avgHorizontal = 6.0), // -3 descent
            TrackPoint(altitude = null, avgVertical = 4.0, avgHorizontal = 8.0),  // ignored for alt metrics
            TrackPoint(altitude = 110.0, avgVertical = 5.0, avgHorizontal = 10.0) // prev null -> ignored for ascent/descent
        )

        assertEquals(5.0, points.totalAscent(), "Total ascent should accumulate only positive gains with non-null prev+curr")
        assertEquals(3.0, points.totalDescent(), "Total descent should accumulate only losses with non-null prev+curr")
        assertEquals(110.0, points.maxAltitude(), "Max altitude should ignore nulls")
        assertEquals(100.0, points.minAltitude(), "Min altitude should ignore nulls")
        assertTrue(abs(points.avgVerticalSpeed() - 3.0) < 1e-9, "Average vertical speed over all points")
        assertTrue(abs(points.avgHorizontalSpeed() - 6.0) < 1e-9, "Average horizontal speed over all points")
    }

    @Test
    fun sessionMetrics_emptyList_returnsZeros() {
        val empty = emptyList<TrackPoint>()
        assertEquals(0.0, empty.totalAscent())
        assertEquals(0.0, empty.totalDescent())
        assertEquals(0.0, empty.maxAltitude())
        assertEquals(0.0, empty.minAltitude())
        assertEquals(0.0, empty.avgVerticalSpeed())
        assertEquals(0.0, empty.avgHorizontalSpeed())
    }

    @Test
    fun sessionMetrics_allNullAltitudes_returnsZeroForAltMetrics() {
        val list = listOf(
            TrackPoint(altitude = null, avgVertical = 1.0, avgHorizontal = 2.0),
            TrackPoint(altitude = null, avgVertical = 3.0, avgHorizontal = 4.0)
        )
        assertEquals(0.0, list.totalAscent())
        assertEquals(0.0, list.totalDescent())
        assertEquals(0.0, list.maxAltitude())
        assertEquals(0.0, list.minAltitude())
        assertTrue(abs(list.avgVerticalSpeed() - 2.0) < 1e-9)
        assertTrue(abs(list.avgHorizontalSpeed() - 3.0) < 1e-9)
    }

    @Test
    fun sessionMetrics_singleElement_edgeCases() {
        val one = listOf(TrackPoint(altitude = 250.0, avgVertical = 7.0, avgHorizontal = 14.0))
        assertEquals(0.0, one.totalAscent())
        assertEquals(0.0, one.totalDescent())
        assertEquals(250.0, one.maxAltitude())
        assertEquals(250.0, one.minAltitude())
        assertTrue(abs(one.avgVerticalSpeed() - 7.0) < 1e-9)
        assertTrue(abs(one.avgHorizontalSpeed() - 14.0) < 1e-9)
    }
}
