package com.topout.kmp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Session(

    val id: Int?,

    @SerialName("user_id")
    val userId: String?,

    val title: String? = "",

    @SerialName("start_time")
    val startTime: Long? = 0L,

    @SerialName("end_time")
    val endTime: Long? = 0L,

    @SerialName("total_ascent")
    val totalAscent: Double? = 0.0,     // gain
    @SerialName("total_descent")
    val totalDescent: Double? = 0.0,    // loss
    @SerialName("max_altitude")
    val maxAltitude: Double? = 0.0,
    @SerialName("min_altitude")
    val minAltitude: Double? = 0.0,
    @SerialName("avg_rate")
    val avgRate: Double? = 0.0,         // avgV

    @SerialName("alert_triggered")
    val alertTriggered: Boolean? = false,
    @SerialName("created_at")
    val createdAt: Long? = 0L,
    @SerialName("graph_image_url")
    val graphImageUrl: String? = null
)
