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
        relativeHeightFromStartThr = relativeHeightFromStartThr ?: 0.0,
        totalHeightFromStartThr = totalHeightFromStartThr ?: 0.0,
        currentAvgHeightSpeedThr = currentAvgHeightSpeedThr ?: 0.0,

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
        relativeHeightFromStartThr = relativeHeightFromStartThr ?: 0.0,
        totalHeightFromStartThr = totalHeightFromStartThr ?: 0.0,
        currentAvgHeightSpeedThr = currentAvgHeightSpeedThr ?: 0.0,
        localSessionsUpdateTime = null, // Will be handled by database defaults or DAO
        localUserUpdateTime = null, // Will be handled by database defaults or DAO

        userUpdatedOffline = if (userUpdatedOffline) 1L else 0L,
        updatedAt = updatedAt,
        createdAt = createdAt
    )
}

