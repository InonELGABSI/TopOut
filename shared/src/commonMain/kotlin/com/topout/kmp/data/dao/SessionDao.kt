package com.topout.kmp.data.dao

import com.topout.kmp.SessionEntity
import com.topout.kmp.SessionsQueries
import com.topout.kmp.models.Session
import com.topout.kmp.utils.extensions.toSession

class SessionDao(
    private val queries: SessionsQueries
) {

    fun getSessionById(id: Long) : Session {
        return queries.getSessionById(id).executeAsOne().toSession()
    }
    fun getAllSessions():List<Session> {
        return queries.getAllSessions().executeAsList().map { it.toSession() }
    }

     fun saveSession(session: Session) {
        queries.saveSession(
            id = session.id?.toLong(),
            userId = session.userId?.toLong(),
            title = session.title,
            startTime = session.startTime,
            endTime = session.endTime,
            totalAscent = session.totalAscent,
            totalDescent = session.totalDescent,
            maxAltitude = session.maxAltitude,
            minAltitude = session.minAltitude,
            avgRate = session.avgRate,
            alertTriggered = session.alertTriggered,
            createdAt = session.createdAt,
            graphImageUrl = session.graphImageUrl
        )

    }

     fun deleteSession(session: Session) {
         session.id?.toLong()?.let { queries.deleteSession(id= it) }

    }

}

