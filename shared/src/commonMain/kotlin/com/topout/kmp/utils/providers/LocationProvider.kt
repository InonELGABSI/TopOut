package com.topout.kmp.utils.providers
import com.topout.kmp.models.sensor.LocationData

expect class LocationProvider {
    suspend fun getLocation(): LocationData          // lat, lon, alt, speed, ts
}