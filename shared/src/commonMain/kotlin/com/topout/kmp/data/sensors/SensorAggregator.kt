package com.topout.kmp.data.sensors

import com.topout.kmp.models.sensor.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock

// Collects raw flows, down-samples to 1 Hz, exposes combined sample
class SensorAggregator(
    private val accelFlow: Flow<AccelerationData>,
    private val altFlow: Flow<AltitudeData>,
    private val locFlow: Flow<LocationData>,
    private val hz: Long = 1_000L        // emit every 1 s
) {

    data class Aggregate(
        val accel: AccelerationData?,
        val altitude: AltitudeData?,
        val location: LocationData?,
        val ts: Long
    )

    private val _tick = MutableSharedFlow<Unit>(
        replay = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var sessionId: String? = null

    // Store latest values from each sensor
    private var latestAccel: AccelerationData? = null
    private var latestAlt: AltitudeData? = null
    private var latestLoc: LocationData? = null

    // public API – emits at fixed interval
    @OptIn(FlowPreview::class)
    val aggregateFlow: Flow<Aggregate> = _tick
        .sample(hz)                       // 1 Hz tick
        .map {
            Aggregate(
                accel = latestAccel,
                altitude = latestAlt,
                location = latestLoc,
                ts = Clock.System.now().toEpochMilliseconds()
            )
        }
        .filter { it.accel != null || it.altitude != null || it.location != null }

    fun setSessionId(sessionId: String?) {
        this.sessionId = sessionId
    }

    private var jobAccel: Job? = null
    private var jobAlt: Job? = null
    private var jobLoc: Job? = null
    private var jobTick: Job? = null

    suspend fun start(scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)) {
        // Collect from all sensor flows to keep latest values
        jobAccel = scope.launch { accelFlow.collect { latestAccel = it } }
        jobAlt   = scope.launch { altFlow.collect { latestAlt = it } }
        jobLoc   = scope.launch { locFlow.collect { latestLoc = it } }
        jobTick  = scope.launch {
            flow {
                while (true) {
                    emit(Unit)
                    kotlinx.coroutines.delay(hz)
                }
            }.collect { _tick.emit(it) }
        }
    }

    fun stop() {
        jobAccel?.cancel(); jobAccel = null
        jobAlt?.cancel(); jobAlt = null
        jobLoc?.cancel(); jobLoc = null
        jobTick?.cancel(); jobTick = null
    }
}
