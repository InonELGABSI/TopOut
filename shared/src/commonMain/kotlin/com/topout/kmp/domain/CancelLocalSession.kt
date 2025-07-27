package com.topout.kmp.domain

import com.topout.kmp.data.Result
import com.topout.kmp.data.track_points.TrackPointsRepository
import com.topout.kmp.data.sessions.SessionsRepository

class CancelLocalSession(
    private val localPointsRepo: TrackPointsRepository,
    private val sessionsRepository: SessionsRepository
) {
    suspend operator fun invoke(sessionId: String) {
        // 1. Delete all track points for this session
        when (val pointsResult = localPointsRepo.deleteBySession(sessionId)) {
            is Result.Success -> {
                // Track points deleted successfully, proceed to delete session
            }
            is Result.Failure -> {
                throw Exception("Failed to delete track points: ${pointsResult.error?.message}")
            }
        }

        // 2. Delete the session from local database (hard delete, no remote sync)
        when (val sessionResult = sessionsRepository.deleteSession(sessionId)) {
            is Result.Success -> {
                // Session deleted successfully
            }
            is Result.Failure -> {
                throw Exception("Failed to delete session: ${sessionResult.error?.message}")
            }
        }
    }
}
