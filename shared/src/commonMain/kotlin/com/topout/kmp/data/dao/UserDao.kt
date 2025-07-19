package com.topout.kmp.data.dao

import com.topout.kmp.UserQueries
import com.topout.kmp.models.User
import com.topout.kmp.utils.extensions.toUser

class UserDao(
    private val queries: UserQueries
) {
    fun getUser() :User = queries.getUser().executeAsOne().toUser()

    fun updateLastSessionsUpdateTime(timestamp: Long) {
        queries.updateLastSessionsUpdateTime(timestamp)
    }

    fun getLastSessionsUpdateTime(): Long? {
        return queries.getLastSessionsUpdateTime().executeAsOneOrNull()?.localSessionsUpdateTime
    }
}

