package com.topout.kmp.domain

import com.topout.kmp.data.Result
import com.topout.kmp.data.track_points.TrackPointsRepository
import com.topout.kmp.models.TrackPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetLocalTrackPointsFlow(
    private val trackPointsRepository: TrackPointsRepository
) {
    suspend operator fun invoke(sessionId: String): Flow<List<TrackPoint>> = flow {
        while (true) {
            when (val result = trackPointsRepository.getBySession(sessionId)) {
                is Result.Success -> {
                    val trackPoints = result.data ?: emptyList()
                    // Filter to only include location data (lat, lon, alt)
                    val locationTrackPoints = trackPoints.filter { point ->
                        point.latitude != null && point.longitude != null
                    }
                    emit(locationTrackPoints)
                }
                is Result.Failure -> {
                    // Emit empty list on failure to avoid breaking the flow
                    emit(emptyList())
                }
            }
            // Update every 5 seconds
            delay(5000)
        }
    }
}
