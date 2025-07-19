package com.topout.kmp.domain

import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.models.Metrics
import com.topout.kmp.models.AlertType
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.data.sensors.SensorAggregator
import com.topout.kmp.utils.RateCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import co.touchlab.kermit.Logger

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

    private val _trackPointFlow = MutableSharedFlow<TrackPoint>(replay = 0)
    val trackPointFlow: SharedFlow<TrackPoint> = _trackPointFlow.asSharedFlow()

    private var collectJob: Job? = null

    private var log = Logger.withTag("SessionTracker")

    fun start() {
        collectJob = scope.launch {
            aggregator.aggregateFlow.collect { sample ->
                // Prioritize GPS altitude when available, fallback to barometric
                val alt = sample.location?.altitude
//                val alt = sample.location?.altitude ?: sample.altitude?.altitude

                if (startAltitude.value == null && alt != null) {
                    startAltitude.value = alt
                    log.i { "Set start altitude to $alt meters for session $sessionId" }
                }

                val vVert = if (alt != null && lastAlt != null) {
                    RateCalculator.verticalSpeedMetersPerMinute(lastAlt!!, alt)
                } else 0.0

                val vHorizon = sample.location?.speed?.toDouble() ?: 0.0
                val vTotal = kotlin.math.sqrt(vVert * vVert + vHorizon * vHorizon)

                if (alt != null && lastAlt != null) {
                    val diff = alt - lastAlt!!
                    if (diff > 0) gain += diff else loss -= diff
                }

                vertDistSum += kotlin.math.abs(vVert)
                vertSampleCount++

                if (alt != null) {
                    lastAlt = alt
                }

                val relAltitude = if (alt != null && startAltitude.value != null)
                    alt - startAltitude.value!! else 0.0

                val avgVert = if (vertSampleCount > 0) vertDistSum / vertSampleCount else 0.0

                val danger = vVert > 600 || vTotal > 15
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

                // Build and persist TrackPoint
                val trackPoint = TrackPoint(
                    sessionId = sessionId,
                    timestamp = sample.ts,
                    latitude  = sample.location?.lat,
                    longitude = sample.location?.lon,
                    altitude  = alt,
                    accelerationX = sample.accel?.x,
                    accelerationY = sample.accel?.y,
                    accelerationZ = sample.accel?.z,
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
                dao.insertTrackPoint(
                    sessionId = trackPoint.sessionId,
                    ts = trackPoint.timestamp,
                    lat = trackPoint.latitude,
                    lon = trackPoint.longitude,
                    altitude = trackPoint.altitude,
                    accelX = trackPoint.accelerationX,
                    accelY = trackPoint.accelerationY,
                    accelZ = trackPoint.accelerationZ,
                    metrics = newMetrics
                )

                _trackPointFlow.emit(trackPoint)
            }
        }
    }

    fun stop() {
        collectJob?.cancel()
        collectJob = null
    }
}
