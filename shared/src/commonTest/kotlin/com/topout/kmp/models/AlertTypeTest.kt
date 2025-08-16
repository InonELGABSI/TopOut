package com.topout.kmp.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AlertTypeTest {

    @Test
    fun alertType_allExpectedValuesExist() {
        // Arrange & Act
        val alertTypes = AlertType.values()

        // Assert
        assertEquals(5, alertTypes.size)
        assertTrue(alertTypes.contains(AlertType.NONE))
        assertTrue(alertTypes.contains(AlertType.RAPID_ASCENT))
        assertTrue(alertTypes.contains(AlertType.RAPID_DESCENT))
        assertTrue(alertTypes.contains(AlertType.RELATIVE_HEIGHT_EXCEEDED))
        assertTrue(alertTypes.contains(AlertType.TOTAL_HEIGHT_EXCEEDED))
    }

    @Test
    fun alertType_valuesAreNotNull() {
        // Act & Assert
        assertNotNull(AlertType.NONE)
        assertNotNull(AlertType.RAPID_ASCENT)
        assertNotNull(AlertType.RAPID_DESCENT)
        assertNotNull(AlertType.RELATIVE_HEIGHT_EXCEEDED)
        assertNotNull(AlertType.TOTAL_HEIGHT_EXCEEDED)
    }

    @Test
    fun alertType_enumOrdinalValues() {
        // Act & Assert
        assertEquals(0, AlertType.NONE.ordinal)
        assertEquals(1, AlertType.RAPID_ASCENT.ordinal)
        assertEquals(2, AlertType.RAPID_DESCENT.ordinal)
        assertEquals(3, AlertType.RELATIVE_HEIGHT_EXCEEDED.ordinal)
        assertEquals(4, AlertType.TOTAL_HEIGHT_EXCEEDED.ordinal)
    }

    @Test
    fun alertType_enumNameValues() {
        // Act & Assert
        assertEquals("NONE", AlertType.NONE.name)
        assertEquals("RAPID_ASCENT", AlertType.RAPID_ASCENT.name)
        assertEquals("RAPID_DESCENT", AlertType.RAPID_DESCENT.name)
        assertEquals("RELATIVE_HEIGHT_EXCEEDED", AlertType.RELATIVE_HEIGHT_EXCEEDED.name)
        assertEquals("TOTAL_HEIGHT_EXCEEDED", AlertType.TOTAL_HEIGHT_EXCEEDED.name)
    }

    @Test
    fun alertType_valueOfWorksCorrectly() {
        // Act & Assert
        assertEquals(AlertType.NONE, AlertType.valueOf("NONE"))
        assertEquals(AlertType.RAPID_ASCENT, AlertType.valueOf("RAPID_ASCENT"))
        assertEquals(AlertType.RAPID_DESCENT, AlertType.valueOf("RAPID_DESCENT"))
        assertEquals(AlertType.RELATIVE_HEIGHT_EXCEEDED, AlertType.valueOf("RELATIVE_HEIGHT_EXCEEDED"))
        assertEquals(AlertType.TOTAL_HEIGHT_EXCEEDED, AlertType.valueOf("TOTAL_HEIGHT_EXCEEDED"))
    }

    @Test
    fun alertType_canBeUsedInWhenStatement() {
        // Arrange
        val alertType = AlertType.RAPID_ASCENT

        // Act
        val result = when (alertType) {
            AlertType.NONE -> "No alert"
            AlertType.RAPID_ASCENT -> "Climbing too fast"
            AlertType.RAPID_DESCENT -> "Descending too fast"
            AlertType.RELATIVE_HEIGHT_EXCEEDED -> "Too high from start"
            AlertType.TOTAL_HEIGHT_EXCEEDED -> "Total climb too high"
        }

        // Assert
        assertEquals("Climbing too fast", result)
    }

    @Test
    fun alertType_differentInstancesAreEqual() {
        // Arrange
        val alert1 = AlertType.RAPID_ASCENT
        val alert2 = AlertType.valueOf("RAPID_ASCENT")

        // Act & Assert
        assertEquals(alert1, alert2)
        assertEquals(alert1.hashCode(), alert2.hashCode())
    }
}
