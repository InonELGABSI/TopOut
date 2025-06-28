package com.topout.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class Metrics(
    val vVertical: Double = 0.0,       // m/min   –  vᵢ
    val vHorizontal: Double = 0.0,     // m/s
    val vTotal: Double = 0.0,          // √(v_h²+v_v²)

    val gain: Double = 0.0,            // Σ העליות
    val loss: Double = 0.0,            // Σ הירידות
    val relAltitude: Double = 0.0,     // hᵢ − h₀

    val avgVertical: Double = 0.0,     // avgV
    val danger: Boolean = false,       // true אם אחד הספים חצה
    val alertType: AlertType = AlertType.NONE
)
