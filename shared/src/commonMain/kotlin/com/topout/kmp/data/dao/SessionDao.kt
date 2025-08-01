package com.topout.kmp.data.dao

import com.topout.kmp.SessionEntity
import com.topout.kmp.SessionsQueries
import com.topout.kmp.models.Session
import com.topout.kmp.utils.extensions.toEntity
import com.topout.kmp.utils.extensions.toSession
import dev.gitlive.firebase.firestore.Timestamp

class SessionDao(
    private val queries: SessionsQueries
) {

    fun getSessionById(id: String) : Session {
        return queries.getSessionById(id).executeAsOne().toSession()
    }
    fun getAllSessions():List<Session> {
        return queries.getAllSessions().executeAsList().map { it.toSession() }
    }

    fun saveSession(session: Session) {
        val entity = session.toEntity()
        queries.saveSession(
            id = entity.id,
            userId = entity.userId,
            title = entity.title,
            startTime = entity.startTime,
            endTime = entity.endTime,
            totalAscent = entity.totalAscent,
            totalDescent = entity.totalDescent,
            maxAltitude = entity.maxAltitude,
            minAltitude = entity.minAltitude,
            avgRate = entity.avgRate,
            alertTriggered = entity.alertTriggered,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            sessionDeletedOffline = entity.sessionDeletedOffline,
            sessionCreatedOffline = entity.sessionCreatedOffline,
            sessionUpdatedOffline = entity.sessionUpdatedOffline

        )
    }

    fun deleteSession(sessionId: String) {
        queries.deleteSession(sessionId)
    }

    fun markSessionDeletedOffline(sessionId: String) {
        queries.markSessionDeletedOffline(sessionId)
    }

    fun markSessionCreatedOffline(sessionId: String) {
        queries.markSessionCreatedOffline(sessionId)
    }

    fun getSessionsForSync(): List<Session> {
        return queries.getSessionsForSync().executeAsList().map { it.toSession() }
    }

    fun resolveCreatedOfflineSync(sessionId: String) {
        queries.resolveCreatedOfflineSync(sessionId)
    }

    fun resolveDeletedOfflineSync(sessionId: String) {
        queries.resolveDeletedOfflineSync(sessionId)
    }

    fun updateSessionTitle(sessionId: String, title: String, sessionUpdatedOffline: Boolean) {
        queries.updateSessionTitle(title, if (sessionUpdatedOffline) 1L else 0L, sessionId)
    }

    fun resolveUpdatedOfflineSync(sessionId: String) {
        queries.resolveUpdatedOfflineSync(sessionId)
    }

    fun updateSessionSummary(
        id: String,
        endTime: Long,
        totalAscent: Double?,
        totalDescent: Double?,
        maxAltitude: Double?,
        minAltitude: Double?,
        avgRate: Double?
    ) {
        queries.updateSessionSummary(
            endTime,
            totalAscent,
            totalDescent,
            maxAltitude,
            minAltitude,
            avgRate,
            id
        )
    }

}
