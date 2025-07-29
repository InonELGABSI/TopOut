package com.topout.kmp.utils.providers

import com.topout.kmp.models.sensor.AccelerationData
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSDate
import platform.Foundation.NSOperationQueue
import platform.Foundation.timeIntervalSince1970
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class AccelerometerProvider {

    private val motionManager = CMMotionManager()
    private val log = Logger.withTag("AccelerometerProvider")

    /**
     * One-shot read; returns X, Y, Z in m/s² plus epoch-millis timestamp.
     *
     * * Axis directions follow the iOS Core Motion coordinate system
     * * The function suspends until **one** sensor event arrives, then stops updates;
     *   cancellation immediately stops updates as well.
     *
     * @throws IllegalStateException when the device has no accelerometer.
     */
    actual suspend fun getAcceleration(): AccelerationData =
        suspendCancellableCoroutine { cont ->
            //log.d { "getAcceleration()" }

            if (!motionManager.isAccelerometerAvailable()) {
                cont.resumeWithException(
                    IllegalStateException("Accelerometer not available on this device")
                )
                return@suspendCancellableCoroutine
            }

            // Match Android's SENSOR_DELAY_GAME (≈ 50 Hz, ~20 ms)
            motionManager.accelerometerUpdateInterval = 0.02  // 50 Hz to match Android

            motionManager.startAccelerometerUpdatesToQueue(
                NSOperationQueue()  // Use background queue instead of main queue for consistency
            ) { data, error ->

                when {
                    error != null -> {
                        motionManager.stopAccelerometerUpdates()
                        cont.resumeWithException(
                            IllegalStateException(error.localizedDescription ?: "CoreMotion accelerometer error")
                        )
                    }

                    data != null -> {
                        motionManager.stopAccelerometerUpdates()
                        //log.d { "onAccelerometerUpdate" }

                        // Safely unwrap CMAcceleration struct (x, y, z)
                        // Keep as Float to match Android SensorEvent.values type
                        val (x, y, z) = data.acceleration.useContents {
                            Triple(x.toFloat(), y.toFloat(), z.toFloat())
                        }

                        cont.resume(
                            AccelerationData(
                                x  = x,
                                y  = y,
                                z  = z,
                                ts = (NSDate().timeIntervalSince1970 * 1000).toLong() // matches System.currentTimeMillis
                            )
                        )
                    }

                    else -> {
                        motionManager.stopAccelerometerUpdates()
                        cont.resumeWithException(
                            IllegalStateException("Received null data from CoreMotion")
                        )
                    }
                }
            }

            cont.invokeOnCancellation {
                log.d { "invokeOnCancellation: stopping accelerometer updates" }
                motionManager.stopAccelerometerUpdates()
                cont.resumeWithException(
                    CancellationException("getAcceleration() was cancelled")
                )
            }
        }
}
