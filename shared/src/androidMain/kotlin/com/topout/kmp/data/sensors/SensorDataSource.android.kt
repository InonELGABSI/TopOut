// androidMain/…/SensorDataSource.kt
package com.topout.kmp.data.sensors

import android.hardware.*
import android.os.Handler
import android.os.HandlerThread
import com.google.android.gms.location.*
import com.topout.kmp.models.sensor.*
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.LocationProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.channels.awaitClose   //  ← add this

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

    actual val locFlow  : Flow<LocationData>     = sensorLoop(100) { // 10 Hz
        locProvider.getLocation()
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
