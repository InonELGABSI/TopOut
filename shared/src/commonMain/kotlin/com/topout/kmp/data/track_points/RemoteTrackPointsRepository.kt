package com.topout.kmp.data.track_points

import com.topout.kmp.data.Error
import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.models.Metrics
import com.topout.kmp.models.TrackPoint
import kotlinx.coroutines.flow.Flow

data class TrackPointsError (
    override val message: String
) : Error

class RemoteTrackPointsRepository(
    private val trackPointsDao: TrackPointsDao
) {
    suspend fun insertTrackPoint(trackPoint: TrackPoint) {
        trackPoint.timestamp.let {
            trackPointsDao.insertTrackPoint(
                sessionId = trackPoint.sessionId,
                ts = it,
                lat = trackPoint.latitude,
                lon = trackPoint.longitude,
                altitude = trackPoint.altitude,
                accelX = trackPoint.accelerationX,
                accelY = trackPoint.accelerationY,
                accelZ = trackPoint.accelerationZ,
                metrics = Metrics(
                    vVertical = trackPoint.vVertical,
                    vHorizontal = trackPoint.vHorizontal,
                    vTotal = trackPoint.vTotal,
                    gain = trackPoint.gain,
                    loss = trackPoint.loss,
                    relAltitude = trackPoint.relAltitude,
                    avgVertical = trackPoint.avgVertical,
                    danger = trackPoint.danger,
                    alertType = trackPoint.alertType
                )
            )
        }
    }

    suspend fun getTrackPointsBySessionId(sessionId: String): List<TrackPoint> {
        return trackPointsDao.getTrackPointsBySessionId(sessionId)
    }

    suspend fun getTrackPointsFlowBySessionId(sessionId: String) : Flow<List<TrackPoint>> = trackPointsDao.getTrackPointsFlowBySessionId(sessionId)

    suspend fun deleteTrackPointsBySessionId(sessionId: String) {
        trackPointsDao.deleteTrackPointsBySessionId(sessionId)
    }
    suspend fun endCurrentSession() {
        trackPointsDao.endCurrentSession()
    }
}

