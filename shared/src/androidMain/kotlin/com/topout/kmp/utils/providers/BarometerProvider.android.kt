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

/**
 * One-shot barometer read that returns both the raw pressure (hPa) and the
 * derived altitude (m) in an AltitudeData record.
 *
 * @throws IllegalStateException if the device has no barometer.
 */
actual class BarometerProvider(
    private val context: Context
) {

    private val sm: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val log = Logger.withTag("BarometerProvider")

    private val baro: Sensor = sm.getDefaultSensor(Sensor.TYPE_PRESSURE)
            ?: throw IllegalStateException("Device has no barometer")

    /** Suspend until we receive one sensor sample, then unregister. */
    actual suspend fun getBarometerReading(): AltitudeData =
        suspendCancellableCoroutine { cont ->
            //log.d { "getBarometerReading()" }

            val listener = object : SensorEventListener {

                override fun onSensorChanged(event: SensorEvent) {
                    sm.unregisterListener(this)
                    //log.d { "onSensorChanged" }

                    val pressure = event.values[0]                    // hPa ㊀
                    val altitude = SensorManager.getAltitude(
                        SensorManager.PRESSURE_STANDARD_ATMOSPHERE,   // 1013.25 hPa ㊂
                        pressure
                    ).toDouble()                                      // metres ㊁

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
