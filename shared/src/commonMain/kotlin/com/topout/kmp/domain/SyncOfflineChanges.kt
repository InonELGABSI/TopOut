package com.topout.kmp.domain

import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.data.sessions.SessionsError
import com.topout.kmp.data.sessions.SyncType
import com.topout.kmp.data.track_points.TrackPointsRepository
import com.topout.kmp.data.user.UserRepository
import com.topout.kmp.data.Result
import com.topout.kmp.data.Error

class SyncOfflineChanges(
    private val sessionsRepository: SessionsRepository,
    private val remoteRepository: FirebaseRepository,
    private val trackPointsRepository: TrackPointsRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<SyncResult, SyncError> {
        return try {
            var sessionsCreatedCount = 0
            var sessionsUpdatedCount = 0
            var sessionsDeletedCount = 0
            var sessionsFailedCount = 0
            var usersUpdatedCount = 0
            var usersFailedCount = 0

            // 1️⃣ Sync the single user if updated offline
            val userResult = userRepository.getUser()
            when (userResult) {
                is Result.Success -> {
                    val user = userResult.data
                    if (user != null) {
                        if (user.userUpdatedOffline == true) {
                            try {
                                // Update user on remote
                                val updateUserResult = remoteRepository.updateUser(user)

                                if (updateUserResult is Result.Success) {
                                    // Mark user as synced locally
                                    userRepository.markUserAsSynced()
                                    usersUpdatedCount++
                                } else {
                                    usersFailedCount++
                                }
                            } catch (e: Exception) {
                                usersFailedCount++
                            }
                        }
                    }
                }
                is Result.Failure -> {
                    usersFailedCount++
                }
            }

            // 2️⃣ Get all sessions that need syncing
            val sessionsForSyncResult = sessionsRepository.getSessionsForSync()

            when (sessionsForSyncResult) {
                is Result.Success -> {
                    val sessionsToSync = sessionsForSyncResult.data?.items ?: emptyList()

                    for (session in sessionsToSync) {
                        when {
                            // 3️⃣ Handle sessions created offline
                            session.sessionCreatedOffline -> {
                                try {
                                    // Upload session to remote
                                    val createResult = remoteRepository.saveSession(session)

                                    if (createResult is Result.Success) {
                                        // Save the updated session (with userId and title) to local database
                                        val updatedSession = createResult.data
                                        if (updatedSession != null) {
                                            sessionsRepository.saveSession(updatedSession)
                                        }

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
                                        sessionsCreatedCount++
                                    } else {
                                        sessionsFailedCount++
                                    }
                                } catch (e: Exception) {
                                    sessionsFailedCount++
                                }
                            }

                            // 4️⃣ Handle sessions updated offline
                            session.sessionUpdatedOffline -> {
                                try {
                                    // Update session on remote
                                    val updateResult = remoteRepository.updateSession(session)

                                    if (updateResult is Result.Success) {
                                        // Mark session as no longer updated offline
                                        sessionsRepository.resolveLocalSync(session.id, SyncType.UPDATED_OFFLINE)
                                        sessionsUpdatedCount++
                                    } else {
                                        sessionsFailedCount++
                                    }
                                } catch (e: Exception) {
                                    sessionsFailedCount++
                                }
                            }

                            // 5️⃣ Handle sessions deleted offline
                            session.sessionDeletedOffline -> {
                                try {
                                    // Delete session from remote
                                    val deleteResult = remoteRepository.deleteSession(session.id)

                                    if (deleteResult is Result.Success) {
                                        // Remove session from local database permanently
                                        sessionsRepository.resolveLocalSync(session.id, SyncType.DELETED_OFFLINE)
                                        sessionsDeletedCount++
                                    } else {
                                        sessionsFailedCount++
                                    }
                                } catch (e: Exception) {
                                    sessionsFailedCount++
                                }
                            }
                        }
                    }

                    Result.Success(
                        SyncResult(
                            sessionsCreated = sessionsCreatedCount,
                            sessionsUpdated = sessionsUpdatedCount,
                            sessionsDeleted = sessionsDeletedCount,
                            sessionsFailed = sessionsFailedCount,
                            usersUpdated = usersUpdatedCount,
                            usersFailed = usersFailedCount,
                            totalProcessed = sessionsToSync.size + if (userResult is Result.Success && userResult.data?.userUpdatedOffline == true) 1 else 0
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
    val sessionsUpdated: Int,
    val sessionsDeleted: Int,
    val sessionsFailed: Int,
    val usersUpdated: Int,
    val usersFailed: Int,
    val totalProcessed: Int
) {
    val isSuccessful: Boolean = sessionsFailed == 0 && usersFailed == 0
    val hasChanges: Boolean = sessionsCreated > 0 || sessionsUpdated > 0 || sessionsDeleted > 0 || usersUpdated > 0
    val hasUserChanges: Boolean = usersUpdated > 0
}

data class SyncError(
    override val message: String
) : Error
