package com.topout.kmp.domain

import com.topout.kmp.data.Result
import com.topout.kmp.data.user.UserError
import com.topout.kmp.data.user.UserRepository
import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.models.User
import com.topout.kmp.utils.nowEpochMillis

class UpdateUser(
    private val localUserRepository: UserRepository,
    private val remoteFirebaseRepository: FirebaseRepository
) {
    suspend operator fun invoke(user: User): Result<User, UserError> {
        // Update the updatedAt field
        val updatedUser = user.copy(
            updatedAt = nowEpochMillis(),
            userUpdatedOffline = false
        )

        // Try to update remote first
        val remoteResult = remoteFirebaseRepository.updateUser(updatedUser)

        val finalUser = when (remoteResult) {
            is Result.Success -> {
                // Remote update successful, keep user as is
                updatedUser
            }
            is Result.Failure -> {
                // Remote update failed, mark as updated offline
                updatedUser.copy(userUpdatedOffline = true)
            }
        }

        // Always update local (whether remote succeeded or failed)
        return when (val localResult = localUserRepository.saveUser(finalUser)) {
            is Result.Success -> Result.Success(finalUser)
            is Result.Failure -> localResult
        }
    }
}
