package com.topout.kmp.models

import com.topout.kmp.utils.nowEpochMillis
import kotlinx.serialization.Serializable

@Serializable
data class TrackPoint(
    val id: Long = nowEpochMillis(),
    val sessionId: String = "",
    val timestamp: Long = nowEpochMillis(),

    // Raw sensor data
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val accelerationX: Float? = null,
    val accelerationY: Float? = null,
    val accelerationZ: Float? = null,

    val gain: Double = 0.0,
    val loss: Double = 0.0,
    val relAltitude: Double = 0.0,

    val avgVertical: Double = 0.0,
    val avgHorizontal: Double = 0.0,
    val danger: Boolean = false,
    val alertType: AlertType = AlertType.NONE
)
