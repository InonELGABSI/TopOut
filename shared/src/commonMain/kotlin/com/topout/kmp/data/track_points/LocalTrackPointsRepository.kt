// LocalTrackPointsRepository.kt
package com.topout.kmp.data.track_points

import com.topout.kmp.data.Error
import com.topout.kmp.data.Result
import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.models.Metrics
import com.topout.kmp.models.TrackPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class TrackPointsError (
    override val message: String
) : Error

class LocalTrackPointsRepository(
    private val dao: TrackPointsDao
) : TrackPointsRepository {

    override suspend fun insert(trackPoint: TrackPoint): Result<String, TrackPointsError> = try {
        val id = dao.insertTrackPoint(
            sessionId = trackPoint.sessionId,
            ts        = trackPoint.timestamp,
            lat       = trackPoint.latitude,
            lon       = trackPoint.longitude,
            altitude  = trackPoint.altitude,
            accelX    = trackPoint.accelerationX,
            accelY    = trackPoint.accelerationY,
            accelZ    = trackPoint.accelerationZ,
            metrics   = Metrics(
                gain        = trackPoint.gain,
                loss        = trackPoint.loss,
                relAltitude = trackPoint.relAltitude,
                avgVertical = trackPoint.avgVertical,
                avgHorizontal = trackPoint.avgHorizontal,
                danger      = trackPoint.danger,
                alertType   = trackPoint.alertType
            )
        )
        Result.Success(id)
    } catch (e: Exception) {
        Result.Failure(TrackPointsError(e.message ?: "Insert failed"))
    }

    override suspend fun getBySession(sessionId: String)
            : Result<List<TrackPoint>, TrackPointsError> = try {
        Result.Success(dao.getTrackPointsBySessionId(sessionId))
    } catch (e: Exception) {
        Result.Failure(TrackPointsError(e.message ?: "Fetch failed"))
    }

    override fun flowBySession(sessionId: String)
            : Flow<Result<List<TrackPoint>, TrackPointsError>> =
        dao.getTrackPointsFlowBySessionId(sessionId)
            .map { Result.Success(it) }

    override suspend fun deleteBySession(sessionId: String)
            : Result<Unit, TrackPointsError> = try {
        dao.deleteTrackPointsBySessionId(sessionId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(TrackPointsError(e.message ?: "Delete failed"))
    }
}
