package com.topout.kmp.utils.extensions

import com.topout.kmp.models.Session
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Timestamp

/* ---------- read ---------- */
private fun Any?.toEpochMillis(): Long = when (this) {
    is Timestamp -> this.toEpochMillis()
    is Long      -> this
    else         -> 0L
}


/* Firestore document ➜ Session (all Long timestamps) */
fun DocumentSnapshot.toSession(): Session {
    val base = data<Session>() ?: error("Invalid Session data")

    return base.copy(
        startTime = get<Any?>("start_time").toEpochMillis(),
        endTime   = get<Any?>("end_time").toEpochMillis(),
        createdAt = get<Any?>("created_at").toEpochMillis()
    )
}

/* ---------- write ---------- */
/**
 * Convert Session ➜ Map ready for Firestore.
 * Pass `serverCreatedAt = true` to let Firestore set `created_at`
 * using FieldValue.serverTimestamp().
 */
fun Session.toFirestoreMap(serverCreatedAt: Boolean = false): Map<String, Any?> =
    mutableMapOf<String, Any?>(
        "id"              to id,
        "userId"          to userId,
        "title"           to title,
        "start_time"      to startTime?.toTimestamp(),
        "end_time"        to endTime?.toTimestamp(),
        "total_ascent"    to totalAscent,
        "total_descent"   to totalDescent,
        "max_altitude"    to maxAltitude,
        "min_altitude"    to minAltitude,
        "avg_rate"        to avgRate,
        "alert_triggered" to alertTriggered,
        "created_at"      to if (serverCreatedAt)
            FieldValue.serverTimestamp
        else
            createdAt?.toTimestamp(),
        "graph_image_url" to graphImageUrl
    )
