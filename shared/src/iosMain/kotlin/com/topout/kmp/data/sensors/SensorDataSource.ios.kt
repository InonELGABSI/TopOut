// iosMain/com/topout/kmp/data/sensors/SensorDataSource.kt
package com.topout.kmp.data.sensors

import com.topout.kmp.models.sensor.*
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.LocationProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import co.touchlab.kermit.Logger

/** iOS-specific data-source that mirrors Android behavior with MutableSharedFlow and proper lifecycle. */
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

    actual fun start(scope: CoroutineScope) {
        log.i { "start()" }
        this.scope = scope

        // Start accelerometer (≈50 Hz, same as before)
        scope.launch {
            while (isActive) {
                _accelFlow.emit(accelProvider.getAcceleration())
                delay(20)
            }
        }

        // Start barometer (10 Hz, same as before)
        scope.launch {
            while (isActive) {
                _baroFlow.emit(baroProvider.getBarometerReading())
                delay(100)
            }
        }

        // Start location (1 Hz, same as before)
        scope.launch {
            while (isActive) {
                _locFlow.emit(locProvider.getLocation())
                delay(1_000)
            }
        }
    }

    actual fun stop() {
        log.i { "stop()" }
        scope?.cancel()
        scope = null
    }
}
