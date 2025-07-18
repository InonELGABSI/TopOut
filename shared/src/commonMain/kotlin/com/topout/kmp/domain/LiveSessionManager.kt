package com.topout.kmp.domain

import com.topout.kmp.data.Result
import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.data.sensors.SensorAggregator
import com.topout.kmp.data.sensors.SensorDataSource
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.models.TrackPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class LiveSessionManager(
    private val sessionsRepo : SessionsRepository,
    private val dao          : TrackPointsDao,
    private val sensors      : SensorDataSource,
    private val scope        : CoroutineScope
) {
    private val aggregator = SensorAggregator(
        accelFlow = sensors.accelFlow,
        altFlow   = sensors.baroFlow,
        locFlow   = sensors.locFlow,
        hz = 1_000L           // Match location update rate - 1 second ticks
    )
    private var tracker: SessionTracker? = null

    suspend operator fun invoke(): Flow<TrackPoint> = start()

    private suspend fun start(): Flow<TrackPoint> {
        sensors.start(scope)
        val result = sessionsRepo.createSession()
            ?: error("Failed to create session (null Result)")
        return when (result) {
            is Result.Success -> {
                val sessionId = result.data?.id
                    ?: error("Session created but id == null")
                aggregator.setSessionId(sessionId)
                aggregator.start(scope)
                tracker = SessionTracker(sessionId, aggregator, dao, scope).apply { start() }
                tracker!!.trackPointFlow // this is a SharedFlow<TrackPoint>
            }
            is Result.Failure ->
                error("Failed to create session: ${result.error?.message}")
        }
    }

    fun stop() {
        tracker?.stop(); tracker = null
        aggregator.stop()
        sensors.stop()
    }
}
