package com.topout.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class Metrics(
    val gain: Double = 0.0,
    val loss: Double = 0.0,
    val relAltitude: Double = 0.0,

    val avgHorizontal: Double = 0.0,
    val avgVertical: Double = 0.0,
    val danger: Boolean = false,
    val alertType: AlertType = AlertType.NONE
)
