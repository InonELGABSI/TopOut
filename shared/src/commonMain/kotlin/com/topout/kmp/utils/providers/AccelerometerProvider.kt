package com.topout.kmp.utils.providers

expect class AccelerometerProvider {
    suspend fun getAccelerometerData(): List<Float>   // [x, y, z]
}
