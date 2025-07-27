package com.topout.kmp.domain

import com.topout.kmp.data.Result
import com.topout.kmp.data.Error
import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.data.track_points.TrackPointsRepository
import com.topout.kmp.models.SessionDetails

class GetSessionDetails(
    private val remoteFirebaseRepository: FirebaseRepository,
    private val sessionsRepo: SessionsRepository,
    private val pointsRepo: TrackPointsRepository
) {
    suspend operator fun invoke(sessionId: String): Result<SessionDetails, Error> {
        // Step 1: Always try to get local data first
        val localSessionResult = sessionsRepo.getSessionById(sessionId)
        val localPointsResult = pointsRepo.getBySession(sessionId)

        var finalSession = if (localSessionResult is Result.Success) {
            localSessionResult.data
        } else null

        var finalPoints = if (localPointsResult is Result.Success) {
            localPointsResult.data ?: emptyList()
        } else emptyList()

        // Step 2: Determine what's missing and try to fetch from remote
        val needsSession = finalSession == null
        val needsPoints = finalPoints.isEmpty() // Could also check if points seem incomplete
        //val needsPoints = true // test failure result answer from firebase

        if (needsSession || needsPoints) {
            try {
                // Only fetch what's missing
                if (needsSession) {
                    val remoteSessionResult = remoteFirebaseRepository.getSessionById(sessionId)
                    if (remoteSessionResult is Result.Success && remoteSessionResult.data != null) {
                        finalSession = remoteSessionResult.data
                    }
                }

                if (needsPoints) {
                    val remotePointsResult = remoteFirebaseRepository.getTrackPointsBySession(sessionId)
                    if (remotePointsResult is Result.Success) {
                        finalPoints = remotePointsResult.data ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                // Network error - continue with whatever local data we have
                // finalSession and finalPoints already contain local data or defaults
            }
        }

        // Step 3: Return the best combination of data we have
        return if (finalSession != null) {
            Result.Success(SessionDetails(finalSession, finalPoints))
        } else {
            Result.Failure(object : Error {
                override val message = "Session not found locally or remotely"
            })
        }
    }
}
