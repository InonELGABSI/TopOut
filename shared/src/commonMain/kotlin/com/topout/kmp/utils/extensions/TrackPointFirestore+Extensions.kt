package com.topout.kmp.utils.extensions

import com.topout.kmp.models.TrackPoint
import dev.gitlive.firebase.firestore.Timestamp

/* TrackPoint ➜ Map ready for Firestore write */
fun TrackPoint.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id"            to id,
    "sessionId"     to sessionId,
    "timestamp"     to timestamp.toTimestamp(),
    "latitude"      to latitude,
    "longitude"     to longitude,
    "altitude"      to altitude,
    "accelerationX" to accelerationX,
    "accelerationY" to accelerationY,
    "accelerationZ" to accelerationZ,
    "vVertical"     to vVertical,
    "vHorizontal"   to vHorizontal,
    "vTotal"        to vTotal,
    "gain"          to gain,
    "loss"          to loss,
    "relAltitude"   to relAltitude,
    "avgVertical"   to avgVertical,
    "danger"        to danger,
    "alertType"     to alertType.name
)
