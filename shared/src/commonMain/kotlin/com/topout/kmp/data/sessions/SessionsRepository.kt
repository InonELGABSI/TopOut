package com.topout.kmp.data.sessions

import com.topout.kmp.data.Result
import com.topout.kmp.models.Session
import com.topout.kmp.models.Sessions

interface SessionsRepository {
    suspend fun getSessions(): Result<Sessions, SessionsError>
    suspend fun getSessionById(id: String): Result<Session, SessionsError>
    suspend fun saveSession(session: Session)
    suspend fun deleteSession(sessionId: String) : Result<Unit, SessionsError>
    suspend fun createSession(): Result<Session, SessionsError>
    suspend fun saveSessions(sessions: List<Session>): Result<Unit, SessionsError>
//    suspend fun updateSession(session: Session)
}

