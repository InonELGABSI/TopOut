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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
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

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setMinUpdateIntervalMillis(500)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    trySend(location.toModel()).isSuccess
                }
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
    altitude = altitude,
    speed = speed,
    ts = System.currentTimeMillis()
)

