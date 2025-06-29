package com.topout.kmp.data.firebase

import com.topout.kmp.data.sessions.SessionsError
import com.topout.kmp.models.Session
import com.topout.kmp.models.Sessions
import com.topout.kmp.data.Result
interface FirebaseRepository {
    suspend fun getSessions() : List<Session>

    suspend fun saveSession(session: Session)

    suspend fun updateSession(session: Session)

    suspend fun deleteSession(sessionId: String)

    suspend fun signInAnonymously()

}