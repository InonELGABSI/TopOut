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
    val id = get<String>("id") ?: error("Missing id in Session document")

    return Session(
        id = id,
        userId = get<String?>("user_id") ?: "",
        title = get<String?>("title") ?: "",
        startTime = millis("start_time"),
        endTime = millis("end_time"),
        totalAscent = get<Double?>("total_ascent") ?: 0.0,
        totalDescent = get<Double?>("total_descent") ?: 0.0,
        maxAltitude = get<Double?>("max_altitude") ?: 0.0,
        minAltitude = get<Double?>("min_altitude") ?: 0.0,
        avgRate = get<Double?>("avg_rate") ?: 0.0,
        alertTriggered = get<Long?>("alert_triggered") ?: 0,
        createdAt = millis("created_at"),
        updatedAt = millis("updated_at"), // Note: different field name convention
        sessionDeletedOffline = false,
        sessionCreatedOffline = false,
        sessionUpdatedOffline = false
    )
}

/* ---------- write ---------- */
/**
 * Session ➜ Map ready for Firestore.
 * Pass `serverCreatedAt = true` to store `created_at`
 * using FieldValue.serverTimestamp().
 */
fun Session.toFirestoreMap(): Map<String, Any?> =
    mutableMapOf<String, Any?>(
        "id"              to id,
        "user_id"          to userId,
        "title"           to title,
        "start_time"      to startTime,
        "end_time"        to endTime,
        "total_ascent"    to totalAscent,
        "total_descent"   to totalDescent,
        "max_altitude"    to maxAltitude,
        "min_altitude"    to minAltitude,
        "avg_rate"        to avgRate,
        "alert_triggered" to alertTriggered,
        "updated_at"       to updatedAt,
        "created_at"      to createdAt
    )
