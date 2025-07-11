package com.topout.kmp.data.firebase

import com.topout.kmp.data.sessions.SessionsError
import com.topout.kmp.models.Session
import com.topout.kmp.models.Sessions
import com.topout.kmp.data.Result
import com.topout.kmp.data.user.UserError
import com.topout.kmp.models.User

interface FirebaseRepository {
    suspend fun getSessions() : Result<List<Session>, SessionsError>

    suspend fun saveSession(session: Session)

    suspend fun updateSession(session: Session)

    suspend fun deleteSession(sessionId: String)

    suspend fun signInAnonymously() : Result<User, UserError>

    suspend fun getUser() : Result<User, UserError>

    suspend fun ensureUserDocument(): Result<Unit, UserError>
}
