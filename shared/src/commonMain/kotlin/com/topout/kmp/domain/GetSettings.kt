package com.topout.kmp.domain

import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.user.UserRepository
import com.topout.kmp.data.Result
import com.topout.kmp.data.user.UserError
import com.topout.kmp.models.User
import com.topout.kmp.utils.nowEpochMillis

class GetSettings (
    private val userRepository: UserRepository,
    private val remoteFirebaseRepository: FirebaseRepository
) {
    suspend operator fun invoke(): Result<User, UserError> {
        return try {
            // Step 1: Check when user settings were last updated locally
            val lastUpdateResult = userRepository.getLastUserUpdateTime()
            val lastUpdateTime = when (lastUpdateResult) {
                is Result.Success -> lastUpdateResult.data ?: 0L
                is Result.Failure -> 0L // If no record, sync from beginning
            }

            // Step 2: Try to get updated user data from remote
            val remoteUserResult = remoteFirebaseRepository.getUser()

            when (remoteUserResult) {
                is Result.Success -> {
                    val remoteUser = remoteUserResult.data

                    if (remoteUser != null) {
                        // Check if remote data is newer than local
                        val remoteUpdateTime = remoteUser.updatedAt ?: 0L

                        if (remoteUpdateTime > lastUpdateTime) {
                            // Step 3: Update the local db with newer remote data
                            val saveResult = userRepository.saveUser(remoteUser)

                            when (saveResult) {
                                is Result.Success -> {
                                    // Step 4: Update the last update time in user db
                                    val currentTime = nowEpochMillis()
                                    userRepository.updateLastUserUpdateTime(currentTime)
                                }
                                is Result.Failure -> {
                                    // If save fails, still return the remote data but don't update timestamp
                                    return Result.Success(remoteUser)
                                }
                            }
                        }
                    }

                    // Step 5: Return the data requested to the user (from local db)
                    return userRepository.getUser()
                }
                is Result.Failure -> {
                    // If remote fetch fails, return local data
                    return userRepository.getUser()
                }
            }
        } catch (e: Exception) {
            // Fallback to local data in case of any unexpected errors
            userRepository.getUser()
        }
    }
}

