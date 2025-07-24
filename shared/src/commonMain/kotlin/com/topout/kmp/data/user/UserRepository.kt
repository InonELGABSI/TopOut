package com.topout.kmp.data.user

import com.topout.kmp.data.Result
import com.topout.kmp.models.User

interface UserRepository {
    suspend fun getUser():Result<User, UserError>
    suspend fun saveUser(user: User): Result<Unit, UserError>
    suspend fun updateLastSessionsUpdateTime(timestamp: Long): Result<Unit, UserError>
    suspend fun getLastSessionsUpdateTime(): Result<Long?, UserError>
    suspend fun updateLastUserUpdateTime(timestamp: Long): Result<Unit, UserError>
    suspend fun getLastUserUpdateTime(): Result<Long?, UserError>
}

