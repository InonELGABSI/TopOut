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
import android.os.Build
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.absoluteValue

actual class SensorDataSource(
    private val context: android.content.Context,
    private val accelProvider: AccelerometerProvider = AccelerometerProvider(context),
    private val baroProvider : BarometerProvider = BarometerProvider(context),
    private val locProvider  : LocationProvider = LocationProvider(context)
) {
    private val log = Logger.withTag("SensorDataSource")

    private val _accelFlow = MutableSharedFlow<AccelerationData>(replay = 1)
    private val _baroFlow  = MutableSharedFlow<AltitudeData>(replay = 1)
    private val _locFlow   = MutableSharedFlow<LocationData>(replay = 1)

    actual val accelFlow: Flow<AccelerationData> get() = _accelFlow
    actual val baroFlow : Flow<AltitudeData> get() = _baroFlow
    actual val locFlow  : Flow<LocationData> get() = _locFlow

    private var scope: CoroutineScope? = null
    private var locationCallback: LocationCallback? = null

    // Emulator detection & altitude simulation (to match LocationProvider and iOS behavior)
    private val isEmulator: Boolean by lazy {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val product = Build.PRODUCT.lowercase()
        fingerprint.contains("generic") || fingerprint.contains("emulator") ||
            model.contains("sdk") || model.contains("emulator") ||
            product.contains("sdk") || product.contains("emulator") || product.contains("generic")
    }
    private val simulationJump = AtomicInteger(0)
    private val baseSimulationAltitude = 100.0
    private var lastAltitude: Double? = null
    private var lastSimStepTs: Long = 0L

    private fun computeAltitude(src: android.location.Location): Double {
        val now = System.currentTimeMillis()
        return if (isEmulator) {
            // Always simulate on emulator to avoid unreliable 0.0 altitude from fused provider
            if (now - lastSimStepTs >= 1000) { // advance roughly once per second
                simulationJump.incrementAndGet()
                lastSimStepTs = now
            }
            val simulated = baseSimulationAltitude + (simulationJump.get() * 10)
            lastAltitude = simulated
            simulated
        } else if (src.hasAltitude()) {
            val alt = src.altitude
            // Treat extremely small or implausible altitudes as noise
            val cleaned = if (alt.absoluteValue < 0.5) lastAltitude ?: alt else alt
            lastAltitude = cleaned
            cleaned
        } else {
            lastAltitude ?: 0.0
        }
    }

    @SuppressLint("MissingPermission")
    actual fun start(scope: CoroutineScope) {
        log.i { "start() with scope: ${scope}" }
        this.scope = scope

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

        startLocationTracking(scope)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationTracking(scope: CoroutineScope) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(2000)
            .setWaitForAccurateLocation(false)
            .setMaxUpdateAgeMillis(10000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    scope.launch {
                        try {
                            val altitude = computeAltitude(location)
                            val locationData = LocationData(
                                lat = location.latitude,
                                lon = location.longitude,
                                altitude = altitude,
                                speed = location.speed,
                                ts = System.currentTimeMillis()
                            )
                            _locFlow.emit(locationData)
                            log.d { "Location: ${'$'}{location.latitude}, ${'$'}{location.longitude} alt=${'$'}altitude (src=stream ${if (isEmulator) "SIM" else "REAL"})" }
                        } catch (e: Exception) {
                            log.w { "Failed to emit location: ${'$'}{e.message}" }
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

        scope.launch {
            delay(2000)
            while (isActive) {
                try {
                    val freshLocation = locProvider.getLocation()
                    // Ensure altitude continuity & simulation for fallback path
                    val adjusted = freshLocation.copy(altitude = freshLocation.altitude.takeIf { it != 0.0 } ?: (lastAltitude ?: freshLocation.altitude))
                    if (adjusted.altitude != freshLocation.altitude) {
                        log.d { "Adjusted fallback altitude from ${'$'}{freshLocation.altitude} to ${'$'}{adjusted.altitude}" }
                    }
                    lastAltitude = adjusted.altitude
                    _locFlow.emit(adjusted)
                    log.d { "Fallback location update: ${'$'}{adjusted.lat}, ${'$'}{adjusted.lon} alt=${'$'}{adjusted.altitude}" }
                } catch (e: Exception) {
                    log.w { "Fallback location failed: ${'$'}{e.message}" }
                }
                delay(2000)
            }
        }
    }

    actual fun stop() {
        log.i { "stop()" }
        scope?.cancel()
        scope = null
        locationCallback?.let { cb ->
            LocationServices.getFusedLocationProviderClient(context)
                .removeLocationUpdates(cb)
        }
        locationCallback = null
    }
}
