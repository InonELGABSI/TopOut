package com.topout.kmp.data.track_points

import com.topout.kmp.models.TrackPoint
import kotlinx.coroutines.flow.Flow

interface TrackPointsRepository {
    suspend fun insertTrackPoint (trackPoint: TrackPoint)
    suspend fun getTrackPointsBySessionId(sessionId: String): List<TrackPoint>
    suspend fun getTrackPointsFlowBySessionId (sessionId: String): Flow<List<TrackPoint>>
    suspend fun deleteTrackPointsBySessionId(sessionId: String)
    suspend fun endCurrentSession()
}