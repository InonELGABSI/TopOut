package com.topout.kmp.domain

import com.topout.kmp.data.Result
import com.topout.kmp.data.Error
import com.topout.kmp.utils.providers.LocationProvider
import com.topout.kmp.utils.GeoidUtils
import co.touchlab.kermit.Logger

class GetCurrentMSLHeight(
    private val locationProvider: LocationProvider
) {
    private val log = Logger.withTag("GetCurrentMSLHeight")

    suspend operator fun invoke(): Result<MSLHeightData, Error> {
        return try {
            log.i { "Getting current MSL height with geoid corrections..." }

            val locationData = locationProvider.getLocation()
            val ellipsoidHeight = locationData.altitude
            val latitude = locationData.lat
            val longitude = locationData.lon

            val geoidHeight = GeoidUtils.calculateGeoidHeight(latitude, longitude)

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


data class MSLHeightData(
    val mslHeight: Double,
    val ellipsoidHeight: Double,
    val geoidHeight: Double,
    val latitude: Double,
    val longitude: Double,
    val accuracy: String
)


data class MSLHeightError(override val message: String) : Error
