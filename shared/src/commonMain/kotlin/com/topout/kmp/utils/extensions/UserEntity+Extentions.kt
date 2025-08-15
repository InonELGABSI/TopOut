package com.topout.kmp.utils.extensions

import com.topout.kmp.UserEntity
import com.topout.kmp.models.User

fun UserEntity.toUser(): User {
    return User(
        id = id,
        name = name,
        email = email,
        imgUrl = imgUrl,

        unitPreference = unitPreference ?: "meters",
        enabledNotifications = enabledNotifications?.toInt() == 1,
        // leave thresholds nullable if not set in DB
        relativeHeightFromStartThr = relativeHeightFromStartThr,
        totalHeightFromStartThr = totalHeightFromStartThr,
        currentAvgHeightSpeedThr = currentAvgHeightSpeedThr,

        userUpdatedOffline = userUpdatedOffline == 1L,
        updatedAt = updatedAt,
        createdAt = createdAt
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        imgUrl = imgUrl,

        unitPreference = unitPreference ?: "meters",
        enabledNotifications = if (enabledNotifications == true) 1L else 0L,
        // write 0.0 only if explicitly set, otherwise keep null so Firestore merge can distinguish
        relativeHeightFromStartThr = relativeHeightFromStartThr,
        totalHeightFromStartThr = totalHeightFromStartThr,
        currentAvgHeightSpeedThr = currentAvgHeightSpeedThr,
        localSessionsUpdateTime = null,
        localUserUpdateTime = null,

        userUpdatedOffline = if (userUpdatedOffline == true) 1L else 0L,
        updatedAt = updatedAt,
        createdAt = createdAt
    )
}
