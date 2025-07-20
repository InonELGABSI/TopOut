package com.topout.kmp.domain

import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.data.sessions.SessionsError
import com.topout.kmp.data.sessions.SyncType
import com.topout.kmp.data.track_points.TrackPointsRepository
import com.topout.kmp.data.Result
import com.topout.kmp.data.Error

class SyncOfflineChanges(
    private val sessionsRepository: SessionsRepository,
    private val remoteRepository: FirebaseRepository,
    private val trackPointsRepository: TrackPointsRepository
) {
    suspend operator fun invoke(): Result<SyncResult, SyncError> {
        return try {
            // 1️⃣ Get all sessions that need syncing
            val sessionsForSyncResult = sessionsRepository.getSessionsForSync()

            when (sessionsForSyncResult) {
                is Result.Success -> {
                    val sessionsToSync = sessionsForSyncResult.data?.items ?: emptyList()
                    var createdCount = 0
                    var deletedCount = 0
                    var failedCount = 0

                    for (session in sessionsToSync) {
                        when {
                            // 2️⃣ Handle sessions created offline
                            session.sessionCreatedOffline == true -> {
                                try {
                                    // Upload session to remote
                                    val createResult = remoteRepository.createSession(session)

                                    if (createResult is Result.Success) {
                                        // Get track points for this session
                                        val trackPointsResult = trackPointsRepository.getBySession(session.id)

                                        if (trackPointsResult is Result.Success) {
                                            val trackPoints = trackPointsResult.data

                                            // Upload track points if they exist
                                            if (!trackPoints.isNullOrEmpty()) {
                                                remoteRepository.pushTrackPoints(session.id, trackPoints)
                                            }

                                            // Clean up local track points after successful upload
                                            trackPointsRepository.deleteBySession(session.id)
                                        }

                                        // Mark session as no longer created offline
                                        sessionsRepository.resolveLocalSync(session.id, SyncType.CREATED_OFFLINE)
                                        createdCount++
                                    } else {
                                        failedCount++
                                    }
                                } catch (e: Exception) {
                                    failedCount++
                                }
                            }

                            // 3️⃣ Handle sessions deleted offline
                            session.sessionDeletedOffline == true -> {
                                try {
                                    // Delete session from remote
                                    val deleteResult = remoteRepository.deleteSession(session.id)

                                    if (deleteResult is Result.Success) {
                                        // Remove session from local database permanently
                                        sessionsRepository.resolveLocalSync(session.id, SyncType.DELETED_OFFLINE)
                                        deletedCount++
                                    } else {
                                        failedCount++
                                    }
                                } catch (e: Exception) {
                                    failedCount++
                                }
                            }
                        }
                    }

                    Result.Success(
                        SyncResult(
                            sessionsCreated = createdCount,
                            sessionsDeleted = deletedCount,
                            sessionsFailed = failedCount,
                            totalProcessed = sessionsToSync.size
                        )
                    )
                }
                is Result.Failure -> {
                    Result.Failure(SyncError("Failed to get sessions for sync: ${sessionsForSyncResult.error?.message}"))
                }
            }
        } catch (e: Exception) {
            Result.Failure(SyncError("Sync operation failed: ${e.message}"))
        }
    }
}

data class SyncResult(
    val sessionsCreated: Int,
    val sessionsDeleted: Int,
    val sessionsFailed: Int,
    val totalProcessed: Int
) {
    val isSuccessful: Boolean = sessionsFailed == 0
    val hasChanges: Boolean = sessionsCreated > 0 || sessionsDeleted > 0
}

data class SyncError(
    override val message: String
) : Error
