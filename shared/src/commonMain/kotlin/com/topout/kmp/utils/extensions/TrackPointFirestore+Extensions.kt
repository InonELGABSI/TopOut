package com.topout.kmp.utils.extensions

import com.topout.kmp.models.AlertType
import com.topout.kmp.models.TrackPoint
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Timestamp

/* ---------- helpers ---------- */
private fun Any?.toMillis(): Long = when (this) {
    is Timestamp -> this.toEpochMillis()
    is Long      -> this
    else         -> 0L
}

/* ---------- read ---------- */
fun DocumentSnapshot.toTrackPoint(): TrackPoint {
    // Raw fields (firebase-kotlin-sdk maps simple numbers & strings automatically)
    val base = data<TrackPoint>() ?: error("Invalid TrackPoint data")

    return base.copy(
        timestamp  = get<Any?>("timestamp").toMillis()
    )
}

/* ---------- write ---------- */
fun TrackPoint.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id"            to id,
    "sessionId"     to sessionId,
    "timestamp"     to timestamp.toTimestamp(),  // millis ➜ Timestamp
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
