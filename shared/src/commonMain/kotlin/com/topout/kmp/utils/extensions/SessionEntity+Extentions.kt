package com.topout.kmp.utils.extensions

import com.topout.kmp.SessionEntity
import com.topout.kmp.models.Session

fun SessionEntity.toSession(): Session {
    return Session(
        id = id.toInt(),
        userId = userId,
        title = title,
        startTime = startTime,
        endTime = endTime,
        totalAscent = totalAscent ?: 0.0,
        totalDescent = totalDescent ?: 0.0,
        maxAltitude = maxAltitude ?: 0.0,
        minAltitude = minAltitude ?: 0.0,
        avgRate = avgRate ?: 0.0,
        alertTriggered = alertTriggered,
        createdAt = createdAt,
        graphImageUrl = graphImageUrl
    )
}