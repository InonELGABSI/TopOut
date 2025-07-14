package com.topout.kmp.domain

import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.firebase.RemoteFirebaseRepository
import com.topout.kmp.data.sessions.SessionsRepository

class GetSessions (
    private val sessionsRepository: SessionsRepository,
    private val remoteFirebaseRepository: FirebaseRepository
) {
    //suspend operator fun invoke() = sessionsRepository.getSessions()
    suspend operator fun invoke() = remoteFirebaseRepository.getSessions()
}

