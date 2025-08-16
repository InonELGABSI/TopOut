package com.topout.kmp.models.sensor

data class LocationData(
    val lat: Double,
    val lon: Double,
    val altitude: Double,
    val speed: Float,
    val ts: Long
)