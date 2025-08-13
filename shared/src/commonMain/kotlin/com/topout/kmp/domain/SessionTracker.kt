package com.topout.kmp.domain

import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.models.Metrics
import com.topout.kmp.models.AlertType
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.models.User
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
    private val scope: CoroutineScope,
    private val user: User? = null // Add user preferences
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

    private val paused = MutableStateFlow(false)

    fun pause() { paused.value = true }
    fun resume() { paused.value = false }

    fun start() {
        log.i { "start() for session $sessionId" }
        collectJob = scope.launch {
            aggregator.aggregateFlow.collect { sample ->
                if (paused.value) return@collect
                // Prioritize GPS altitude when available, fallback to barometric
                val alt = sample.location?.altitude ?: sample.altitude?.altitude

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

                // Use user thresholds or defaults
                val relativeHeightThreshold = user?.relativeHeightFromStartThr ?: 0.0
                val totalHeightThreshold = user?.totalHeightFromStartThr ?: 0.0
                val avgSpeedThreshold = user?.currentAvgHeightSpeedThr ?: 600.0

                // Enhanced danger detection using user thresholds
                val danger = checkDangerConditions(
                    vVert = vVert,
                    relAltitude = relAltitude,
                    totalHeight = alt ?: 0.0,
                    avgVert = avgVert,
                    relativeHeightThreshold = relativeHeightThreshold,
                    totalHeightThreshold = totalHeightThreshold,
                    avgSpeedThreshold = avgSpeedThreshold
                )

                val alertType = determineAlertType(
                    relAltitude = relAltitude,
                    totalHeight = alt ?: 0.0,
                    avgVert = avgVert,
                    relativeHeightThreshold = relativeHeightThreshold,
                    totalHeightThreshold = totalHeightThreshold,
                    avgSpeedThreshold = avgSpeedThreshold
                )

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

    private fun checkDangerConditions(
        vVert: Double,
        relAltitude: Double,
        totalHeight: Double,
        avgVert: Double,
        relativeHeightThreshold: Double,
        totalHeightThreshold: Double,
        avgSpeedThreshold: Double
    ): Boolean {
        return when {
            // Check vertical speed threshold
            kotlin.math.abs(vVert) > avgSpeedThreshold -> true
            // Check relative altitude threshold (if user has set it)
            relativeHeightThreshold > 0.0 && kotlin.math.abs(relAltitude) > relativeHeightThreshold -> true
            // Check total height threshold (if user has set it)
            totalHeightThreshold > 0.0 && totalHeight > totalHeightThreshold -> true
            else -> false
        }
    }

    private fun determineAlertType(
        relAltitude: Double,
        totalHeight: Double,
        avgVert: Double,
        relativeHeightThreshold: Double,
        totalHeightThreshold: Double,
        avgSpeedThreshold: Double
    ): AlertType {
        return when {
            // Priority 1: Rapid vertical speed alerts (most critical)
            avgVert > avgSpeedThreshold -> AlertType.RAPID_ASCENT
            avgVert < -avgSpeedThreshold -> AlertType.RAPID_DESCENT

            // Priority 2: Relative height from start threshold
            relativeHeightThreshold > 0.0 && kotlin.math.abs(relAltitude) > relativeHeightThreshold ->
                AlertType.RELATIVE_HEIGHT_EXCEEDED

            // Priority 3: Total height threshold
            totalHeightThreshold > 0.0 && totalHeight > totalHeightThreshold ->
                AlertType.TOTAL_HEIGHT_EXCEEDED

            else -> AlertType.NONE
        }
    }

    fun stop() {
        log.i { "stop() for session $sessionId" }
        collectJob?.cancel()
        collectJob = null
    }
}
