package com.topout.kmp.domain

import com.topout.kmp.data.firebase.RemoteFirebaseRepository
import com.topout.kmp.data.sessions.SessionsRepository

// shared/commonMain
class SyncOfflineChanges(
    private val sessionsRepository: SessionsRepository,
    private val remoteRepository: RemoteFirebaseRepository
) {
    suspend operator fun invoke() {

    }
}
