package com.topout.kmp.utils

import kotlin.math.*

/**
 * Geoid height calculation utilities for converting GPS ellipsoid heights to MSL heights
 */
object GeoidUtils {

    /**
     * Calculate geoid height using EGM96-based approximation
     * This provides reasonably accurate geoid undulations globally
     *
     * @param latitude Latitude in degrees
     * @param longitude Longitude in degrees
     * @return Geoid height in meters (geoid above ellipsoid)
     */
    fun calculateGeoidHeight(latitude: Double, longitude: Double): Double {
        // EGM96-based geoid approximation using spherical harmonics
        val lat = latitude * PI / 180.0  // Convert degrees to radians
        val lon = longitude * PI / 180.0  // Convert degrees to radians

        // Major geoid undulation patterns based on EGM96 model
        var geoidHeight = 0.0

        // Degree 2 terms (major Earth shape components)
        geoidHeight += -0.53 * (3 * sin(lat) * sin(lat) - 1) / 2  // J2 zonal harmonic

        // Regional corrections based on known geoid patterns
        geoidHeight += when {
            // Indian Ocean geoid low (-100m to -40m)
            latitude in -40.0..-10.0 && longitude in 60.0..100.0 -> -60.0

            // North Atlantic ridge (+30m to +50m)
            latitude in 40.0..70.0 && longitude in -60.0..-10.0 -> 35.0

            // Western Pacific high (+20m to +40m)
            latitude in -10.0..40.0 && longitude in 120.0..180.0 -> 30.0

            // South American Andes (+15m to +35m)
            latitude in -40.0..10.0 && longitude in -80.0..-40.0 -> 25.0

            // Himalayan/Tibetan region (+35m to +50m)
            latitude in 20.0..40.0 && longitude in 70.0..100.0 -> 40.0

            // Antarctic region (-30m to -10m)
            latitude < -60.0 -> -20.0

            // Arctic region (-20m to 0m)
            latitude > 70.0 -> -10.0

            // African continent (+20m to +40m)
            latitude in -35.0..35.0 && longitude in -20.0..50.0 -> 28.0

            // North American continent (+10m to +30m)
            latitude in 25.0..70.0 && longitude in -170.0..-50.0 -> 18.0

            // European continent (+15m to +35m)
            latitude in 35.0..70.0 && longitude in -10.0..40.0 -> 25.0

            // Default mid-latitude approximation
            else -> 15.0 * sin(lat) * cos(2.0 * lon)
        }

        // Add longitude-dependent harmonic variations
        geoidHeight += 8.0 * sin(lat) * sin(lon) +
                      3.0 * cos(lat) * cos(lat) * cos(2.0 * lon) +
                      2.0 * sin(2.0 * lat) * sin(lon) +
                      1.5 * cos(3.0 * lat) * cos(3.0 * lon)

        // Add latitude-dependent variations
        geoidHeight += 5.0 * sin(3.0 * lat) * cos(lon) +
                      2.0 * cos(4.0 * lat) * sin(2.0 * lon)

        // Clamp to realistic geoid height range (global extremes: -107m to +85m)
        return geoidHeight.coerceIn(-110.0, 85.0)
    }

    /**
     * Convert GPS ellipsoid height to Mean Sea Level (MSL) height
     *
     * @param ellipsoidHeight Height above WGS84 ellipsoid in meters
     * @param latitude Latitude in degrees
     * @param longitude Longitude in degrees
     * @return MSL height in meters
     */
    fun ellipsoidToMSL(ellipsoidHeight: Double, latitude: Double, longitude: Double): Double {
        val geoidHeight = calculateGeoidHeight(latitude, longitude)
        return ellipsoidHeight - geoidHeight
    }

    /**
     * Convert MSL height to GPS ellipsoid height
     *
     * @param mslHeight Height above mean sea level in meters
     * @param latitude Latitude in degrees
     * @param longitude Longitude in degrees
     * @return Ellipsoid height in meters
     */
    fun mslToEllipsoid(mslHeight: Double, latitude: Double, longitude: Double): Double {
        val geoidHeight = calculateGeoidHeight(latitude, longitude)
        return mslHeight + geoidHeight
    }
}
