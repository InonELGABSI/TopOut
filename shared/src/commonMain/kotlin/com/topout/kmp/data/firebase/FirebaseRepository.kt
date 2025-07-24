package com.topout.kmp.data.firebase

import com.topout.kmp.data.sessions.SessionsError
import com.topout.kmp.models.Session
import com.topout.kmp.data.Result
import com.topout.kmp.data.user.UserError
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.models.User

interface FirebaseRepository {

    suspend fun getSessions() : Result<List<Session>, SessionsError>

    suspend fun getSessionsUpdatedAfter(timestamp: Long) : Result<List<Session>, SessionsError>

    suspend fun saveSession(session: Session) : Result<Session, SessionsError>

    suspend fun updateSession(session: Session)

    suspend fun deleteSession(sessionId: String) : Result<Unit, SessionsError>

    suspend fun signInAnonymously() : Result<User, UserError>

    suspend fun getUser() : Result<User, UserError>

    suspend fun ensureUserDocument(): Result<Unit, UserError>

    suspend fun pushTrackPoints(sessionId: String, points: List<TrackPoint>) : Result<Unit, SessionsError>

    suspend fun getSessionById(sessionId: String): Result<Session?, SessionsError>

    suspend fun getTrackPointsBySession(sessionId: String): Result<List<TrackPoint>, SessionsError>

}
