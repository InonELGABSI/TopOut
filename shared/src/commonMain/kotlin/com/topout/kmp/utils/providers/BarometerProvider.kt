package com.topout.kmp.utils.providers
import com.topout.kmp.models.sensor.AltitudeData

expect class BarometerProvider {
    suspend fun getBarometerReading(): AltitudeData   // altitude + pressure + ts
}