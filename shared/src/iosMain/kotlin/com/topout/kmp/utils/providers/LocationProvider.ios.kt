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

/**
 * Koin-safe wrapper for location functionality that delegates to CoreLocationDelegate
 */
actual class LocationProvider {
    private val delegate = CoreLocationDelegate()

    actual suspend fun getLocation(): LocationData {
        return delegate.getLocation()
    }
}

/**
 * Internal Core Location delegate that extends NSObject.
 * This class is NOT registered with Koin to avoid KClass issues.
 */
private class CoreLocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {

    private val manager = CLLocationManager().apply {
        desiredAccuracy = kCLLocationAccuracyBest      // ≤10 m when hardware allows (matches Android PRIORITY_HIGH_ACCURACY)
        distanceFilter  = kCLDistanceFilterNone
    }

    private val log = Logger.withTag("LocationProvider")
    private var cont: kotlinx.coroutines.CancellableContinuation<LocationData>? = null
    private var hasRequestedFreshLocation = false

    @OptIn(ExperimentalForeignApi::class)
    suspend fun getLocation(): LocationData {
        // Check if location services are enabled
        if (!CLLocationManager.locationServicesEnabled()) {
            throw IllegalStateException("Location services disabled")
        }

        // Get location with a 5-second timeout (iOS best practice to prevent hangs)
        return withTimeout(5_000) {
            suspendCancellableCoroutine { continuation ->
                cont = continuation
                hasRequestedFreshLocation = false

                // Set up delegate to receive location updates
                manager.delegate = this@CoreLocationDelegate

                // Check authorization status and request permissions if needed
                when (CLLocationManager.authorizationStatus()) {
                    kCLAuthorizationStatusNotDetermined -> {
                        manager.requestWhenInUseAuthorization()
                        // Will call requestLocation() in locationManagerDidChangeAuthorization
                    }
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> {
                        continuation.resumeWithException(
                            IllegalStateException("Location permission not granted")
                        )
                    }
                    else -> {
                        // Already authorized
                        requestFreshLocation()
                    }
                }

                // Clean up resources when coroutine is cancelled
                continuation.invokeOnCancellation {
                    log.d { "Location request cancelled" }
                    manager.stopUpdatingLocation()
                    manager.delegate = null
                    cont = null
                }
            }
        }
    }

    private fun requestFreshLocation() {
        hasRequestedFreshLocation = true
        // iOS best practice: use requestLocation() for one-time location requests
        // instead of startUpdatingLocation() + stopUpdatingLocation()
        manager.requestLocation()
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
        cont?.resume(locationData)
        cleanup()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(
        manager: CLLocationManager,
        didFailWithError: NSError
    ) {
        log.e { "Location error: ${didFailWithError.localizedDescription}" }

        // iOS best practice: try to use cached location as fallback
        if (hasRequestedFreshLocation) {
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
                cont?.resume(locationData)
                cleanup()
                return
            }
        }

        // No fallback available
        cont?.resumeWithException(IllegalStateException("No location available"))
        cleanup()
    }

    // iOS 14+ authorization callback
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        if (cont == null) return

        when (manager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> {
                requestFreshLocation()
            }
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> {
                cont?.resumeWithException(IllegalStateException("Location permission not granted"))
                cleanup()
            }
            else -> { /* Not determined yet */ }
        }
    }

    // iOS best practice: clean up resources in one place
    private fun cleanup() {
        manager.delegate = null
        cont = null
    }
}
