package com.topout.kmp.utils.extensions

import com.topout.kmp.models.User
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Timestamp


/** get field as Long else Timestamp else 0  */
private fun DocumentSnapshot.millis(field: String): Long {
    get<Long?>(field)?.let { return it }

    get<Timestamp?>(field)?.let { return it.toEpochMillis() }

    return 0L
}

/** Firestore document ➜ User (pure Long timestamps) */
fun DocumentSnapshot.toUser(): User {
    val id: String = get("id")

    return User(
        id = id,
        name = get<String?>("name"),
        email = get<String?>("email"),
        imgUrl = get<String?>("img_url"),
        unitPreference = get<String?>("unit_preference") ?: "meters",
        enabledNotifications = get<Boolean?>("enabled_notifications") ?: false,
        relativeHeightFromStartThr = get<Double?>("relative_height_from_start_thr"),
        totalHeightFromStartThr = get<Double?>("total_height_from_start_thr"),
        currentAvgHeightSpeedThr = get<Double?>("current_avg_height_speed_thr"),
        userUpdatedOffline = false,
        createdAt = millis("created_at"),
        updatedAt = millis("updated_at")
    )
}

/**
 * User ➜ Map ready for Firestore.
 */
fun User.toFirestoreMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>(
        "id" to id,
        "name" to name,
        "email" to email,
        "img_url" to imgUrl,
        "unit_preference" to (unitPreference ?: "meters"),
        "enabled_notifications" to (enabledNotifications ?: false),
        "created_at" to createdAt,
        "updated_at" to updatedAt
    )
    relativeHeightFromStartThr?.let { map["relative_height_from_start_thr"] = it }
    totalHeightFromStartThr?.let { map["total_height_from_start_thr"] = it }
    currentAvgHeightSpeedThr?.let { map["current_avg_height_speed_thr"] = it }
    return map
}
