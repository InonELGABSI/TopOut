package com.topout.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class Metrics(
    val gain: Double = 0.0,            // Σ העליות
    val loss: Double = 0.0,            // Σ הירידות
    val relAltitude: Double = 0.0,     // hᵢ − h₀

    val avgHorizontal: Double = 0.0,   // avgH
    val avgVertical: Double = 0.0,     // avgV
    val danger: Boolean = false,       // true אם אחד הספים חצה
    val alertType: AlertType = AlertType.NONE
)
