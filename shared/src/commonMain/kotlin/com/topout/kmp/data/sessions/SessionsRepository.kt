package com.topout.kmp.data.sessions

import com.topout.kmp.data.Result
import com.topout.kmp.models.Session
import com.topout.kmp.models.Sessions

interface SessionsRepository {
    suspend fun getSessions(): Result<Sessions, SessionsError>
//    suspend fun getSessionById(id: String): Result<Session, SessionsError>
//    suspend fun addSession(session: Session)
//    suspend fun updateSession(session: Session)
//    suspend fun deleteSession(id: String)
}