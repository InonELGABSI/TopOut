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
import co.touchlab.kermit.Logger
actual class AccelerometerProvider(private val context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val log = Logger.withTag("AccelerometerProvider")

    private val accelerometer: Sensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: throw IllegalStateException("Device has no accelerometer")

    actual suspend fun getAcceleration(): AccelerationData =
        suspendCancellableCoroutine { cont ->

            val listener = object : SensorEventListener {

                override fun onSensorChanged(event: SensorEvent) {
                    sensorManager.unregisterListener(this)

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
                SensorManager.SENSOR_DELAY_GAME
            )

            cont.invokeOnCancellation {
                log.d { "invokeOnCancellation: unregistering listener" }
                sensorManager.unregisterListener(listener)
                cont.resumeWithException(
                    CancellationException("getAcceleration() was cancelled")
                )
            }
        }
}
