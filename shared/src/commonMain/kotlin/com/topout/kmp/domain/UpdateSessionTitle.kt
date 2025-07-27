package com.topout.kmp.domain

import com.topout.kmp.data.Result
import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.data.sessions.SessionsError

class UpdateSessionTitle(
    private val sessionsRepository: SessionsRepository,
    private val firebaseRepository: FirebaseRepository
) {
    suspend operator fun invoke(sessionId: String, newTitle: String): Result<Unit, SessionsError> {
        return try {
            // Get the current session
            val currentSessionResult = sessionsRepository.getSessionById(sessionId)

            when (currentSessionResult) {
                is Result.Success -> {
                    val currentSession = currentSessionResult.data
                    if (currentSession == null) {
                        return Result.Failure(SessionsError("Session not found"))
                    }

                    val updatedSession = currentSession.copy(
                        title = newTitle,
                        sessionUpdatedOffline = false // Initially assume we'll sync successfully
                    )

                    // Try to update remotely first
                    val remoteUpdateResult = firebaseRepository.updateSession(updatedSession)

                    val finalSession = when (remoteUpdateResult) {
                        is Result.Success -> {
                            // Remote update successful, keep sessionUpdatedOffline = false
                            updatedSession
                        }
                        is Result.Failure -> {
                            // Remote update failed, mark for offline sync
                            updatedSession.copy(sessionUpdatedOffline = true)
                        }
                    }

                    // Always save locally (whether remote succeeded or failed)
                    sessionsRepository.saveSession(finalSession)

                    // Always return success since we saved locally
                    Result.Success(Unit)
                }
                is Result.Failure -> {
                    Result.Failure(SessionsError("Session not found: ${currentSessionResult.error?.message}"))
                }
            }
        } catch (e: Exception) {
            Result.Failure(SessionsError("Failed to update session title: ${e.message}"))
        }
    }
}
