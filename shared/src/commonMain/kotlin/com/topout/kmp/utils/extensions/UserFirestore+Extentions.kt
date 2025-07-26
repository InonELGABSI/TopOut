package com.topout.kmp.utils.extensions

import com.topout.kmp.models.User
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
/** Firestore document ➜ User (pure Long timestamps) */
fun DocumentSnapshot.toUser(): User {
    val id = get<String>("id") ?: error("Missing id in User document")

    return User(
        id = id,
        name = get<String?>("name"),
        email = get<String?>("email"),
        imgUrl = get<String?>("img_url"),
        unitPreference = get<String?>("unit_preference") ?: "meters",
        enabledNotifications = get<Boolean?>("enabled_notifications") ?: false,
        relativeHeightFromStartThr = get<Double?>("relative_height_from_start_thr") ?: 0.0,
        totalHeightFromStartThr = get<Double?>("total_height_from_start_thr") ?: 0.0,
        currentAvgHeightSpeedThr = get<Double?>("current_avg_height_speed_thr") ?: 0.0,
        userUpdatedOffline = get<Boolean?>("user_updated_offline") ?: false,
        createdAt = millis("created_at"),
        updatedAt = millis("updated_at")
    )
}

/* ---------- write ---------- */
/**
 * User ➜ Map ready for Firestore.
 */
fun User.toFirestoreMap(): Map<String, Any?> =
    mutableMapOf<String, Any?>(
        "id" to id,
        "name" to name,
        "email" to email,
        "img_url" to imgUrl,
        "unit_preference" to (unitPreference ?: "meters"),
        "enabled_notifications" to (enabledNotifications ?: false),
        "relative_height_from_start_thr" to (relativeHeightFromStartThr ?: 0.0),
        "total_height_from_start_thr" to (totalHeightFromStartThr ?: 0.0),
        "current_avg_height_speed_thr" to (currentAvgHeightSpeedThr ?: 0.0),
        "user_updated_offline" to userUpdatedOffline,
        "created_at" to createdAt,
        "updated_at" to updatedAt
    )
