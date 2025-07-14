package com.topout.kmp.utils.extensions

import com.topout.kmp.models.Session
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Timestamp

/* ---------- helpers ---------- */

/** get field as Long else Timestamp else 0  */
private fun DocumentSnapshot.millis(field: String): Long {
    // try Long first (what we usually store)
    get<Long?>(field)?.let { return it }

    // fallback to Timestamp (when Firestore wrote serverTimestamp)
    get<Timestamp?>(field)?.let { return it.toEpochMillis() }

    return 0L
}

/* ---------- read ---------- */
/** Firestore document ➜ Session (pure Long timestamps) */
fun DocumentSnapshot.toSession(): Session {
    val base = data<Session>() ?: error("Invalid Session data")

    return base.copy(
        startTime = millis("start_time"),
        endTime   = get<Long?>("end_time")           // may be null
            ?: get<Timestamp?>("end_time")?.toEpochMillis(),
        createdAt = millis("created_at")
    )
}

/* ---------- write ---------- */
/**
 * Session ➜ Map ready for Firestore.
 * Pass `serverCreatedAt = true` to store `created_at`
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
