package com.topout.kmp.utils.providers

expect class GPSProvider {
    suspend fun getGPSData(): List<Double> // [latitude, longitude, altitude]
}