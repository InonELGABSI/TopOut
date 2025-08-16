package com.topout.kmp.models

import com.topout.kmp.utils.nowEpochMillis
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class User(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    @SerialName("img_url") val imgUrl: String? = null,

    @SerialName("unit_preference") val unitPreference: String? = "meters",
    @SerialName("enabled_notifications") val enabledNotifications: Boolean? = false,
    @SerialName("relative_height_from_start_thr") val relativeHeightFromStartThr: Double? = null,
    @SerialName("total_height_from_start_thr") val totalHeightFromStartThr: Double? = null,
    @SerialName("current_avg_height_speed_thr") val currentAvgHeightSpeedThr: Double? = null,

    @SerialName("user_updated_offline") val userUpdatedOffline: Boolean? = false,

    @SerialName("created_at") val createdAt: Long? = nowEpochMillis(),
    @SerialName("updated_at") val updatedAt: Long? = nowEpochMillis()
)
