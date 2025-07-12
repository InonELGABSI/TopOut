package com.topout.kmp.utils.extensions

import com.topout.kmp.Track_points
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.models.AlertType

fun Track_points.toTrackPoint(): TrackPoint {
    return TrackPoint(
        id = id,
        sessionId = sessionId.toString(),
        timestamp = ts,
        latitude = lat,
        longitude = lon,
        altitude = altitude,
        accelerationX = accelX?.toFloat(),
        accelerationY = accelY?.toFloat(),
        accelerationZ = accelZ?.toFloat(),
        vVertical = vVertical ?: 0.0,
        vHorizontal = vHorizontal ?: 0.0,
        vTotal = vTotal ?: 0.0,
        gain = gain ?: 0.0,
        loss = loss ?: 0.0,
        relAltitude = relAltitude ?: 0.0,
        avgVertical = avgVertical ?: 0.0,
        danger = danger == 1L,
        alertType = AlertType.valueOf(alertType ?: "NONE")
    )
}
