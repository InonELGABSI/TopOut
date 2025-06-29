package com.topout.kmp.utils.providers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class AccelerometerProvider(private val context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: throw IllegalStateException("No accelerometer on device")

    actual suspend fun getAccelerometerData(): List<Float> =
        suspendCancellableCoroutine { cont ->

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    sensorManager.unregisterListener(this)
                    cont.resume(event.values.take(3).map { it })
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sensorManager.registerListener(
                listener, accelerometer, SensorManager.SENSOR_DELAY_GAME
            )

            cont.invokeOnCancellation { sensorManager.unregisterListener(listener) }
        }
}
