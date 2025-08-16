package com.topout.kmp.utils.providers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.topout.kmp.models.sensor.AltitudeData
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import co.touchlab.kermit.Logger

actual class BarometerProvider(
    private val context: Context
) {

    private val sm: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val log = Logger.withTag("BarometerProvider")

    private val baro: Sensor = sm.getDefaultSensor(Sensor.TYPE_PRESSURE)
            ?: throw IllegalStateException("Device has no barometer")

    actual suspend fun getBarometerReading(): AltitudeData =
        suspendCancellableCoroutine { cont ->
            //log.d { "getBarometerReading()" }

            val listener = object : SensorEventListener {

                override fun onSensorChanged(event: SensorEvent) {
                    sm.unregisterListener(this)
                    //log.d { "onSensorChanged" }

                    val pressure = event.values[0]
                    val altitude = SensorManager.getAltitude(
                        SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                        pressure
                    ).toDouble()

                    cont.resume(
                        AltitudeData(
                            altitude = altitude,
                            pressure = pressure,
                            ts       = System.currentTimeMillis()
                        )
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sm.registerListener(listener, baro, SensorManager.SENSOR_DELAY_UI)

            cont.invokeOnCancellation {
                log.d { "invokeOnCancellation: unregistering listener" }
                sm.unregisterListener(listener)
            }
        }
}
