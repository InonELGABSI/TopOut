package com.topout.kmp.data.sensors

import com.topout.kmp.models.sensor.*
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.LocationProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import co.touchlab.kermit.Logger


actual class SensorDataSource(
    private val accelProvider: AccelerometerProvider,
    private val baroProvider: BarometerProvider,
    private val locProvider: LocationProvider,
    private val appStateMonitor: AppStateMonitor
) {
    private val log = Logger.withTag("SensorDataSource")

    private val _accelFlow = MutableSharedFlow<AccelerationData>(replay = 1)
    private val _baroFlow  = MutableSharedFlow<AltitudeData>(replay = 1)
    private val _locFlow   = MutableSharedFlow<LocationData>(replay = 1)

    actual val accelFlow: Flow<AccelerationData> get() = _accelFlow
    actual val baroFlow : Flow<AltitudeData> get() = _baroFlow
    actual val locFlow  : Flow<LocationData> get() = _locFlow

    private var scope: CoroutineScope? = null
    private var accelJob: Job? = null
    private var baroJob: Job? = null
    private var locationJob: Job? = null

    private fun isForeground(): Boolean = appStateMonitor.isForeground


    actual fun start(scope: CoroutineScope) {
        log.i { "start() with scope: $scope" }
        this.scope = scope

        // Mark active tracking session (enables high-accuracy + background indicator logic)
        locProvider.beginActiveSession()

        // Accelerometer – only when app is foreground
        accelJob = scope.launch {
            while (isActive) {
                if (isForeground()) {
                    try {
                        _accelFlow.emit(accelProvider.getAcceleration())
                    } catch (e: Exception) {
                        log.w { "Accelerometer error: ${e.message}" }
                    }
                    delay(20) // ~50Hz
                } else {
                    delay(500) // slow down in background
                }
            }
        }

        // Barometer – only when app is foreground
        baroJob = scope.launch {
            while (isActive) {
                if (isForeground()) {
                    try {
                        _baroFlow.emit(baroProvider.getBarometerReading())
                    } catch (e: Exception) {
                        log.w { "Barometer error: ${e.message}" }
                    }
                    delay(100) // ~10Hz
                } else {
                    delay(1000) // slow down in background
                }
            }
        }

        // Location – keep running in background to maintain app execution
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
        accelJob?.cancel()
        baroJob?.cancel()
        locationJob?.cancel()

        locProvider.endActiveSession()
        locProvider.stopUpdatingLocation()

        scope?.cancel()
        scope = null
    }
}
