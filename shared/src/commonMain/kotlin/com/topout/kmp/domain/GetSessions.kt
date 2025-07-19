package com.topout.kmp.domain

import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.data.user.UserRepository
import com.topout.kmp.data.Result
import com.topout.kmp.models.Sessions
import com.topout.kmp.data.sessions.SessionsError
import com.topout.kmp.utils.nowEpochMillis

class GetSessions (
    private val sessionsRepository: SessionsRepository,
    private val remoteFirebaseRepository: FirebaseRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Sessions, SessionsError> {
        return try {
            // Step 1: Check when sessions were last updated locally
            val lastUpdateResult = userRepository.getLastSessionsUpdateTime()
            val lastUpdateTime = when (lastUpdateResult) {
                is Result.Success -> lastUpdateResult.data ?: 0L
                is Result.Failure -> 0L // If no record, sync from beginning
            }

            // Step 2: Bring all data updated after the last local update
            // If lastUpdateTime is 0 (first sync), get all sessions instead of just updated ones
            val remoteSessionsResult = if (lastUpdateTime == 0L) {
                remoteFirebaseRepository.getSessions() // Get ALL sessions for initial sync
            } else {
                remoteFirebaseRepository.getSessionsUpdatedAfter(lastUpdateTime) // Get only updated sessions
            }

            when (remoteSessionsResult) {
                is Result.Success -> {
                    val updatedSessions = remoteSessionsResult.data

                    if (!updatedSessions.isNullOrEmpty()) {
                        // Step 3: Update the local db for specific updated sessions
                        val saveResult = sessionsRepository.saveSessions(updatedSessions)

                        when (saveResult) {
                            is Result.Success -> {
                                // Step 4: Update the last update time in user db
                                val currentTime = nowEpochMillis()
                                userRepository.updateLastSessionsUpdateTime(currentTime)
                            }
                            is Result.Failure -> {
                                return Result.Failure(saveResult.error)
                            }
                        }
                    }

                    // Step 5: Return the data requested to the user (from local db)
                    return sessionsRepository.getSessions()
                }
                is Result.Failure -> {
                    // If remote fetch fails, return local data
                    return sessionsRepository.getSessions()
                }
            }
        } catch (e: Exception) {
            // Fallback to local data in case of any unexpected errors
            sessionsRepository.getSessions()
        }
    }
}
