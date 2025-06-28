package com.topout.kmp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DangerSettings(
    val enabledTriggers: Set<DangerTrigger> = setOf(DangerTrigger.INSTANT_RATE),
    val instantRateThr: Double = 10.0,    // m/min
    val averageRateThr: Double = 6.0,     // m/min
    val relativeAltThr: Double = 500.0,   // m
    val totalGainThr:  Double = 800.0     // m
)
