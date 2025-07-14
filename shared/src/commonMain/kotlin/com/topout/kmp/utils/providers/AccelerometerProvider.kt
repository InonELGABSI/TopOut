package com.topout.kmp.utils.providers
import com.topout.kmp.models.sensor.AccelerationData

expect class AccelerometerProvider {
    /** Single, one-shot read – handy for suspend & forget. */
    suspend fun getAcceleration(): AccelerationData
}
