// shared/src/commonMain/kotlin/com/topout/kmp/domain/session/FinishSession.kt
package com.topout.kmp.domain.session

import com.topout.kmp.data.Result
import com.topout.kmp.data.dao.SessionDao
import com.topout.kmp.data.track_points.TrackPointsRepository
import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.models.SessionDetails
import com.topout.kmp.utils.extensions.*
import kotlinx.datetime.Clock

class FinishSession(
    private val sessionDao         : SessionDao,
    private val localPointsRepo    : TrackPointsRepository,
    private val firebaseRepository : FirebaseRepository
) {
    suspend operator fun invoke(sessionId: String): SessionDetails {
        // 1️⃣ Pull local points
        val points = when (val r = localPointsRepo.getBySession(sessionId)) {
            is Result.Success -> r.data
                ?: throw IllegalStateException("Null track-points list")
            is Result.Failure -> throw IllegalStateException(
                "Could not load track-points: ${r.error?.message}"
            )
        }

        // 2️⃣ Compute summary fields - using EXTENSIONS
        val gain    = points.totalAscent()
        val loss    = points.totalDescent()
        val maxAlt  = points.maxAltitude()
        val minAlt  = points.minAltitude()
        val avgVert = points.avgVerticalSpeed()
        val endTime = Clock.System.now().toEpochMilliseconds()

        // 3️⃣ Update LOCAL session row
        sessionDao.updateSessionSummary(
            id            = sessionId,
            endTime       = endTime,
            totalAscent   = gain,
            totalDescent  = loss,
            maxAltitude   = maxAlt,
            minAltitude   = minAlt,
            avgRate       = avgVert
        )
        val updated = sessionDao.getSessionById(sessionId)

        // 4️⃣ Try to PUSH to FIRESTORE via FirebaseRepository
        when (val saveResult = firebaseRepository.saveSession(updated)) {
            is Result.Success -> {
                // Save the updated session (with userId and title) to local database
                val updatedSessionFromRemote = saveResult.data
                if (updatedSessionFromRemote != null) {
                    sessionDao.saveSession(updatedSessionFromRemote)
                }

                // Session save succeeded, now try to push track points
                when (val pushResult = firebaseRepository.pushTrackPoints(sessionId, points)) {
                    is Result.Success -> {
                        // 5️⃣ Remote sync SUCCESS - clean up local points
                        localPointsRepo.deleteBySession(sessionId)
                    }
                    is Result.Failure -> {
                        // 6️⃣ Track points push FAILED - mark as offline created session
                        sessionDao.markSessionCreatedOffline(sessionId)
                        // Keep track points in DB for later sync - DO NOT delete them
                    }
                }
            }
            is Result.Failure -> {
                // 6️⃣ Session save FAILED - mark as offline created session
                sessionDao.markSessionCreatedOffline(sessionId)
                // Keep track points in DB for later sync - DO NOT delete them
            }
        }

        // 7️⃣ RETURN combined DTO
        return SessionDetails(updated, points)
    }
}
