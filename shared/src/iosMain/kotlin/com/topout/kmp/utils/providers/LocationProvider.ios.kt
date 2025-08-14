package com.topout.kmp.utils.providers

import com.topout.kmp.models.sensor.LocationData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.*
import platform.CoreLocation.*
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.NSSelectorFromString
import platform.darwin.NSObject
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import platform.Foundation.NSNotification
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.Foundation.NSNotificationCenter
import platform.UIKit.*
import platform.darwin.NSObjectProtocol
import platform.Foundation.NSDate
import platform.Foundation.NSProcessInfo

actual class LocationProvider {
    private val delegate = CoreLocationDelegate()

    actual suspend fun getLocation(): LocationData = delegate.getSingleLocation()
    fun locationFlow(): Flow<LocationData> = delegate.locationFlow()
    fun startUpdatingLocation() = delegate.startUpdatingLocation()
    fun stopUpdatingLocation() = delegate.stopUpdatingLocation()
    fun beginActiveSession() = delegate.beginActiveSession()
    fun endActiveSession() = delegate.endActiveSession()
    fun upgradeToAlwaysAuthorization() = delegate.upgradeToAlwaysAuthorization()
    fun dispose() = delegate.dispose()
}

private class CoreLocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {

    private val manager = CLLocationManager().apply {
        desiredAccuracy = kCLLocationAccuracyBest
        distanceFilter = 5.0
        activityType = CLActivityTypeFitness
        allowsBackgroundLocationUpdates = true
        pausesLocationUpdatesAutomatically = false
        showsBackgroundLocationIndicator = false // enable only when actively tracking in background
    }

    private val log = Logger.withTag("LocationProvider")
    private var trackingActive = false
    private var isInBackground = false
    private var isUpdating = false
    private var singleLocationCont: CancellableContinuation<LocationData>? = null
    private val _locationFlow = MutableSharedFlow<LocationData>(replay = 1)
    private val locationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var retryCount = 0
    private var requestSeq = 0
    private val singleRequestTimeoutMs = 10_000L
    private var backoffMs = 2000L
    private val maxBackoffMs = 60_000L

    private var initialStatusLogged = false
    private var didBecomeActiveObs: NSObjectProtocol? = null
    private var didEnterBgObs: NSObjectProtocol? = null

    // Proactive stale refresh threshold
    private val staleMs = 10_000L

    // Simulation handling
    private val isSimulator = NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null
    private var simulationJump = 0
    private val baseSimulationAltitude = 100.0

