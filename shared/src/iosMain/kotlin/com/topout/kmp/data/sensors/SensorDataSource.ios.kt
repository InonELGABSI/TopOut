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
    private var locationJob: Job? = null

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
        locationJob = scope.launch {
            locProvider.locationFlow().collect { locationData ->
                _locFlow.emit(locationData)
            }
        }
        locProvider.startUpdatingLocation()
    }

    actual fun stop() {
        log.i { "stop()" }
        locationJob?.cancel()
        locationJob = null
        locProvider.stopUpdatingLocation()
        scope?.cancel()
        scope = null
    }
}
