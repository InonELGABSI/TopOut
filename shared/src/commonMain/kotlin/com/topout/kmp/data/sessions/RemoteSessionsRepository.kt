package com.topout.kmp.data.sessions

import com.topout.kmp.data.Error
import com.topout.kmp.data.Result
import com.topout.kmp.data.dao.SessionDao
import com.topout.kmp.models.Session
import com.topout.kmp.models.Sessions
import kotlinx.serialization.Serializable

data class SessionsError (
    override val message: String
) : Error

class RemoteSessionsRepository (
    private val sessionDao: SessionDao
): SessionsRepository {


    override suspend fun getSessions(): Result<Sessions, SessionsError> {
        // Simulate a network call to fetch sessions
        return try {
            val sessions = sessionDao.getAllSessions()
            Result.Success(Sessions(sessions))

        } catch (e: Exception) {
            Result.Failure(SessionsError(e.message ?: "Unknown error"))
        }
    }


    override suspend fun getSessionById(id: String): Result<Session, SessionsError> {
        return try {
            val session = sessionDao.getSessionById(id)
            Result.Success(session)
        } catch (e: Exception) {
            Result.Failure(SessionsError(e.message ?: "Unknown error"))
        }
    }


    override suspend fun saveSession(session: Session) {
        sessionDao.saveSession(session)
    }


    override suspend fun deleteSession(session: Session) {
        sessionDao.deleteSession(session)
    }

    override suspend fun createSession(): Result<Session, SessionsError> {
        return try {
            val newSession = Session()
            sessionDao.saveSession(newSession)
            Result.Success(newSession)
        } catch (e: Exception) {
            Result.Failure(SessionsError(e.message ?: "Failed to create session"))
        }
    }
}


@Serializable
data class SessionsResponse(
    val results: Sessions
)
