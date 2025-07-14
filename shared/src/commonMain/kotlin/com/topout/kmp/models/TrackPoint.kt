package com.topout.kmp.models

import com.topout.kmp.utils.nowEpochMillis
import kotlinx.serialization.Serializable

@Serializable
data class TrackPoint(
    val id: Long = nowEpochMillis(),           // Unique ID for point (default: timestamp)
    val sessionId: String = "",                // Session ID
    val timestamp: Long = nowEpochMillis(),    // Sample time, milliseconds since epoch

    // Raw sensor data
    val latitude: Double? = null,              // GPS lat
    val longitude: Double? = null,             // GPS lon
    val altitude: Double? = null,              // Barometer/GPS altitude
    val accelerationX: Float? = null,          // Accelerometer X
    val accelerationY: Float? = null,          // Accelerometer Y
    val accelerationZ: Float? = null,          // Accelerometer Z

    // Calculated metrics
    val vVertical: Double = 0.0,               // Vertical speed
    val vHorizontal: Double = 0.0,             // Horizontal speed
    val vTotal: Double = 0.0,                  // Total speed

    val gain: Double = 0.0,                    // Σ העליות
    val loss: Double = 0.0,                    // Σ הירידות
    val relAltitude: Double = 0.0,             // hᵢ − h₀

    val avgVertical: Double = 0.0,             // avgV
    val danger: Boolean = false,               // Danger threshold crossed
    val alertType: AlertType = AlertType.NONE  // Alert type
)
