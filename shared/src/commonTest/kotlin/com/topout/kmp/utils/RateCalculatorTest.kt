package com.topout.kmp.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCalculatorTest {

    @Test
    fun verticalSpeedMetersPerMinute_correctSpeedBetweenTwoAltitudes() {
        // Arrange
        val previousAltitude = 100.0
        val currentAltitude = 150.0

        // Act
        val result = RateCalculator.verticalSpeedMetersPerMinute(previousAltitude, currentAltitude)

        // Assert
        assertEquals(3000.0, result) // 50m difference * 60 = 3000 m/min
    }

    @Test
    fun verticalSpeedMetersPerMinute_noChangeInAltitude_speedIsZero() {
        // Arrange
        val previousAltitude = 200.0
        val currentAltitude = 200.0

        // Act
        val result = RateCalculator.verticalSpeedMetersPerMinute(previousAltitude, currentAltitude)

        // Assert
        assertEquals(0.0, result)
    }

    @Test
    fun verticalSpeedMetersPerMinute_negativeAltitudeChange_negativeSpeed() {
        // Arrange
        val previousAltitude = 300.0
        val currentAltitude = 250.0

        // Act
        val result = RateCalculator.verticalSpeedMetersPerMinute(previousAltitude, currentAltitude)

        // Assert
        assertEquals(-3000.0, result) // -50m difference * 60 = -3000 m/min
    }

    @Test
    fun verticalSpeedMetersPerMinute_largeAltitudeJump_largeSpeed() {
        // Arrange
        val previousAltitude = 1000.0
        val currentAltitude = 1500.0

        // Act
        val result = RateCalculator.verticalSpeedMetersPerMinute(previousAltitude, currentAltitude)

        // Assert
        assertEquals(30000.0, result) // 500m difference * 60 = 30000 m/min
    }

    @Test
    fun verticalSpeedMetersPerMinute_smallPreciseChanges() {
        // Arrange
        val previousAltitude = 100.5
        val currentAltitude = 101.2

        // Act
        val result = RateCalculator.verticalSpeedMetersPerMinute(previousAltitude, currentAltitude)

        // Assert
        assertEquals(42.0, result, 0.0001)
        // 0.7m difference * 60 = 42.0 m/min
    }
}
