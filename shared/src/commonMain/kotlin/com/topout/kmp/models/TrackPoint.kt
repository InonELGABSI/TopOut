package com.topout.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class TrackPoint(
    val timestamp: Long,          // epoch millis
    val altitude: Double,         // m  (hᵢ)
    val latitude: Double,
    val longitude: Double,

    val verticalRate: Double = 0.0,    // vᵢ  m/min
    val horizontalSpeed: Double = 0.0, // v_h m/s
    val accuracy: Float? = null
)
