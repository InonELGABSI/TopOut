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

        createdAt = createdAt ?: 0L
    )
}
