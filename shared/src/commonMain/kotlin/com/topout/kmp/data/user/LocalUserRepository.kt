package com.topout.kmp.data.user
import com.topout.kmp.data.Result

import com.topout.kmp.data.Error
import com.topout.kmp.data.dao.UserDao
import com.topout.kmp.models.User

data class UserError(
    override val message: String
) : Error

class LocalUserRepository(
    private val userDao : UserDao
) : UserRepository {

    override suspend fun getUser(): Result<User, UserError> {
        return try {
            val user = userDao.getUser()
            Result.Success(user)
        } catch (e: Exception) {
            Result.Failure(UserError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun saveUser(user: User): Result<Unit, UserError> {
        return try {
            userDao.saveUser(user)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(UserError(e.message ?: "Failed to save user"))
        }
    }

    override suspend fun updateLastSessionsUpdateTime(timestamp: Long): Result<Unit, UserError> {
        return try {
            userDao.updateLastSessionsUpdateTime(timestamp)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(UserError(e.message ?: "Failed to update last sessions update time"))
        }
    }

    override suspend fun getLastSessionsUpdateTime(): Result<Long?, UserError> {
        return try {
            val updateTime = userDao.getLastSessionsUpdateTime()
            Result.Success(updateTime)
        } catch (e: Exception) {
            Result.Failure(UserError(e.message ?: "Failed to get last sessions update time"))
        }
    }

    override suspend fun updateLastUserUpdateTime(timestamp: Long): Result<Unit, UserError> {
        return try {
            userDao.updateLastUserUpdateTime(timestamp)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(UserError(e.message ?: "Failed to update last user update time"))
        }
    }

    override suspend fun getLastUserUpdateTime(): Result<Long?, UserError> {
        return try {
            val updateTime = userDao.getLastUserUpdateTime()
            Result.Success(updateTime)
        } catch (e: Exception) {
            Result.Failure(UserError(e.message ?: "Failed to get last user update time"))
        }
    }
}
