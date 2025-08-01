package com.topout.kmp.utils.extensions

import com.topout.kmp.SessionEntity         // generated by SQLDelight
import com.topout.kmp.models.Session

/* DB row ➜ domain */
fun SessionEntity.toSession(): Session = Session(
    id                 = id,
    userId             = userId,
    title              = title,
    startTime          = startTime,
    endTime            = endTime,
    totalAscent        = totalAscent ?: 0.0,
    totalDescent       = totalDescent ?: 0.0,
    maxAltitude        = maxAltitude ?: 0.0,
    minAltitude        = minAltitude ?: 0.0,
    avgRate            = avgRate ?: 0.0,
    alertTriggered     = alertTriggered,
    createdAt          = createdAt,
    updatedAt          = updatedAt,
    sessionDeletedOffline = sessionDeletedOffline == 1L,
    sessionCreatedOffline = sessionCreatedOffline == 1L,
    sessionUpdatedOffline = sessionUpdatedOffline == 1L,

)

/* domain ➜ DB row */
fun Session.toEntity(): SessionEntity = SessionEntity(
    id                 = id,
    userId             = userId ?: "",
    title              = title ?: "",
    startTime          = startTime,
    endTime            = endTime,
    totalAscent        = totalAscent,
    totalDescent       = totalDescent,
    maxAltitude        = maxAltitude,
    minAltitude        = minAltitude,
    avgRate            = avgRate,
    alertTriggered     = alertTriggered ?: 0,
    createdAt          = createdAt,
    updatedAt          = updatedAt,
    sessionDeletedOffline = if (sessionDeletedOffline) 1L else 0L,
    sessionCreatedOffline = if (sessionCreatedOffline) 1L else 0L,
    sessionUpdatedOffline = if (sessionUpdatedOffline) 1L else 0L
)
