package com.topout.kmp.domain

import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.models.Metrics
import com.topout.kmp.models.AlertType
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.models.User
import com.topout.kmp.data.sensors.SensorAggregator
import com.topout.kmp.platform.NotificationController
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
    private val user: User? = null, // Add user preferences
    private val notificationController: NotificationController? = null
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

    // Track which alert types have already been notified for this session
    private val notifiedAlertTypes = mutableSetOf<AlertType>()

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

                // Check all applicable alert types for this track point
                val triggeredAlerts = mutableListOf<AlertType>()

                // Check average vertical speed threshold
                if (kotlin.math.abs(avgVert) > avgSpeedThreshold) {
                    triggeredAlerts.add(AlertType.RAPID_ASCENT)
                    log.i { "Speed threshold exceeded: avgVert=${avgVert}, threshold=${avgSpeedThreshold}" }
                }

                // Check relative altitude threshold (if user has set it)
                if (relativeHeightThreshold > 0.0 && kotlin.math.abs(relAltitude) > relativeHeightThreshold) {
                    triggeredAlerts.add(AlertType.RELATIVE_HEIGHT_EXCEEDED)
                    log.i { "Relative height threshold exceeded: relAltitude=${relAltitude}, threshold=${relativeHeightThreshold}" }
                }

                // Check total height threshold (if user has set it)
                if (totalHeightThreshold > 0.0 && gain > totalHeightThreshold) {
                    triggeredAlerts.add(AlertType.TOTAL_HEIGHT_EXCEEDED)
                    log.i { "Total height threshold exceeded: totalHeight=${gain}, threshold=${totalHeightThreshold}" }
                }

                // Determine if any danger exists and primary alert type for metrics
                val danger = triggeredAlerts.isNotEmpty()
                val primaryAlertType = triggeredAlerts.firstOrNull() ?: AlertType.NONE

                val newMetrics = Metrics(
                    vVertical = vVert,
                    vHorizontal = vHorizon,
                    vTotal = vTotal,
                    gain = gain,
                    loss = loss,
                    relAltitude = relAltitude,
                    avgVertical = avgVert,
                    danger = danger,
                    alertType = primaryAlertType
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
                    alertType = primaryAlertType
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

                // Send notifications for ALL triggered alerts that haven't been sent yet
                triggeredAlerts.forEach { alertType ->
                    if (!notifiedAlertTypes.contains(alertType)) {
                        sendNotificationIfNeeded(alertType, relAltitude, gain, avgVert)
                    }
                }

                _trackPointFlow.emit(trackPoint)
            }
        }
    }

    private fun sendNotificationIfNeeded(
        alertType: AlertType,
        relAltitude: Double,
        totalHeight: Double,
        avgVert: Double
    ) {
        log.i { "sendNotificationIfNeeded called - alertType: $alertType, already notified: ${notifiedAlertTypes.contains(alertType)}" }

        // Only send notifications if:
        // 1. We haven't sent one for this alert type yet
        // 2. The alert type is not NONE
        if (notifiedAlertTypes.contains(alertType) || alertType == AlertType.NONE) {
            log.i { "Skipping notification - already sent or NONE type" }
            return
        }

        // Log user notification preferences
        val notificationsEnabled = user?.enabledNotifications ?: false
        log.i { "User notifications enabled: $notificationsEnabled, user exists: ${user != null}" }

        if (notificationController == null) {
            log.w { "NotificationController is null - cannot send notifications" }
            return
        }

        // Check system notification permissions
        val systemEnabled = notificationController.areNotificationsEnabled()
        log.i { "System notifications enabled: $systemEnabled" }

        if (!systemEnabled) {
            log.w { "System notifications not enabled - skipping notification" }
            return
        }

        // For debugging: send notification even if user preference is disabled
        // Remove this condition in production if you want to respect user preference
        if (!notificationsEnabled) {
            log.w { "User has disabled notifications in settings, but sending anyway for debugging" }
            // Don't return here for debugging purposes
        }

        // Generate notification content based on the alert type
        val (title, message) = generateNotificationContent(alertType, relAltitude, totalHeight, avgVert)
        log.i { "Sending notification: title='$title', message='$message'" }

        // Send the notification
        val sent = notificationController.sendAlertNotification(alertType, title, message)
        if (sent) {
            notifiedAlertTypes.add(alertType)
            log.i { "✅ Notification sent successfully for session $sessionId with alert type $alertType" }
        } else {
            log.e { "❌ Failed to send notification for session $sessionId with alert type $alertType" }
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
                // This now represents the Average Speed threshold (both ascent/descent)
                val direction = if (avgVert > 0) "climbing" else "descending"
                val speed = kotlin.math.abs(avgVert.toInt())
                Pair(
                    "Speed Alert",
                    "You're $direction at $speed m/min, which exceeds your average speed threshold."
                )
            }
            AlertType.RELATIVE_HEIGHT_EXCEEDED -> {
                Pair(
                    "Height Alert",
                    "You've reached ${relAltitude.toInt()} m above your starting point, exceeding your relative height threshold."
                )
            }
            AlertType.TOTAL_HEIGHT_EXCEEDED -> {
                Pair(
                    "Total Climb Alert",
                    "You've climbed a total of ${totalHeight.toInt()} m during this session, exceeding your total height threshold."
                )
            }
            else -> Pair("Climbing Alert", "A threshold has been exceeded in your climbing session.")
        }
    }

    fun stop() {
        log.i { "stop() for session $sessionId" }
        collectJob?.cancel()
        collectJob = null
        // Reset notification sent status when stopping
        notifiedAlertTypes.clear()
    }
}
