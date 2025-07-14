package com.topout.kmp.utils.providers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.topout.kmp.models.sensor.AccelerationData
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Reads a single accelerometer sample and returns it as an [AccelerationData] record.
 *
 * * Axis directions follow the Android sensor coordinate system — X points right,
 *   Y points up, Z points out of the screen.:contentReference[oaicite:0]{index=0}
 * * The function suspends until **one** sensor event arrives, then unregisters the listener;
 *   cancellation immediately unregisters as well.:contentReference[oaicite:1]{index=1}
 *
 * @throws IllegalStateException when the device has no accelerometer.
 */
actual class AccelerometerProvider(private val context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: throw IllegalStateException("Device has no accelerometer")   // most phones do

    /** One-shot read — ideal for KMP coroutines. */
    actual suspend fun getAcceleration(): AccelerationData =
        suspendCancellableCoroutine { cont ->

            val listener = object : SensorEventListener {

                override fun onSensorChanged(event: SensorEvent) {
                    sensorManager.unregisterListener(this)

                    // event.values = [x, y, z, …] expressed in m/s².:contentReference[oaicite:2]{index=2}
                    val v = event.values
                    cont.resume(
                        AccelerationData(
                            x  = v[0],
                            y  = v[1],
                            z  = v[2],
                            ts = System.currentTimeMillis()
                        )
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sensorManager.registerListener(
                listener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME       // ≈ 50 Hz (~20 ms).:contentReference[oaicite:3]{index=3}
            )

            cont.invokeOnCancellation {                // tidy-up if the coroutine is cancelled
                sensorManager.unregisterListener(listener)
                cont.resumeWithException(
                    CancellationException("getAcceleration() was cancelled")
                )
            }
        }
}
