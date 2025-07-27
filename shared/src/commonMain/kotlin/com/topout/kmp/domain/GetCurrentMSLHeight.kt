package com.topout.kmp.domain

import com.topout.kmp.data.Result
import com.topout.kmp.data.Error
import com.topout.kmp.utils.providers.LocationProvider
import com.topout.kmp.utils.GeoidUtils
import co.touchlab.kermit.Logger

/**
 * MSL Height calculator using GeoidUtils for accurate conversions
 * from GPS ellipsoidal heights to true Mean Sea Level heights.
 */
class GetCurrentMSLHeight(
    private val locationProvider: LocationProvider
) {
    private val log = Logger.withTag("GetCurrentMSLHeight")

    /**
     * Gets the current height above mean sea level (MSL) using GPS altitude data
     * and geoid model corrections for accurate MSL calculations.
     *
     * @return Result containing the accurate MSL height in meters, or an error if location cannot be obtained
     */
    suspend operator fun invoke(): Result<MSLHeightData, Error> {
        return try {
            log.i { "Getting current MSL height with geoid corrections..." }

            val locationData = locationProvider.getLocation()
            val ellipsoidHeight = locationData.altitude
            val latitude = locationData.lat
            val longitude = locationData.lon

            // Calculate geoid height using common utility
            val geoidHeight = GeoidUtils.calculateGeoidHeight(latitude, longitude)

            // Convert from ellipsoid height to MSL height
            val mslHeight = GeoidUtils.ellipsoidToMSL(ellipsoidHeight, latitude, longitude)

            val accuracy = "EGM96-based geoid approximation (±3-5m accuracy)"

            log.i {
                "MSL calculation: Ellipsoid=${ellipsoidHeight}m, Geoid=${geoidHeight}m, MSL=${mslHeight}m at (${latitude}, ${longitude})"
            }

            Result.Success(
                MSLHeightData(
                    mslHeight = mslHeight,
                    ellipsoidHeight = ellipsoidHeight,
                    geoidHeight = geoidHeight,
                    latitude = latitude,
                    longitude = longitude,
                    accuracy = accuracy
                )
            )

        } catch (exception: Exception) {
            val errorMessage = when {
                exception.message?.contains("permission", ignoreCase = true) == true ->
                    "Location permission not granted"
                exception.message?.contains("Location services disabled", ignoreCase = true) == true ->
                    "Location services are disabled"
                exception.message?.contains("No location available", ignoreCase = true) == true ->
                    "No location data available"
                else -> "Failed to get MSL height: ${exception.message}"
            }

            log.e(exception) { errorMessage }
            Result.Failure(MSLHeightError(errorMessage))
        }
    }
}

/**
 * Data class containing MSL height information
 */
data class MSLHeightData(
    val mslHeight: Double,           // Height above mean sea level in meters
    val ellipsoidHeight: Double,     // Original GPS ellipsoid height in meters
    val geoidHeight: Double,         // Geoid height (geoid above ellipsoid) in meters
    val latitude: Double,            // Latitude where measurement was taken
    val longitude: Double,           // Longitude where measurement was taken
    val accuracy: String             // Accuracy information about the geoid model used
)

/**
 * Error class for MSL height calculation failures
 */
data class MSLHeightError(override val message: String) : Error
