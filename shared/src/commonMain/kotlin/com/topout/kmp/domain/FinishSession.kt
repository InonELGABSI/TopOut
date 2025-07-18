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

        // 4️⃣ PUSH to FIRESTORE via FirebaseRepository
        firebaseRepository.createSession(updated)
        firebaseRepository.pushTrackPoints(sessionId, points) // sub-collection

        // 5️⃣ CLEAN UP local points
        // (optional—errors here are non-fatal)
        localPointsRepo.deleteBySession(sessionId)

        // 6️⃣ RETURN combined DTO
        return SessionDetails(updated, points)
    }
}
