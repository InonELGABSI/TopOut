package com.topout.kmp.data.dao

import com.topout.kmp.Track_pointsQueries
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.models.Metrics
import com.topout.kmp.utils.extensions.toTrackPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.IO

/**
 * Data Access Object for TrackPoints - handles persistence to SQLDelight
 * Single Responsibility: Data access operations for track points
 */
class TrackPointsDao(
    private val queries: Track_pointsQueries
) {

    fun insertTrackPoint(
        sessionId: String,
        ts: Long,
        lat: Double? = null,
        lon: Double? = null,
        altitude: Double? = null,
        accelX: Float? = null,
        accelY: Float? = null,
        accelZ: Float? = null,
        metrics: Metrics
    ): String {
        queries.insertTrackPoint(
            sessionId = sessionId,
            ts = ts,
            lat = lat,
            lon = lon,
            altitude = altitude,
            accelX = accelX?.toDouble(),
            accelY = accelY?.toDouble(),
            accelZ = accelZ?.toDouble(),
            gain = metrics.gain,
            loss = metrics.loss,
            relAltitude = metrics.relAltitude,
            avgHorizontal = metrics.avgHorizontal,
            avgVertical = metrics.avgVertical,
            danger = if (metrics.danger) 1L else 0L,
            alertType = metrics.alertType.name
        )

        return queries.transactionWithResult {
            queries.lastInsertRowId().executeAsOne().toString()
        }
    }

     fun getTrackPointsBySessionId(sessionId: String): List<TrackPoint> {
        return queries.getTrackPointsBySession(sessionId)
            .executeAsList()
            .map { it.toTrackPoint() }
    }

    fun getTrackPointsFlowBySessionId(sessionId: String): Flow<List<TrackPoint>> {
        return queries.getTrackPointsBySession(sessionId)
            .asFlow()
            .mapToList(kotlinx.coroutines.Dispatchers.IO)
            .map { list -> list.map { it.toTrackPoint() } }
    }

     fun deleteTrackPointsBySessionId(sessionId: String) {
        queries.deleteTrackPointsBySession(sessionId)
    }

}
