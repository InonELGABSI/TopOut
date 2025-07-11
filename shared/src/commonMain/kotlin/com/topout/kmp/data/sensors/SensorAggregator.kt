package com.topout.kmp.data.sensors

import com.topout.kmp.models.sensor.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

// Collects raw flows, down-samples to 1 Hz, exposes combined sample
class SensorAggregator(
    accelFlow: Flow<AccelerationData>,
    altFlow: Flow<AltitudeData>,
    locFlow: Flow<LocationData>,
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

    // public API – emits at fixed interval
    @OptIn(FlowPreview::class)
    val aggregateFlow: Flow<Aggregate> = _tick
        .sample(hz)                       // 1 Hz tick
        .withLatestFrom(accelFlow, altFlow, locFlow) { _, a, h, g ->
            Aggregate(a, h, g, System.currentTimeMillis())
        }
        .filter { it.accel != null || it.altitude != null || it.location != null }

    suspend fun start() {
        flow { while (true) { emit(Unit); kotlinx.coroutines.delay(hz) } }
            .collect { _tick.emit(it) }
    }
}
