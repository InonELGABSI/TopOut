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
        val sessionResult = remoteFirebaseRepository.getSessionById(sessionId)
        val pointsResult = remoteFirebaseRepository.getTrackPointsBySession(sessionId)

        return when {
            sessionResult is Result.Failure -> Result.Failure(sessionResult.error)
            pointsResult is Result.Failure -> Result.Failure(pointsResult.error)
            sessionResult is Result.Success && pointsResult is Result.Success -> {
                val session = sessionResult.data
                val points  = pointsResult.data
                return if (session != null && points != null) {
                    Result.Success(SessionDetails(session, points))
                } else {
                    Result.Failure(object : Error {
                        override val message = "Session or track points is null"
                    })
                }
            }
            else -> Result.Failure(object : Error {
                override val message = "Unknown error in com.topout.kmp.domain.GetSessionDetails"
            })
        }
    }
}
