package com.topout.kmp.utils.providers
import com.topout.kmp.models.sensor.AccelerationData

expect class AccelerometerProvider {
    suspend fun getAcceleration(): AccelerationData
}
