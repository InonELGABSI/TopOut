package com.topout.kmp.domain

import com.topout.kmp.data.Result
import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.data.sensors.SensorAggregator
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.models.Metrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class LiveSessionManager(
    private val sessionsRepo: SessionsRepository,
    private val dao: TrackPointsDao,
    private val aggregate: SensorAggregator,
    private val scope: CoroutineScope
) {
    private var tracker: SessionTracker? = null

    // main entry (still supports controller())
    suspend operator fun invoke(): Flow<Metrics> = start()

    // explicit start function
    private suspend fun start(): Flow<Metrics> {
        val result = sessionsRepo.createSession()
            ?: throw IllegalStateException("Failed to create session in LiveSessionController")
        when (result) {
            is Result.Success -> {
                val sessionId = result.data?.id
                aggregate.setSessionId(sessionId)
                aggregate.start(scope)
                tracker = sessionId?.let { SessionTracker(it, aggregate, dao, scope).also { it.start() } }
                return tracker!!.metrics
            }
            is Result.Failure -> {
                throw IllegalStateException("Failed to create session: ${result.error?.message}")
            }
        }
    }

    fun stop() {
        tracker?.stop()
        aggregate.stop()
        tracker = null
    }
}


