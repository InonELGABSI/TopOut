package com.topout.kmp.domain

import com.topout.kmp.data.Result
import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.data.sensors.SensorAggregator
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.models.Metrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class StartSession(
    private val sessionsRepo: SessionsRepository,
    private val dao: TrackPointsDao,
    private val aggreg: SensorAggregator,
    private val scope: CoroutineScope
) {
    private var tracker: SessionTracker? = null

    suspend operator fun invoke(): Flow<Metrics> {
        // 1. create session row → returns sessionId
        val result = sessionsRepo.createSession()
            ?: throw IllegalStateException("Failed to create session in StartSession")
        when (result) {
            is Result.Success -> {
                val sessionId = result.data?.id
                // 1.1 store sessionId in aggregator
                aggreg.setSessionId(sessionId)

                // 2. start aggregator timers
                scope.launch { aggreg.start() }

                // 3. spin tracker
                tracker = SessionTracker(sessionId, aggreg, dao, scope).also { it.start() }

                // 4. return metrics flow for UI
                return tracker!!.metrics
            }
            is Result.Failure -> {
                throw IllegalStateException("Failed to create session: ${result.error?.message}")
            }
        }
    }

    fun stop() { tracker?.stop() }
}