    init {
        manager.delegate = this
        log.i { "Init CoreLocationDelegate. Deferring status query until authorization callback" }

        // Log simulation status
        if (isSimulator) {
            log.i { "Simulator detected: Altitude simulation enabled (base=${baseSimulationAltitude}m, increment=10m)" }
        } else {
            log.d { "Real device detected: Using actual GPS altitude data" }
        }

        val center = NSNotificationCenter.defaultCenter
        didBecomeActiveObs = center.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null
        ) { _: NSNotification? -> runOnMain { handleEnterForeground() } }
        didEnterBgObs = center.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = null
        ) { _: NSNotification? -> runOnMain { handleEnterBackground() } }
    }

    private fun nowMs(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

    fun beginActiveSession() {
        if (!trackingActive) {
            trackingActive = true
            log.i { "Active tracking session began" }
            adjustAccuracy(foreground = !isInBackground)
            updateBackgroundIndicator()
        }
    }

    fun endActiveSession() {
        if (trackingActive) {
            trackingActive = false
            log.i { "Active tracking session ended" }
            updateBackgroundIndicator()
            adjustAccuracy(foreground = !isInBackground)
            // Reset simulation jump counter for next session
            if (isSimulator) {
                simulationJump = 0
                log.d { "Reset simulation jump counter for next session" }
            }
        }
    }

    fun upgradeToAlwaysAuthorization() {
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse -> {
                log.i { "Requesting upgrade to ALWAYS auth (user initiated)" }
                manager.requestAlwaysAuthorization()
            }
            kCLAuthorizationStatusNotDetermined -> {
                log.i { "Cannot upgrade yet; first request when-in-use" }
            }
            else -> log.d { "No upgrade needed state=${statusString(manager.authorizationStatus)}" }
        }
    }

    fun dispose() { stopUpdatingLocation() }

    fun locationFlow(): Flow<LocationData> = _locationFlow

    @OptIn(ExperimentalForeignApi::class)
    suspend fun getSingleLocation(): LocationData {
        log.i { "getSingleLocation() called. auth=${statusString(manager.authorizationStatus)} accuracy=${accuracyStatusString(manager)}" }

        _locationFlow.replayCache.lastOrNull()?.let { last ->
            if (isUpdating && (nowMs() - last.ts) <= 5_000) {
                log.d { "Returning recent fix instead of issuing single request" }
                return last
            }
        }

        val auth = manager.authorizationStatus
        when (auth) {
            kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> {
                log.e { "Location permission not granted (auth=${statusString(auth)})" }
                throw IllegalStateException("Location permission not granted")
            }
            kCLAuthorizationStatusNotDetermined -> {
                log.i { "Requesting when-in-use authorization (not determined)" }
                return suspendCancellableCoroutine { cont ->
                    singleLocationCont = cont
                    runOnMain { manager.requestWhenInUseAuthorization() }
                    cont.invokeOnCancellation { singleLocationCont = null }
                }
            }
            else -> Unit
        }

        log.i { "Already authorized (auth=${statusString(manager.authorizationStatus)}) → requesting single location with ${singleRequestTimeoutMs}ms timeout" }
        return try {
            withTimeout(singleRequestTimeoutMs) {
                suspendCancellableCoroutine { cont ->
                    singleLocationCont = cont
                    val seq = ++requestSeq
                    log.d { "requestLocation(single seq=$seq)" }
                    runOnMain { manager.requestLocation() }
                    cont.invokeOnCancellation { singleLocationCont = null; log.i { "Single location continuation cancelled seq=$seq" } }
                }
            }
        } catch (t: TimeoutCancellationException) {
            log.e { "Single location request timed out after ${singleRequestTimeoutMs}ms" }
            throw t
        }
    }

    fun startUpdatingLocation() {
        log.i { "startUpdatingLocation() auth=${statusString(manager.authorizationStatus)}" }
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusNotDetermined -> {
                log.i { "Requesting when-in-use auth (first step)" }
                manager.requestWhenInUseAuthorization()
            }
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> startStandardUpdates()
            else -> log.e { "Cannot start updates. Permission state=${statusString(manager.authorizationStatus)}" }
        }
    }

    private fun startStandardUpdates() {
        adjustAccuracy(foreground = !isInBackground)
        if (!isUpdating) {
            log.i { "Starting continuous updates (foreground=${!isInBackground})" }
            manager.startUpdatingLocation()
            isUpdating = true
        } else log.d { "Continuous updates already active" }
    }

    private fun handleEnterBackground() {
        isInBackground = true
        log.d { "Entered background; applying background profile" }
        adjustAccuracy(foreground = false)
        updateBackgroundIndicator()
        // Diagnostics snapshot
        val lastTs = _locationFlow.replayCache.lastOrNull()?.ts ?: -1L
        val age = if (lastTs > 0) nowMs() - lastTs else -1L
        log.i {
            "BG diag: tracking=$trackingActive updating=$isUpdating auth=${statusString(manager.authorizationStatus)} " +
            "desiredAcc=${manager.desiredAccuracy} distFilter=${manager.distanceFilter} indicator=${manager.showsBackgroundLocationIndicator} " +
            "lastFixTs=$lastTs ageMs=$age"
        }
    }

    private fun handleEnterForeground() {
        isInBackground = false
        log.d { "Entered foreground; applying foreground profile" }
        adjustAccuracy(foreground = true)
        updateBackgroundIndicator()
        if (!isUpdating && (manager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways || manager.authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse)) {
            startStandardUpdates()
        }
    }

    private fun updateBackgroundIndicator() {
        val shouldShow = trackingActive && isInBackground && manager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways
        if (manager.showsBackgroundLocationIndicator != shouldShow) {
            manager.showsBackgroundLocationIndicator = shouldShow
            log.d { "Background indicator set=$shouldShow" }
        }
    }

    private fun adjustAccuracy(foreground: Boolean) {
        if (trackingActive) {
            if (foreground) {
                manager.desiredAccuracy = kCLLocationAccuracyBestForNavigation
                manager.distanceFilter = 5.0
            } else {
                manager.desiredAccuracy = kCLLocationAccuracyBest
                manager.distanceFilter = 10.0
            }
        } else { // passive mode
            manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
            manager.distanceFilter = 100.0
        }
        log.d { "Adjusted accuracy fg=$foreground tracking=$trackingActive desired=${manager.desiredAccuracy} distanceFilter=${manager.distanceFilter}" }
    }

    fun stopUpdatingLocation() {
        log.i { "stopUpdatingLocation()" }
        runOnMain {
            if (isUpdating) {
                manager.stopUpdatingLocation()
                isUpdating = false
            }
            singleLocationCont?.cancel(); singleLocationCont = null
            removeObservers()
            trackingActive = false
            updateBackgroundIndicator()
        }
    }

    private fun removeObservers() {
        val center = NSNotificationCenter.defaultCenter
        didBecomeActiveObs?.let { center.removeObserver(it); didBecomeActiveObs = null }
        didEnterBgObs?.let { center.removeObserver(it); didEnterBgObs = null }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>
    ) {
        val loc = (didUpdateLocations.lastOrNull() as? CLLocation) ?: return
        val (lat, lon) = loc.coordinate.useContents { latitude to longitude }
        val speedMps = if (loc.speed >= 0) loc.speed.toFloat() else 0f

        // Apply simulation altitude logic if running in simulator
        val altitudeM = if (isSimulator) {
            val simulatedAltitude = baseSimulationAltitude + (simulationJump * 10)
            simulationJump++
            log.d { "Simulator detected: Using simulated altitude=${simulatedAltitude}m (jump=${simulationJump-1})" }
            simulatedAltitude
        } else {
            if (loc.verticalAccuracy >= 0) loc.altitude else 0.0
        }

        val tsMs = (loc.timestamp.timeIntervalSince1970 * 1000).toLong()
        val hAcc = loc.horizontalAccuracy
        val ageMs = nowMs() - tsMs
        if (ageMs > 15_000) log.w { "Stale fix age=${ageMs}ms hAcc=$hAcc" }
        log.i { "Location update lat=$lat lon=$lon hAcc=$hAcc speed=$speedMps altitude=$altitudeM ts=$tsMs src=${if (singleLocationCont!=null) "single" else "stream"}${if (isSimulator) " [SIM]" else ""}" }
        val locationData = LocationData(lat, lon, altitudeM, speedMps, tsMs)
        singleLocationCont?.let { cont -> cont.resume(locationData); singleLocationCont = null }
        retryCount = 0
        locationScope.launch { _locationFlow.emit(locationData) }
        // Proactive stale refresh scheduling
        scheduleStaleRefresh()
    }

    private var staleRefreshJob: Job? = null
    private fun scheduleStaleRefresh() {
        staleRefreshJob?.cancel()
        if (!isUpdating) return
        staleRefreshJob = locationScope.launch {
            delay(staleMs)
            val last = _locationFlow.replayCache.lastOrNull() ?: return@launch
            val age = nowMs() - last.ts
            if (age >= staleMs && isUpdating) {
                log.d { "Proactive requestLocation() due to potential staleness age=${age}ms" }
                runOnMain { manager.requestLocation() }
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(
        manager: CLLocationManager,
        didFailWithError: NSError
    ) {
        log.e { "Location error domain=${didFailWithError.domain} code=${didFailWithError.code} desc=${didFailWithError.localizedDescription}" }
        if (didFailWithError.domain == kCLErrorDomain && didFailWithError.code.toInt() == 0) {
            if (singleLocationCont?.isActive == true) {
                retryCount += 1
                locationScope.launch {
                    delay(backoffMs)
                    if (backoffMs < maxBackoffMs) backoffMs = (backoffMs * 2).coerceAtMost(maxBackoffMs)
                    val seq = ++requestSeq
                    log.d { "Retry single requestLocation seq=$seq attempt=$retryCount" }
                    runOnMain { manager.requestLocation() }
                }
            } else {
                log.d { "Ignoring transient error during continuous updates" }
            }
            return
        }
        manager.location?.let { cached ->
            val (lat, lon) = cached.coordinate.useContents { latitude to longitude }
            val speedMps = if (cached.speed >= 0) cached.speed.toFloat() else 0f

            // Apply simulation altitude logic for cached location too
            val altitudeM = if (isSimulator) {
                val simulatedAltitude = baseSimulationAltitude + (simulationJump * 10)
                simulationJump++
                log.d { "Simulator detected (cached): Using simulated altitude=${simulatedAltitude}m (jump=${simulationJump-1})" }
                simulatedAltitude
            } else {
                if (cached.verticalAccuracy >= 0) cached.altitude else 0.0
            }

            val tsMs = (cached.timestamp.timeIntervalSince1970 * 1000).toLong()
            log.i { "Using cached fallback lat=$lat lon=$lon altitude=$altitudeM${if (isSimulator) " [SIM]" else ""}" }
            singleLocationCont?.resume(LocationData(lat, lon, altitudeM, speedMps, tsMs))
            singleLocationCont = null
            return
        }
        singleLocationCont?.resumeWithException(IllegalStateException("No location available"))
        singleLocationCont = null
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        log.i { "Authorization changed ${statusString(manager.authorizationStatus)} accuracy=${accuracyStatusString(manager)}" }
        if (!initialStatusLogged) {
            initialStatusLogged = true
            locationScope.launch {
                val services = try { CLLocationManager.locationServicesEnabled() } catch (_: Throwable) { null }
                log.i { "(Initial) servicesEnabled=$services" }
            }
        }
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> {
                if (singleLocationCont?.isActive == true) runOnMain { manager.requestLocation() }
                if (trackingActive && !isUpdating) startStandardUpdates()
                adjustAccuracy(foreground = !isInBackground)
                updateBackgroundIndicator()
            }
            kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> {
                singleLocationCont?.resumeWithException(IllegalStateException("Location permission not granted"))
                singleLocationCont = null
            }
            else -> Unit
        }
    }

    override fun locationManagerDidPauseLocationUpdates(manager: CLLocationManager) {
        log.w { "Updates paused by system" }
        if (isUpdating) runOnMain { manager.requestLocation() }
    }

    override fun locationManagerDidResumeLocationUpdates(manager: CLLocationManager) {
        log.w { "Updates resumed by system" }
        runOnMain { manager.requestLocation() }
    }

    private fun runOnMain(block: () -> Unit) = dispatch_async(dispatch_get_main_queue(), block)

    private fun statusString(status: CLAuthorizationStatus): String = when (status) {
        kCLAuthorizationStatusNotDetermined -> "NotDetermined"
        kCLAuthorizationStatusRestricted -> "Restricted"
        kCLAuthorizationStatusDenied -> "Denied"
        kCLAuthorizationStatusAuthorizedAlways -> "AuthorizedAlways"
        kCLAuthorizationStatusAuthorizedWhenInUse -> "AuthorizedWhenInUse"
        else -> "Unknown($status)"
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun accuracyStatusString(manager: CLLocationManager): String = try {
        if (manager.respondsToSelector(NSSelectorFromString("accuracyAuthorization"))) {
            when (manager.accuracyAuthorization) {
                CLAccuracyAuthorization.CLAccuracyAuthorizationFullAccuracy -> "Full"
                CLAccuracyAuthorization.CLAccuracyAuthorizationReducedAccuracy -> "Reduced"
                else -> "Other"
            }
        } else "N/A"
    } catch (_: Throwable) { "N/A" }

    @Suppress("unused")
    private fun requestFullAccuracyIfPossible(manager: CLLocationManager) {
        val purposeKey = "PrecisePurpose"
        log.d { "Requesting temporary full accuracy purposeKey=$purposeKey" }
        manager.requestTemporaryFullAccuracyAuthorizationWithPurposeKey(purposeKey)
    }
}
