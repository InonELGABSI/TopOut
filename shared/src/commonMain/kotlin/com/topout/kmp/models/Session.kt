package com.topout.kmp.models

import com.topout.kmp.utils.generateId
import com.topout.kmp.utils.nowEpochMillis
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String = generateId(),

    @SerialName("user_id")
    val userId: String? = "",

    val title: String? = "",

    @SerialName("start_time")
    val startTime: Long? = nowEpochMillis(),

    @SerialName("end_time")
    val endTime: Long? = null,

    @SerialName("total_ascent")
    val totalAscent: Double? = 0.0,

    @SerialName("total_descent")
    val totalDescent: Double? = 0.0,

    @SerialName("max_altitude")
    val maxAltitude: Double? = 0.0,

    @SerialName("min_altitude")
    val minAltitude: Double? = 0.0,

    @SerialName("avg_rate")
    val avgRate: Double? = 0.0,

    @SerialName("alert_triggered")
    val alertTriggered: Long? = 0,

    @SerialName("created_at")
    val createdAt: Long? = nowEpochMillis(),

    @SerialName("updated_at")
    val updatedAt: Long? = nowEpochMillis(),

    @SerialName("session_deleted_offline")
    val sessionDeletedOffline: Boolean = false,

    @SerialName("session_created_offline")
    val sessionCreatedOffline: Boolean = false,

    @SerialName("session_updated_offline")
    val sessionUpdatedOffline: Boolean = false,
)
