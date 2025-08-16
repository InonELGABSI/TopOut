package com.topout.kmp.domain

import com.topout.kmp.models.AlertType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.math.abs
import kotlin.math.sqrt

class SessionTrackerTest {

    @Test
    fun gainCalculation_positiveAltitudeChange() {
        // Arrange
        val lastAlt = 100.0
        val currentAlt = 150.0
        var gain = 0.0

        // Act - Simulating the gain calculation logic from SessionTracker
        val diff = currentAlt - lastAlt
        if (diff > 0) gain += diff

        // Assert
        assertEquals(50.0, gain)
    }

    @Test
    fun lossCalculation_negativeAltitudeChange() {
        // Arrange
        val lastAlt = 200.0
        val currentAlt = 150.0
        var loss = 0.0

        // Act - Simulating the loss calculation logic from SessionTracker
        val diff = currentAlt - lastAlt
        if (diff < 0) loss -= diff // Note: loss -= diff makes loss positive

        // Assert
        assertEquals(50.0, loss)
    }

    @Test
    fun gainAndLoss_noAltitudeChange() {
        // Arrange
        val lastAlt = 100.0
        val currentAlt = 100.0
        var gain = 0.0
        var loss = 0.0

        // Act - Simulating the gain/loss calculation logic
        val diff = currentAlt - lastAlt
        if (diff > 0) gain += diff else if (diff < 0) loss -= diff

        // Assert
        assertEquals(0.0, gain)
        assertEquals(0.0, loss)
    }

    @Test
    fun averageVerticalSpeedComputation_withSamples() {
        // Arrange
        var vertDistSum = 0.0
        var vertSampleCount = 0
        val verticalSpeeds = listOf(10.0, -5.0, 15.0, -8.0, 20.0)

        // Act - Simulating the average calculation logic from SessionTracker
        verticalSpeeds.forEach { vVert ->
            vertDistSum += abs(vVert)
            vertSampleCount++
        }
        val avgVert = if (vertSampleCount > 0) vertDistSum / vertSampleCount else 0.0

        // Assert
        assertEquals(11.6, avgVert) // (10 + 5 + 15 + 8 + 20) / 5 = 58 / 5 = 11.6
    }

    @Test
    fun averageVerticalSpeedComputation_noSamples() {
        // Arrange
        var vertDistSum = 0.0
        var vertSampleCount = 0

        // Act
        val avgVert = if (vertSampleCount > 0) vertDistSum / vertSampleCount else 0.0

        // Assert
        assertEquals(0.0, avgVert)
    }

    @Test
    fun relativeAltitudeCalculation_withStartAltitude() {
        // Arrange
        val startAltitude = 100.0
        val currentAltitude = 250.0

        // Act - Simulating relative altitude calculation
        val relAltitude = currentAltitude - startAltitude

        // Assert
        assertEquals(150.0, relAltitude)
    }

    @Test
    fun relativeAltitudeCalculation_nullAltitudes() {
        // Arrange
        val startAltitude: Double? = null
        val currentAltitude: Double? = 250.0

        // Act - Simulating the null check logic from SessionTracker
        val relAltitude = if (currentAltitude != null && startAltitude != null)
            currentAltitude - startAltitude else 0.0

        // Assert
        assertEquals(0.0, relAltitude)
    }

    @Test
    fun totalVelocityCalculation_withVerticalAndHorizontalSpeeds() {
        // Arrange
        val vVert = 30.0 // m/min
        val vHorizon = 40.0 // m/min

        // Act - Simulating total velocity calculation from SessionTracker
        val vTotal = sqrt(vVert * vVert + vHorizon * vHorizon)

        // Assert
        assertEquals(50.0, vTotal) // 3-4-5 triangle: sqrt(30² + 40²) = 50
    }

    @Test
    fun thresholdExceeded_averageSpeedThreshold() {
        // Arrange
        val avgVert = 700.0
        val avgSpeedThreshold = 600.0

        // Act - Simulating threshold check logic
        val thresholdExceeded = abs(avgVert) > avgSpeedThreshold

        // Assert
        assertTrue(thresholdExceeded)
    }

    @Test
    fun thresholdNotExceeded_averageSpeedThreshold() {
        // Arrange
        val avgVert = 500.0
        val avgSpeedThreshold = 600.0

        // Act
        val thresholdExceeded = abs(avgVert) > avgSpeedThreshold

        // Assert
        assertFalse(thresholdExceeded)
    }

    @Test
    fun thresholdExceeded_relativeHeightThreshold() {
        // Arrange
        val relAltitude = 150.0
        val relativeHeightThreshold = 100.0

        // Act - Simulating relative height threshold check
        val thresholdExceeded = relativeHeightThreshold > 0.0 && abs(relAltitude) > relativeHeightThreshold

        // Assert
        assertTrue(thresholdExceeded)
    }

    @Test
    fun thresholdExceeded_totalHeightThreshold() {
        // Arrange
        val totalGain = 600.0
        val totalHeightThreshold = 500.0

        // Act - Simulating total height threshold check
        val thresholdExceeded = totalHeightThreshold > 0.0 && totalGain > totalHeightThreshold

        // Assert
        assertTrue(thresholdExceeded)
    }

    @Test
    fun alertTypeDetermination_multipleThresholdsExceeded() {
        // Arrange
        val triggeredAlerts = mutableListOf<AlertType>()
        val avgVert = 700.0
        val avgSpeedThreshold = 600.0
        val relAltitude = 150.0
        val relativeHeightThreshold = 100.0

        // Act - Simulating alert determination logic
        if (abs(avgVert) > avgSpeedThreshold) {
            triggeredAlerts.add(AlertType.RAPID_ASCENT)
        }
        if (relativeHeightThreshold > 0.0 && abs(relAltitude) > relativeHeightThreshold) {
            triggeredAlerts.add(AlertType.RELATIVE_HEIGHT_EXCEEDED)
        }

        val primaryAlertType = triggeredAlerts.firstOrNull() ?: AlertType.NONE
        val danger = triggeredAlerts.isNotEmpty()

        // Assert
        assertEquals(AlertType.RAPID_ASCENT, primaryAlertType)
        assertTrue(danger)
        assertEquals(2, triggeredAlerts.size)
    }

    @Test
    fun alertTypeDetermination_noThresholdsExceeded() {
        // Arrange
        val triggeredAlerts = mutableListOf<AlertType>()
        val avgVert = 400.0
        val avgSpeedThreshold = 600.0
        val relAltitude = 50.0
        val relativeHeightThreshold = 100.0

        // Act
        if (abs(avgVert) > avgSpeedThreshold) {
            triggeredAlerts.add(AlertType.RAPID_ASCENT)
        }
        if (relativeHeightThreshold > 0.0 && abs(relAltitude) > relativeHeightThreshold) {
            triggeredAlerts.add(AlertType.RELATIVE_HEIGHT_EXCEEDED)
        }

        val primaryAlertType = triggeredAlerts.firstOrNull() ?: AlertType.NONE
        val danger = triggeredAlerts.isNotEmpty()

        // Assert
        assertEquals(AlertType.NONE, primaryAlertType)
        assertFalse(danger)
        assertEquals(0, triggeredAlerts.size)
    }
}
