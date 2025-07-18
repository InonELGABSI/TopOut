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
        return firebaseRepository.deleteSession(sessionId)
    }
}

