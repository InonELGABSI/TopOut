package com.topout.kmp.data.user
import com.topout.kmp.data.Result

import com.topout.kmp.data.Error
import com.topout.kmp.data.dao.UserDao
import com.topout.kmp.models.User
import kotlinx.serialization.Serializable

data class UserError(
    override val message: String
) : Error

class RemoteUserRepository(
    private val userDao : UserDao
) : UserRepository {

    override suspend fun getUser(

    ): Result<User, UserError> {
        return try {
            val user = userDao.getUser()
            Result.Success(user)
        } catch (e: Exception) {
            Result.Failure(UserError(e.message ?: "Unknown error"))
        }
    }
}
@Serializable
data class UserResponse(
    val results: User
)