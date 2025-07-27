package com.topout.kmp.domain
import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.data.Result
import com.topout.kmp.data.sessions.SessionsError
import com.topout.kmp.models.Session

class DeleteSession (
    private val sessionRepository: SessionsRepository,
    private val firebaseRepository: FirebaseRepository
) {

    suspend operator fun invoke(sessionId: String): Result<Unit, SessionsError> {
        return try {
            // Step 1: Mark session as deleted offline first (immediate UI feedback)
            val markDeletedResult = sessionRepository.markSessionDeletedOffline(sessionId)

            when (markDeletedResult) {
                is Result.Success -> {
                    // Step 2: Try to delete from remote (Firebase)
                    val remoteDeleteResult = firebaseRepository.deleteSession(sessionId)

                    when (remoteDeleteResult) {
                        is Result.Success -> {
                            // Step 3: If remote deletion successful, hard delete from local
                            sessionRepository.deleteSession(sessionId)
                        }
                        is Result.Failure -> {
                            // Remote deletion failed, but session is marked as deleted offline
                            // Session will remain hidden from user but stay in DB for later sync
                            Result.Success(Unit)
                        }
                    }
                }
                is Result.Failure -> {
                    // Failed to mark as deleted offline
                    return markDeletedResult
                }
            }
        } catch (e: Exception) {
            // If any unexpected error occurs, try to at least mark as deleted offline
            sessionRepository.markSessionDeletedOffline(sessionId)
        }
    }
}
