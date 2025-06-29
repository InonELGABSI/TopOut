package com.topout.kmp.utils.providers

expect class BarometerProvider {
    suspend fun getPressureData(): Float // Returns atmospheric pressure in hPa
    suspend fun getAltitudeData(): Float // Returns altitude in meters based on pressure
}