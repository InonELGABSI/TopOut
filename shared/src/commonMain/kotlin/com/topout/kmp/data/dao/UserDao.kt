package com.topout.kmp.data.dao

import com.topout.kmp.UserQueries
import com.topout.kmp.models.User
import com.topout.kmp.utils.extensions.toUser
import com.topout.kmp.utils.extensions.toEntity

class UserDao(
    private val queries: UserQueries
) {
    fun getUser() :User {
        val entity =queries.getUser().executeAsOne()
        return entity.toUser()
    }

    fun saveUser(user: User) {
        val entity = user.toEntity()
        queries.saveUser(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            imgUrl = entity.imgUrl,
            unitPreference = entity.unitPreference,
            enabledNotifications = entity.enabledNotifications,
            relativeHeightFromStartThr = entity.relativeHeightFromStartThr,
            totalHeightFromStartThr = entity.totalHeightFromStartThr,
            currentAvgHeightSpeedThr = entity.currentAvgHeightSpeedThr,
            localSessionsUpdateTime = entity.localSessionsUpdateTime,
            localUserUpdateTime = entity.localUserUpdateTime,
            userUpdatedOffline = entity.userUpdatedOffline,
            updatedAt = entity.updatedAt,
            createdAt = entity.createdAt
        )
    }

    fun updateLastSessionsUpdateTime(timestamp: Long) {
        queries.updateLastSessionsUpdateTime(timestamp)
    }

    fun getLastSessionsUpdateTime(): Long? {
        return queries.getLastSessionsUpdateTime().executeAsOneOrNull()?.localSessionsUpdateTime
    }

    fun updateLastUserUpdateTime(timestamp: Long) {
        queries.updateLastUserUpdateTime(timestamp)
    }

    fun getLastUserUpdateTime(): Long? {
        return queries.getLastUserUpdateTime().executeAsOneOrNull()?.localUserUpdateTime
    }

    fun markUserAsSynced() {
        queries.markUserAsSynced()
    }
}
