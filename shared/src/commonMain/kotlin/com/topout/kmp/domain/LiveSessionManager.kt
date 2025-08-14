package com.topout.kmp.domain

import com.topout.kmp.data.Result
import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.data.sensors.SensorAggregator
import com.topout.kmp.data.sensors.SensorDataSource
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.models.TrackPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import co.touchlab.kermit.Logger
import com.topout.kmp.data.user.UserRepository
import com.topout.kmp.platform.NotificationController
import com.topout.kmp.platform.SessionBackgroundManager


class LiveSessionManager(
    private val sessionsRepo : SessionsRepository,
    private val dao          : TrackPointsDao,
    private val sensors      : SensorDataSource,
    private val scope        : CoroutineScope,
    private val localUserRepository: UserRepository,
    private val sessionBackgroundManager: SessionBackgroundManager,
    private val notificationController: NotificationController? = null
) {
    private val log = Logger.withTag("LiveSessionManager")
    private var tracker: SessionTracker? = null
    private var aggregator: SensorAggregator? = null

    suspend operator fun invoke(): Flow<TrackPoint> = start()

    private suspend fun start(): Flow<TrackPoint> {
        log.i { "start()" }

        // Use background scope for sensors to survive app lifecycle changes
        val effectiveScope = try {
            sessionBackgroundManager.getBackgroundScope() ?: scope
        } catch (_: Exception) {
            // Fallback to regular scope if background manager not available (e.g., on iOS)
            scope
        }

        sensors.start(effectiveScope)
        aggregator?.stop()

        // Get current user preferences for threshold-based alerts
        val user = try {
            when (val result = localUserRepository.getUser()) {
                is Result.Success -> result.data
                is Result.Failure -> {
                    log.w { "Failed to get user preferences: ${result.error?.message}, using defaults" }
                    null
                }
            }
        } catch (e: Exception) {
            log.w { "Error retrieving user preferences: ${e.message}, using defaults" }
            null
        }

        val result = sessionsRepo.createSession()
        return when (result) {
            is Result.Success -> {
                val sessionId = result.data?.id ?: error("Session created but id == null")
                log.i { "Session created with id: $sessionId" }

                // Log user thresholds for debugging
                user?.let {
                    log.i { "Using user thresholds - Height: ${it.relativeHeightFromStartThr}, Total: ${it.totalHeightFromStartThr}, Speed: ${it.currentAvgHeightSpeedThr}" }
                    log.i { "Notifications enabled: ${it.enabledNotifications}" }
                } ?: log.i { "Using default thresholds (no user preferences found)" }

                // Fresh aggregator for each session - USE BACKGROUND SCOPE
                val aggregator = SensorAggregator(
                    accelFlow = sensors.accelFlow,
                    altFlow   = sensors.baroFlow,
                    locFlow   = sensors.locFlow,
                    hz = 1000L
                )
                aggregator.setSessionId(sessionId)
                aggregator.start(effectiveScope) // Use background scope here too
                this.aggregator = aggregator

                // SessionTracker should also use background scope to survive app lifecycle
                tracker = SessionTracker(
                    sessionId = sessionId,
                    aggregator = aggregator,
                    dao = dao,
                    scope = effectiveScope,
                    user = user,
                    notificationController = notificationController
                ).apply { start() }

                tracker!!.trackPointFlow
            }
            is Result.Failure -> {
                error("Failed to create session: ${result.error?.message}")
            }
        }
    }

    fun stop() {
        tracker?.stop()
        tracker = null
        aggregator?.stop()
        aggregator = null
        sensors.stop()
    }

    fun pause() { tracker?.pause() }
    fun resume() { tracker?.resume() }

}