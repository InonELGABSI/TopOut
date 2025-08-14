package com.topout.kmp.utils.providers

import com.topout.kmp.models.sensor.LocationData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import platform.CoreLocation.*
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Koin-safe wrapper for location functionality that delegates to CoreLocationDelegate
 */
actual class LocationProvider {
    private val delegate = CoreLocationDelegate()

    actual suspend fun getLocation(): LocationData {
        return delegate.getSingleLocation()
    }

    fun locationFlow(): Flow<LocationData> {
        return delegate.locationFlow()
    }

    fun startUpdatingLocation() {
        delegate.startUpdatingLocation()
    }

    fun stopUpdatingLocation() {
        delegate.stopUpdatingLocation()
    }
}

/**
 * Internal Core Location delegate that extends NSObject.
 * This class is NOT registered with Koin to avoid KClass issues.
 */
private class CoreLocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {

    private val manager = CLLocationManager().apply {
        desiredAccuracy = kCLLocationAccuracyBest
        distanceFilter = 5.0 // Update every 5 meters to conserve battery
        allowsBackgroundLocationUpdates = true
        pausesLocationUpdatesAutomatically = false
    }

    private val log = Logger.withTag("LocationProvider")
    private var singleLocationCont: kotlinx.coroutines.CancellableContinuation<LocationData>? = null

    private val _locationFlow = MutableSharedFlow<LocationData>(replay = 1)

    init {
        manager.delegate = this
    }

    fun locationFlow(): Flow<LocationData> = _locationFlow

    @OptIn(ExperimentalForeignApi::class)
    suspend fun getSingleLocation(): LocationData {
        // Check if location services are enabled
        if (!CLLocationManager.locationServicesEnabled()) {
            throw IllegalStateException("Location services disabled")
        }

        // Get location with a 5-second timeout (iOS best practice to prevent hangs)
        return withTimeout(5_000) {
            suspendCancellableCoroutine { continuation ->
                singleLocationCont = continuation

                // Set up delegate to receive location updates
                manager.delegate = this@CoreLocationDelegate

                // Check authorization status and request permissions if needed
                when (CLLocationManager.authorizationStatus()) {
                    kCLAuthorizationStatusNotDetermined -> {
                        manager.requestWhenInUseAuthorization()
                    }
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> {
                        continuation.resumeWithException(
                            IllegalStateException("Location permission not granted")
                        )
                    }
                    else -> {
                        // Already authorized
                        manager.requestLocation()
                    }
                }

                // Clean up resources when coroutine is cancelled
                continuation.invokeOnCancellation {
                    log.d { "Location request cancelled" }
                    singleLocationCont = null
                }
            }
        }
    }

    fun startUpdatingLocation() {
        log.i { "Starting continuous location updates" }
        when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusNotDetermined -> manager.requestAlwaysAuthorization()
            kCLAuthorizationStatusAuthorizedWhenInUse -> manager.requestAlwaysAuthorization()
            kCLAuthorizationStatusAuthorizedAlways -> manager.startUpdatingLocation()
            else -> log.e { "Location permission not granted for background updates." }
        }
    }

    fun stopUpdatingLocation() {
        log.i { "Stopping continuous location updates" }
        manager.stopUpdatingLocation()
    }

    /* ────── CLLocationManagerDelegate Methods ────── */

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>
    ) {
        // Get the most recent location (best practice for iOS)
        val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return

        // Extract location data (using Kotlin/Native's safe C-struct interop)
        val (lat, lon) = location.coordinate.useContents {
            latitude to longitude
        }

        val locationData = LocationData(
            lat = lat,
            lon = lon,
            // iOS best practice: only use altitude if it's valid (verticalAccuracy >= 0)
            altitude = if (location.verticalAccuracy >= 0) location.altitude else 0.0,
            // iOS best practice: only use speed if it's valid (horizontalAccuracy >= 0)
            speed = if (location.horizontalAccuracy >= 0) location.speed.toFloat() else 0f,
            ts = (NSDate().timeIntervalSince1970 * 1000).toLong()
        )

        // Complete the coroutine and clean up
        singleLocationCont?.resume(locationData)
        singleLocationCont = null

        // Emit to flow for continuous listeners
        _locationFlow.tryEmit(locationData)
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(
        manager: CLLocationManager,
        didFailWithError: NSError
    ) {
        log.e { "Location error: ${didFailWithError.localizedDescription}" }

        // iOS best practice: try to use cached location as fallback
        manager.location?.let { cachedLocation ->
            val (lat, lon) = cachedLocation.coordinate.useContents {
                latitude to longitude
            }

            val locationData = LocationData(
                lat = lat,
                lon = lon,
                altitude = if (cachedLocation.verticalAccuracy >= 0) cachedLocation.altitude else 0.0,
                speed = if (cachedLocation.horizontalAccuracy >= 0) cachedLocation.speed.toFloat() else 0f,
                ts = (NSDate().timeIntervalSince1970 * 1000).toLong()
            )

            log.i { "Using cached location as fallback" }
            singleLocationCont?.resume(locationData)
            singleLocationCont = null
            return
        }


        // No fallback available
        singleLocationCont?.resumeWithException(IllegalStateException("No location available"))
        singleLocationCont = null
    }

    // iOS 14+ authorization callback
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> {
                if (singleLocationCont?.isActive == true) {
                    manager.requestLocation()
                }
                // For continuous updates, we might need to start them if authorized now.
                if (CLLocationManager.authorizationStatus() == kCLAuthorizationStatusAuthorizedAlways) {
                    manager.startUpdatingLocation()
                }
            }
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> {
                singleLocationCont?.resumeWithException(IllegalStateException("Location permission not granted"))
                singleLocationCont = null
            }
            else -> { /* Not determined yet */ }
        }
    }
}
