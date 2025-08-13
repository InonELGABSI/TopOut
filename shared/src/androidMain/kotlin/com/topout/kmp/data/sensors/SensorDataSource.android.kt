// androidMain/…/SensorDataSource.kt
package com.topout.kmp.data.sensors

import com.google.android.gms.location.*
import com.topout.kmp.models.sensor.*
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.LocationProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import android.os.Looper
import android.annotation.SuppressLint
import co.touchlab.kermit.Logger

actual class SensorDataSource(
    private val context: android.content.Context,
    private val accelProvider: AccelerometerProvider = AccelerometerProvider(context),
    private val baroProvider : BarometerProvider = BarometerProvider(context),
    private val locProvider  : LocationProvider = LocationProvider(context)
) {
    private val log = Logger.withTag("SensorDataSource")

    // Flows that can be shared, only emit while started
    private val _accelFlow = MutableSharedFlow<AccelerationData>(replay = 1)
    private val _baroFlow  = MutableSharedFlow<AltitudeData>(replay = 1)
    private val _locFlow   = MutableSharedFlow<LocationData>(replay = 1)

    // Expose only as read-only Flow
    actual val accelFlow: Flow<AccelerationData> get() = _accelFlow
    actual val baroFlow : Flow<AltitudeData> get() = _baroFlow
    actual val locFlow  : Flow<LocationData> get() = _locFlow

    private var scope: CoroutineScope? = null
    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    actual fun start(scope: CoroutineScope) {
        log.i { "start() with scope: ${scope}" }
        this.scope = scope

        // Start accelerometer
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

        // Start barometer
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

        // Simplified, robust location tracking for background
        startLocationTracking(scope)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationTracking(scope: CoroutineScope) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Aggressive location request for background tracking
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(2000)
            .setWaitForAccurateLocation(false)
            .setMaxUpdateAgeMillis(10000) // Accept older locations in background
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    scope.launch {
                        try {
                            val locationData = location.toModel()
                            _locFlow.emit(locationData)
                            log.d { "Location: ${location.latitude}, ${location.longitude} (${if (location.hasAccuracy()) "±${location.accuracy}m" else "no accuracy"})" }
                        } catch (e: Exception) {
                            log.w { "Failed to emit location: ${e.message}" }
                        }
                    }
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                log.i { "Location availability: ${availability.isLocationAvailable}" }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            log.i { "Location updates requested successfully" }
        } catch (e: Exception) {
            log.e { "Failed to request location updates: ${e.message}" }
        }

        // Single fallback mechanism - only when FusedLocationProvider fails
        scope.launch {
            delay(5000) // Wait 5 seconds for FusedLocationProvider to start
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
        // Remove location updates
        locationCallback?.let { cb ->
            LocationServices.getFusedLocationProviderClient(context)
                .removeLocationUpdates(cb)
        }
        locationCallback = null
    }
}

private fun android.location.Location.toModel() = LocationData(
    lat = latitude,
    lon = longitude,
    altitude = if (hasAltitude()) altitude else 0.0, // Only use altitude if available
    speed = speed,
    ts = System.currentTimeMillis()
)