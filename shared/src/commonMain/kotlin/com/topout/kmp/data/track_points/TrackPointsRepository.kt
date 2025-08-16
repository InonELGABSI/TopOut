// TrackPointsRepository.kt
package com.topout.kmp.data.track_points

import com.topout.kmp.data.Result
import com.topout.kmp.models.TrackPoint
import kotlinx.coroutines.flow.Flow

interface TrackPointsRepository {

    suspend fun insert(trackPoint: TrackPoint): Result<String, TrackPointsError>

    suspend fun getBySession(sessionId: String): Result<List<TrackPoint>, TrackPointsError>

    fun flowBySession(sessionId: String): Flow<Result<List<TrackPoint>, TrackPointsError>>

    suspend fun deleteBySession(sessionId: String): Result<Unit, TrackPointsError>
}
