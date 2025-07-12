package com.topout.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class TrackPoint(
    val id: Long = 0L,                // מזהה ייחודי
    val sessionId: String = "",       // מזהה סשן
    val timestamp: Long = 0L,         // זמן הדגימה

    // Raw sensor data
    val latitude: Double? = null,     // GPS lat
    val longitude: Double? = null,    // GPS lon
    val altitude: Double? = null,     // Barometer/GPS altitude
    val accelerationX: Float? = null, // Accelerometer X
    val accelerationY: Float? = null, // Accelerometer Y
    val accelerationZ: Float? = null, // Accelerometer Z

    // Calculated metrics
    val vVertical: Double = 0.0,       //
    val vHorizontal: Double = 0.0,     //
    val vTotal: Double = 0.0,          // √(v_h²+v_v²)

    val gain: Double = 0.0,            // Σ העליות
    val loss: Double = 0.0,            // Σ הירידות
    val relAltitude: Double = 0.0,     // hᵢ − h₀

    val avgVertical: Double = 0.0,     // avgV
    val danger: Boolean = false,       // true אם אחד הספים חצה
    val alertType: AlertType = AlertType.NONE
)
