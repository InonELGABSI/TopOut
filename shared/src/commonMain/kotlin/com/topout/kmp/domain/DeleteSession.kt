package com.topout.kmp.domain
import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.models.Session

class DeleteSession (
    private val sessionRepository: SessionsRepository,
    private val firebaseRepository: FirebaseRepository
) {

    suspend operator fun invoke(session: Session) {
        sessionRepository.deleteSession(session)
        firebaseRepository.saveSession(session)
    }
}




