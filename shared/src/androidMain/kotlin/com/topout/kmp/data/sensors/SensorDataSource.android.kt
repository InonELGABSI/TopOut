// androidMain/…/SensorDataSource.kt
package com.topout.kmp.data.sensors

import com.google.android.gms.location.*
import com.topout.kmp.models.sensor.*
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.LocationProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

import kotlinx.coroutines.channels.awaitClose
import android.os.Looper
import android.annotation.SuppressLint

actual class SensorDataSource(
    private val context: android.content.Context,
    private val accelProvider: AccelerometerProvider = AccelerometerProvider(context),
    private val baroProvider : BarometerProvider = BarometerProvider(context),
    private val locProvider  : LocationProvider = LocationProvider(context)
) {
    /* ------ cold Flows that call the one-shot providers in a loop ------ */

    actual val accelFlow: Flow<AccelerationData> = sensorLoop(20) {  // 50 Hz
        accelProvider.getAcceleration()
    }

    actual val baroFlow : Flow<AltitudeData>     = sensorLoop(100) { // 10 Hz
        baroProvider.getBarometerReading()
    }

    @SuppressLint("MissingPermission") // Permissions are checked in the UI layer before starting
    actual val locFlow: Flow<LocationData> = callbackFlow {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        // Enhanced location request for better altitude accuracy
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setMinUpdateIntervalMillis(500)
            setMaxUpdateDelayMillis(2000)
            setWaitForAccurateLocation(true) // Wait for more accurate location
        }.build()

        // Callback for immediate lat/lon updates
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    val locationData = location.toModel()
                    println("📍 GPS Update: lat=${locationData.lat}, lon=${locationData.lon}, alt=${locationData.altitude}, accuracy=${location.accuracy}m, hasAltitude=${location.hasAltitude()}")
                    trySend(locationData)
                }
            }
        }

        // Periodic job to get fresh location (especially for altitude)
        val freshLocationJob = scope.launch {
            while (isActive) {
                try {
                    val freshLocation = locProvider.getLocation()
                    println("🔄 Fresh GPS: lat=${freshLocation.lat}, lon=${freshLocation.lon}, alt=${freshLocation.altitude}")
                    trySend(freshLocation)
                } catch (e: Exception) {
                    println("❌ Error getting fresh location: ${e.message}")
                }
                delay(2000) // Every 2 seconds, get a completely fresh location
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            println("🛑 SensorDataSource stopping location updates")
            fusedLocationClient.removeLocationUpdates(locationCallback)
            freshLocationJob.cancel()
            scope.cancel()
        }
    }

    /* lifecycle */

    private var scope: CoroutineScope? = null
    actual fun start(scope: CoroutineScope) { this.scope = scope }
    actual fun stop() { scope?.cancel(); scope = null }

    /* helper — repeats the suspend getter on the given period */
    private fun <T> sensorLoop(periodMs: Long, block: suspend () -> T) =
        channelFlow {
            val job = launch(Dispatchers.Default) {
                while (isActive) {
                    send(block())
                    delay(periodMs)
                }
            }
            awaitClose { job.cancel() }
        }
}

private fun android.location.Location.toModel() = LocationData(
    lat = latitude,
    lon = longitude,
    altitude = if (hasAltitude()) altitude else 0.0, // Only use altitude if available
    speed = speed,
    ts = System.currentTimeMillis()
)

