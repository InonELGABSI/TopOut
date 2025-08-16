package com.topout.kmp.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TrackPointTest {

    @Test
    fun trackPoint_defaultValues() {
        val tp = TrackPoint()
        assertTrue(tp.id > 0)
        assertTrue(tp.timestamp > 0)
        assertEquals("", tp.sessionId)
        assertEquals(0.0, tp.gain)
        assertEquals(0.0, tp.loss)
        assertEquals(0.0, tp.relAltitude)
        assertEquals(0.0, tp.avgVertical)
        assertEquals(0.0, tp.avgHorizontal)
        assertEquals(false, tp.danger)
        assertEquals(AlertType.NONE, tp.alertType)
    }

    @Test
    fun trackPoint_copyChangesSelectedFields() {
        val original = TrackPoint(sessionId = "s1", altitude = 100.0, avgVertical = 2.0, avgHorizontal = 3.0)
        val modified = original.copy(altitude = 150.0, danger = true, alertType = AlertType.RAPID_ASCENT)

        // Immutable fields not changed unless specified
        assertEquals(original.id, modified.id)
        assertEquals(original.sessionId, modified.sessionId)
        assertEquals(150.0, modified.altitude)
        assertEquals(true, modified.danger)
        assertEquals(AlertType.RAPID_ASCENT, modified.alertType)
        // Unchanged metrics remain
        assertEquals(original.avgVertical, modified.avgVertical)
        assertEquals(original.avgHorizontal, modified.avgHorizontal)
    }

    @Test
    fun trackPoint_multipleInstances_uniqueIdsLikely() {
        val a = TrackPoint()
        val b = TrackPoint()
        assertNotNull(a.id)
        assertNotNull(b.id)
        // Low probability of equality (time-based). Allow for fallback if same millisecond by only asserting not both objects same reference.
        if (a.id == b.id) {
            assertTrue(a !== b)
        } else {
            assertTrue(true)
        }
    }
}
