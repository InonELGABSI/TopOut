package com.topout.kmp.utils

object RateCalculator {

    /**
     * Calculates vertical speed in meters per minute
     * @param previousAltitude Previous altitude in meters
     * @param currentAltitude Current altitude in meters
     * @return Vertical speed in meters per minute
     */
    fun verticalSpeedMetersPerMinute(previousAltitude: Double, currentAltitude: Double): Double {
        // Assuming 1-second interval between samples
        val altitudeDiff = currentAltitude - previousAltitude
        // Convert from m/s to m/min by multiplying by 60
        return altitudeDiff * 60.0
    }
}

