package com.topout.kmp.domain

import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.models.Metrics
import com.topout.kmp.models.AlertType
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.models.User
import com.topout.kmp.data.sensors.SensorAggregator
import com.topout.kmp.platform.NotificationController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import co.touchlab.kermit.Logger
import kotlin.math.*

private data class PointInfo(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val timestamp: Long // ms
)

class SessionTracker(
    private val sessionId: String,
    private val aggregator: SensorAggregator,
    private val dao: TrackPointsDao,
    private val scope: CoroutineScope,
    private val user: User? = null,
    private val notificationController: NotificationController? = null
) {
    // ---- Tunables ----
    private val WINDOW_POINTS = 5
    private val MIN_DT_SEC    = 2.0   // need ≥2s across window
    private val H_STATIONARY  = 2.0   // m; net horizontal below => 0 m/s
    private val V_STATIONARY  = 1.5   // m; net vertical below   => 0 m/s
    private val EMA_ALPHA     = 0.25  // smoothing for displayed speeds
    private val H_DEADBAND    = 0.30  // m/s; clamp tiny flicker
    private val V_DEADBAND    = 0.20  // m/s; clamp tiny flicker
    private val ALERT_RATE_LIMIT_MS = 60_000L // 1 minute between same alert type

    private val startAltitude = MutableStateFlow<Double?>(null)
    private var lastAlt: Double? = null
    private var gain = 0.0
    private var loss = 0.0

    private val lastPoints = ArrayDeque<PointInfo>(WINDOW_POINTS)

    private val _trackPointFlow = MutableSharedFlow<TrackPoint>(replay = 0)
    val trackPointFlow: SharedFlow<TrackPoint> = _trackPointFlow.asSharedFlow()

    private var collectJob: Job? = null
    private val log = Logger.withTag("SessionTracker")
    private val paused = MutableStateFlow(false)

    // EMA state for speeds
    private var emaH = 0.0
    private var emaV = 0.0
    private var emaInit = false

    // Alerts once-per-type
    private val lastAlertTimes = mutableMapOf<AlertType, Long>()

    fun pause() { paused.value = true }
    fun resume() { paused.value = false }

    fun start() {
        log.i { "start() for session $sessionId" }
        collectJob = scope.launch {
            aggregator.aggregateFlow.collect { sample ->
                if (paused.value) return@collect

                val lat = sample.location?.lat
                val lon = sample.location?.lon
                val alt = sample.location?.altitude ?: sample.altitude?.altitude
                val ts  = sample.ts

                if (alt != null && startAltitude.value == null) {
                    startAltitude.value = alt
                    emaInit = false
                    lastAlt = alt
                }

                if (lat != null && lon != null && alt != null) {
                    pushPoint(PointInfo(lat, lon, alt, ts))
                }

                val (avgH, avgV) = calculateSpeeds() // vertical always used (simulator ok)

                // Gain/Loss: raw, no smoothing
                if (alt != null && lastAlt != null) {
                    val dz = alt - lastAlt!!
                    if (dz > 0) gain += dz else loss -= dz
                    lastAlt = alt
                } else if (alt != null) {
                    lastAlt = alt
                }

                val relAltitude = if (alt != null && startAltitude.value != null)
                    alt - startAltitude.value!! else 0.0

                // Thresholds (null or <=0 means disabled except speed which falls back to 600 m/min)
                val relThr = user?.relativeHeightFromStartThr?.takeIf { it > 0 } ?: 0.0
                val totalThr = user?.totalHeightFromStartThr?.takeIf { it > 0 } ?: 0.0
                val vThrMin = user?.currentAvgHeightSpeedThr?.takeIf { it > 0 } ?: 600.0 // m/min fallback

                // Alerts
                val triggered = mutableListOf<AlertType>()
                if (abs(avgV * 60.0) > vThrMin) triggered.add(AlertType.RAPID_ASCENT)
                if (relThr > 0.0 && abs(relAltitude) > relThr) triggered.add(AlertType.RELATIVE_HEIGHT_EXCEEDED)
                if (totalThr > 0.0 && gain > totalThr) triggered.add(AlertType.TOTAL_HEIGHT_EXCEEDED)

                val danger = triggered.isNotEmpty()
                val primary = triggered.firstOrNull() ?: AlertType.NONE

                val metrics = Metrics(
                    gain = gain,
                    loss = loss,
                    relAltitude = relAltitude,
                    avgHorizontal = avgH,
                    avgVertical = avgV,
                    danger = danger,
                    alertType = primary
                )

                val tp = TrackPoint(
                    sessionId = sessionId,
                    timestamp = ts,
                    latitude  = lat,
                    longitude = lon,
                    altitude  = alt,
                    accelerationX = sample.accel?.x,
                    accelerationY = sample.accel?.y,
                    accelerationZ = sample.accel?.z,
                    gain = gain,
                    loss = loss,
                    relAltitude = relAltitude,
                    avgHorizontal = avgH,
                    avgVertical = avgV,
                    danger = danger,
                    alertType = primary
                )
                dao.insertTrackPoint(
                    sessionId = tp.sessionId,
                    ts = tp.timestamp,
                    lat = tp.latitude,
                    lon = tp.longitude,
                    altitude = tp.altitude,
                    accelX = tp.accelerationX,
                    accelY = tp.accelerationY,
                    accelZ = tp.accelerationZ,
                    metrics = metrics
                )

                triggered.forEach { a ->
                    // Try rate-limited send (always evaluate each cycle)
                    sendNotificationIfNeeded(a, relAltitude, gain, avgV, ts)
                }

                _trackPointFlow.emit(tp)
            }
        }
    }

    fun stop() {
        log.i { "stop() for session $sessionId" }
        collectJob?.cancel()
        collectJob = null
        lastAlertTimes.clear()
        lastPoints.clear()
        emaInit = false
    }

    // ---- Speed calculation (simple & real-time) ----
    private fun calculateSpeeds(): Pair<Double, Double> {
        if (lastPoints.size < 2) return emaH to emaV

        val first = lastPoints.first()
        val last  = lastPoints.last()
        val dt = (last.timestamp - first.timestamp) / 1000.0
        if (dt < MIN_DT_SEC) return emaH to emaV

        val dH = flatDistanceM(first.latitude, first.longitude, last.latitude, last.longitude)
        val dV = last.altitude - first.altitude

        val rawH = if (dH < H_STATIONARY) 0.0 else dH / dt
        val rawV = if (abs(dV) < V_STATIONARY) 0.0 else dV / dt

        // EMA + deadbands (speeds only)
        if (!emaInit) { emaH = rawH; emaV = rawV; emaInit = true }
        else {
            emaH = EMA_ALPHA * rawH + (1 - EMA_ALPHA) * emaH
            emaV = EMA_ALPHA * rawV + (1 - EMA_ALPHA) * emaV
        }
        if (emaH < H_DEADBAND) emaH = 0.0
        if (abs(emaV) < V_DEADBAND) emaV = 0.0

        return emaH to emaV
    }

    private fun pushPoint(p: PointInfo) {
        lastPoints.addLast(p)
        while (lastPoints.size > WINDOW_POINTS) lastPoints.removeFirst()
    }

    // Flat-earth distance (meters) for small deltas; faster than haversine
    private fun flatDistanceM(lat0: Double, lon0: Double, lat1: Double, lon1: Double): Double {
        val degToRad = PI / 180.0
        val latMid = (lat0 + lat1) * 0.5 * degToRad
        val kx = 111320.0 * cos(latMid)  // m/deg lon
        val ky = 110540.0                // m/deg lat
        val dx = (lon1 - lon0) * kx
        val dy = (lat1 - lat0) * ky
        return hypot(dx, dy)
    }

    // ---- Notifications ----
    private fun sendNotificationIfNeeded(
        alertType: AlertType,
        relAltitude: Double,
        totalHeight: Double,
        avgVert: Double,
        timestampMs: Long
    ) {
        if (alertType == AlertType.NONE) return
        val nc = notificationController ?: return
        if (!nc.areNotificationsEnabled()) return
        if (user?.enabledNotifications == false) return
        val lastTime = lastAlertTimes[alertType]
        if (lastTime != null && timestampMs - lastTime < ALERT_RATE_LIMIT_MS) return
        val (title, msg) = generateNotificationContent(alertType, relAltitude, totalHeight, avgVert)
        if (nc.sendAlertNotification(alertType, title, msg)) {
            lastAlertTimes[alertType] = timestampMs
        }
    }

    private fun generateNotificationContent(
        alertType: AlertType,
        relAltitude: Double,
        totalHeight: Double,
        avgVert: Double
    ): Pair<String, String> {
        return when (alertType) {
            AlertType.RAPID_ASCENT -> {
                val direction = if (avgVert > 0) "climbing" else "descending"
                val speed = abs(avgVert * 60.0).toInt() // m/min
                "Speed Alert" to "You're $direction at $speed m/min, exceeding your average speed threshold."
            }
            AlertType.RELATIVE_HEIGHT_EXCEEDED ->
                "Height Alert" to "You've reached ${relAltitude.toInt()} m above your start, exceeding your relative height threshold."
            AlertType.TOTAL_HEIGHT_EXCEEDED ->
                "Total Climb Alert" to "You've climbed ${totalHeight.toInt()} m total, exceeding your total height threshold."
            else -> "Climbing Alert" to "A threshold has been exceeded."
        }
    }
}
