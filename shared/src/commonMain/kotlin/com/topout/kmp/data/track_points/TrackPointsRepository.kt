// TrackPointsRepository.kt
package com.topout.kmp.data.track_points

import com.topout.kmp.data.Result
import com.topout.kmp.models.TrackPoint
import kotlinx.coroutines.flow.Flow

interface TrackPointsRepository {

    /** Insert one point — returns row-id or Failure */
    suspend fun insert(trackPoint: TrackPoint): Result<String, TrackPointsError>

    /** List points for a session */
    suspend fun getBySession(sessionId: String): Result<List<TrackPoint>, TrackPointsError>

    /** Same list as a flow */
    fun flowBySession(sessionId: String): Flow<Result<List<TrackPoint>, TrackPointsError>>

    /** Delete all points of a session */
    suspend fun deleteBySession(sessionId: String): Result<Unit, TrackPointsError>
}
