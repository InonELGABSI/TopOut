// iosMain/com/topout/kmp/data/sensors/SensorDataSource.kt
package com.topout.kmp.data.sensors

import com.topout.kmp.models.sensor.*
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.LocationProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/** iOS-specific data-source that turns the one-shot providers into cold Flows. */
actual class SensorDataSource(
    private val accelProvider: AccelerometerProvider = AccelerometerProvider(),
    private val baroProvider : BarometerProvider     = BarometerProvider(),
    private val locProvider  : LocationProvider      = LocationProvider()
) {

    /* ─────── public sensor streams ─────── */

    actual val accelFlow: Flow<AccelerationData> = sensorLoop( 20) { // ≈50 Hz
        accelProvider.getAcceleration()
    }

    actual val baroFlow : Flow<AltitudeData>     = sensorLoop(100) { // 10 Hz
        baroProvider.getBarometerReading()
    }

    actual val locFlow  : Flow<LocationData>     = sensorLoop(1_000) { // 1 Hz
        locProvider.getLocation()
    }

    /* ─────── lifecycle (mirrors Android) ─────── */

    private var scope: CoroutineScope? = null

    actual fun start(scope: CoroutineScope) { this.scope = scope }

    actual fun stop() { scope?.cancel(); scope = null }

    /* ─────── helper – polling loop with cancellation ─────── */

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
