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
import platform.Foundation.NSProcessInfo

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class AccelerometerProvider {

    private val motionManager = CMMotionManager()
    private val log = Logger.withTag("AccelerometerProvider")

    private fun isSimulator(): Boolean =
        NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null
    actual suspend fun getAcceleration(): AccelerationData {
        if (isSimulator()) {

            return AccelerationData(
                x = 0.0f,
                y = 0.0f,
                z = 9.8f,
                ts = (NSDate().timeIntervalSince1970 * 1000).toLong()
            )
        }

        return suspendCancellableCoroutine { cont ->
            if (!motionManager.isAccelerometerAvailable()) {
                cont.resumeWithException(
                    IllegalStateException("Accelerometer not available on this device")
                )
                return@suspendCancellableCoroutine
            }

            motionManager.accelerometerUpdateInterval = 0.02  // 50 Hz

            motionManager.startAccelerometerUpdatesToQueue(
                NSOperationQueue()
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
                        val (x, y, z) = data.acceleration.useContents {
                            Triple(x.toFloat(), y.toFloat(), z.toFloat())
                        }
                        cont.resume(
                            AccelerationData(
                                x = x,
                                y = y,
                                z = z,
                                ts = (NSDate().timeIntervalSince1970 * 1000).toLong()
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
}
