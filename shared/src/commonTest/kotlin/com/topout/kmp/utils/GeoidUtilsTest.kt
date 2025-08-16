package com.topout.kmp.utils

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class GeoidUtilsTest {

    @Test
    fun calculateGeoidHeight_indianOceanLow_isSignificantlyNegative() {
        val h = GeoidUtils.calculateGeoidHeight(-20.0, 80.0) // Indian Ocean low
        assertTrue(h < -30.0, "Expected Indian Ocean geoid height to be < -30m, was $h")
    }

    @Test
    fun calculateGeoidHeight_himalayaHigh_isPositiveAndLarge() {
        val h = GeoidUtils.calculateGeoidHeight(30.0, 80.0) // Himalaya region
        assertTrue(h > 20.0, "Expected Himalayan geoid height to be > 20m, was $h")
    }

    @Test
    fun calculateGeoidHeight_defaultRegion_withinRange() {
        val h = GeoidUtils.calculateGeoidHeight(50.0, 100.0) // Should hit default mid-latitude path
        assertTrue(h in -110.0..85.0, "Geoid height should be within clamp range, was $h")
    }

    @Test
    fun calculateGeoidHeight_variousCoordinates_alwaysWithinClampRange() {
        val coords = listOf(
            0.0 to 0.0,
            89.0 to 179.0,
            -89.0 to -179.0,
            72.0 to -30.0, // Arctic region
            -70.0 to 40.0  // Antarctic region
        )
        coords.forEach { (lat, lon) ->
            val h = GeoidUtils.calculateGeoidHeight(lat, lon)
            assertTrue(h in -110.0..85.0, "Clamped geoid height out of range for ($lat,$lon): $h")
        }
    }

    @Test
    fun ellipsoidToMSL_roundingBehavior() {
        val lat = 0.0
        val lon = 0.0
        val geoid = GeoidUtils.calculateGeoidHeight(lat, lon)

        val raw1 = geoid + 5.4
        val msl1 = GeoidUtils.ellipsoidToMSL(raw1, lat, lon)
        assertEquals(5.0, msl1, "5.4 above geoid should round to 5.0")

        val raw2 = geoid + 5.5
        val msl2 = GeoidUtils.ellipsoidToMSL(raw2, lat, lon)
        assertEquals(6.0, msl2, "5.5 above geoid should round to 6.0")

        val raw3 = geoid - 3.2
        val msl3 = GeoidUtils.ellipsoidToMSL(raw3, lat, lon)
        assertEquals(-3.0, msl3, "-3.2 above geoid should round to -3.0")

        val raw4 = geoid - 3.6
        val msl4 = GeoidUtils.ellipsoidToMSL(raw4, lat, lon)
        assertEquals(-4.0, msl4, "-3.6 above geoid should round to -4.0")
    }
}

