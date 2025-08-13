// iosMain/com/topout/kmp/data/sensors/SensorDataSource.kt
package com.topout.kmp.data.sensors

import com.topout.kmp.models.sensor.*
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.LocationProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import co.touchlab.kermit.Logger
import platform.CoreLocation.*
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents

/** iOS-specific data-source with background location tracking similar to Android. */
actual class SensorDataSource(
    private val accelProvider: AccelerometerProvider = AccelerometerProvider(),
    private val baroProvider : BarometerProvider     = BarometerProvider(),
    private val locProvider  : LocationProvider      = LocationProvider()
) {
    private val log = Logger.withTag("SensorDataSource")

    // Flows that can be shared, only emit while started (same as Android)
    private val _accelFlow = MutableSharedFlow<AccelerationData>(replay = 1)
    private val _baroFlow  = MutableSharedFlow<AltitudeData>(replay = 1)
    private val _locFlow   = MutableSharedFlow<LocationData>(replay = 1)

    // Expose only as read-only Flow (same as Android)
    actual val accelFlow: Flow<AccelerationData> get() = _accelFlow
    actual val baroFlow : Flow<AltitudeData> get() = _baroFlow
    actual val locFlow  : Flow<LocationData> get() = _locFlow

    private var scope: CoroutineScope? = null
    private var locationDelegate: BackgroundLocationDelegate? = null

    actual fun start(scope: CoroutineScope) {
        log.i { "start() with scope: ${scope}" }
        this.scope = scope

        // Start accelerometer (≈50 Hz, same as before)
        scope.launch {
            while (isActive) {
                try {
                    _accelFlow.emit(accelProvider.getAcceleration())
                } catch (e: Exception) {
                    log.w { "Accelerometer error: ${e.message}" }
                }
                delay(20)
            }
        }

        // Start barometer (10 Hz, same as before)
        scope.launch {
            while (isActive) {
                try {
                    _baroFlow.emit(baroProvider.getBarometerReading())
                } catch (e: Exception) {
                    log.w { "Barometer error: ${e.message}" }
                }
                delay(100)
            }
        }

        // Start background location tracking
        startLocationTracking(scope)
    }

    private fun startLocationTracking(scope: CoroutineScope) {
        locationDelegate = BackgroundLocationDelegate { locationData ->
            scope.launch {
                try {
                    _locFlow.emit(locationData)
                    log.d { "Background location: ${locationData.lat}, ${locationData.lon}" }
                } catch (e: Exception) {
                    log.w { "Failed to emit location: ${e.message}" }
                }
            }
        }

        locationDelegate?.startBackgroundLocationUpdates()

        // Fallback mechanism similar to Android
        scope.launch {
            delay(5000) // Wait 5 seconds for CoreLocation to start
            while (isActive) {
                try {
                    // Only use fallback if we haven't received location in a while
                    val freshLocation = locProvider.getLocation()
                    _locFlow.emit(freshLocation)
                    log.d { "Fallback location update: ${freshLocation.lat}, ${freshLocation.lon}" }
                } catch (e: Exception) {
                    log.w { "Fallback location failed: ${e.message}" }
                }
                delay(5000) // Less frequent fallback
            }
        }
    }

    actual fun stop() {
        log.i { "stop()" }
        scope?.cancel()
        scope = null
        locationDelegate?.stopLocationUpdates()
        locationDelegate = null
    }
}

/**
 * Background location delegate for continuous location updates
 */
private class BackgroundLocationDelegate(
    private val onLocationUpdate: (LocationData) -> Unit
) : NSObject(), CLLocationManagerDelegateProtocol {

    private val log = Logger.withTag("BackgroundLocationDelegate")
    private val manager = CLLocationManager().apply {
        desiredAccuracy = kCLLocationAccuracyBest
        distanceFilter = 5.0 // Update every 5 meters to conserve battery
        // Enable background location updates
        allowsBackgroundLocationUpdates = true
        pausesLocationUpdatesAutomatically = false
    }

    init {
        manager.delegate = this
    }

    fun startBackgroundLocationUpdates() {
        log.i { "Starting background location updates" }

        when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusNotDetermined -> {
                manager.requestAlwaysAuthorization()
            }
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> {
                log.e { "Location permission denied" }
            }
            kCLAuthorizationStatusAuthorizedWhenInUse -> {
                // Request always authorization for background
                manager.requestAlwaysAuthorization()
            }
            kCLAuthorizationStatusAuthorizedAlways -> {
                manager.startUpdatingLocation()
            }
        }
    }

    fun stopLocationUpdates() {
        log.i { "Stopping location updates" }
        manager.stopUpdatingLocation()
        manager.delegate = null
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>
    ) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return

        val (lat, lon) = location.coordinate.useContents {
            latitude to longitude
        }

        val locationData = LocationData(
            lat = lat,
            lon = lon,
            altitude = if (location.verticalAccuracy >= 0) location.altitude else 0.0,
            speed = if (location.horizontalAccuracy >= 0) location.speed.toFloat() else 0f,
            ts = (NSDate().timeIntervalSince1970 * 1000).toLong()
        )

        onLocationUpdate(locationData)
    }

    override fun locationManager(
        manager: CLLocationManager,
        didFailWithError: NSError
    ) {
        log.e { "Background location error: ${didFailWithError.localizedDescription}" }
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        log.i { "Authorization changed: ${CLLocationManager.authorizationStatus()}" }

        when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> {
                manager.startUpdatingLocation()
            }
            else -> {
                log.w { "Location authorization not granted for background updates" }
            }
        }
    }
}
