package com.topout.kmp.domain

import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.models.Metrics
import com.topout.kmp.models.AlertType
import com.topout.kmp.data.sensors.SensorAggregator
import com.topout.kmp.utils.RateCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SessionTracker(
    private val sessionId: String,
    private val aggregator: SensorAggregator,
    private val dao: TrackPointsDao,
    private val scope: CoroutineScope
) {

    private val startAltitude = MutableStateFlow<Double?>(null)
    private var lastAlt: Double? = null
    private var gain = 0.0
    private var loss = 0.0
    private var vertDistSum = 0.0
    private var vertSampleCount = 0

    // Stream exposed to ViewModel
    private val _metrics = MutableStateFlow(Metrics())
    val metrics: StateFlow<Metrics> = _metrics.asStateFlow()

    private var collectJob: Job? = null

    fun start() {
        collectJob = scope.launch {
            aggregator.aggregateFlow.collect { sample ->
                val alt = sample.altitude?.altitude ?: lastAlt
                if (startAltitude.value == null && alt != null) startAltitude.value = alt

                // ---- compute vertical speed ----
                val vVert = if (alt != null && lastAlt != null) {
                    RateCalculator.verticalSpeedMetersPerMinute(lastAlt!!, alt)
                } else 0.0
                lastAlt = alt

                // ---- compute horizontal speed ----
                val vHorizon = sample.location?.speed?.toDouble() ?: 0.0
                val vTotal = kotlin.math.sqrt(vVert * vVert + vHorizon * vHorizon)

                // ---- gain / loss ----
                if (alt != null && lastAlt != null) {
                    val diff = alt - lastAlt!!
                    if (diff > 0) gain += diff else loss -= diff
                }

                // ---- averages ----
                vertDistSum += kotlin.math.abs(vVert)
                vertSampleCount++

                val relAltitude = if (alt != null && startAltitude.value != null)
                    alt - startAltitude.value!! else 0.0

                val avgVert = if (vertSampleCount > 0) vertDistSum / vertSampleCount else 0.0

                val danger = vVert > 600 || vTotal > 15      // example thresholds
                val alertType = when {
                    vVert > 600 -> AlertType.RAPID_ASCENT
                    vVert < -600 -> AlertType.RAPID_DESCENT
                    else -> AlertType.NONE
                }

                val newMetrics = Metrics(
                    vVertical = vVert,
                    vHorizontal = vHorizon,
                    vTotal = vTotal,
                    gain = gain,
                    loss = loss,
                    relAltitude = relAltitude,
                    avgVertical = avgVert,
                    danger = danger,
                    alertType = alertType
                )
                _metrics.value = newMetrics

                // ---- persist sample with ALL sensor data ----
                dao.insertTrackPoint(
                    sessionId = sessionId,
                    ts = sample.ts,
                    lat = sample.location?.lat,
                    lon = sample.location?.lon,
                    altitude = alt,
                    accelX = sample.accel?.x,
                    accelY = sample.accel?.y,
                    accelZ = sample.accel?.z,
                    metrics = newMetrics
                )
            }
        }
    }

    fun stop() {
        collectJob?.cancel()
        collectJob = null
    }
}
