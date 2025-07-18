package com.topout.kmp.utils.extensions


import com.topout.kmp.models.AlertType
import com.topout.kmp.models.TrackPoint
import dev.gitlive.firebase.firestore.DocumentSnapshot
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

/* DocumentSnapshot ➜ TrackPoint */
fun DocumentSnapshot.toTrackPoint(): TrackPoint {
    return TrackPoint(
        id = get<Long?>("id") ?: 0L,
        sessionId = get<String?>("sessionId") ?: "",
        timestamp = get<Timestamp?>("timestamp")?.toEpochMillis() ?: 0L,
        latitude = get<Double?>("latitude"),
        longitude = get<Double?>("longitude"),
        altitude = get<Double?>("altitude"),
        accelerationX = (get<Double?>("accelerationX") ?: 0.0).toFloat(),
        accelerationY = (get<Double?>("accelerationY") ?: 0.0).toFloat(),
        accelerationZ = (get<Double?>("accelerationZ") ?: 0.0).toFloat(),
        vVertical = get<Double?>("vVertical") ?: 0.0,
        vHorizontal = get<Double?>("vHorizontal") ?: 0.0,
        vTotal = get<Double?>("vTotal") ?: 0.0,
        gain = get<Double?>("gain") ?: 0.0,
        loss = get<Double?>("loss") ?: 0.0,
        relAltitude = get<Double?>("relAltitude") ?: 0.0,
        avgVertical = get<Double?>("avgVertical") ?: 0.0,
        danger = get<Boolean?>("danger") ?: false,
        alertType = try {
            get<String?>("alertType")?.let { AlertType.valueOf(it) } ?: AlertType.NONE
        } catch (e: Exception) {
            AlertType.NONE
        }
    )
}
