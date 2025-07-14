package com.topout.kmp.models

import com.topout.kmp.utils.nowEpochMillis
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val imgUrl: String? = null,

    val unitPreference: String? = "meters",
    val enabledNotifications: Boolean? = false,
    val relativeHeightFromStartThr: Double? = 0.0,
    val totalHeightFromStartThr: Double? = 0.0,
    val currentAvgHeightSpeedThr: Double? = 0.0,

    val createdAt: Long? = nowEpochMillis()
)
