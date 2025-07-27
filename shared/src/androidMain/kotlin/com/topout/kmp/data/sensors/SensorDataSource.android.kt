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
        log.i { "start()" }
        this.scope = scope

        // Start accelerometer
        scope.launch {
            while (isActive) {
                _accelFlow.emit(accelProvider.getAcceleration())
                delay(20)
            }
        }
        // Start barometer
        scope.launch {
            while (isActive) {
                _baroFlow.emit(baroProvider.getBarometerReading())
                delay(100)
            }
        }
        // Start location
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(2000)
            .setWaitForAccurateLocation(true)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    val locationData = location.toModel()
                    scope.launch { _locFlow.emit(locationData) }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        // Extra periodic updates if needed:
        scope.launch {
            while (isActive) {
                try {
                    val freshLocation = locProvider.getLocation()
                    _locFlow.emit(freshLocation)
                } catch (_: Exception) { }
                delay(2000)
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

