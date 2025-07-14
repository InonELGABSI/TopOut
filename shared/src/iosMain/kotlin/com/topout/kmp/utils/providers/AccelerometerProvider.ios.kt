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

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class AccelerometerProvider {

    private val motionManager = CMMotionManager()           // Core Motion manager :contentReference[oaicite:0]{index=0}

    /** One-shot read; returns X, Y, Z in m/s² plus epoch-millis timestamp. */
    actual suspend fun getAcceleration(): AccelerationData =
        suspendCancellableCoroutine { cont ->

            if (!motionManager.isAccelerometerAvailable()) {
                cont.resumeWithException(
                    IllegalStateException("Accelerometer not available on this device")
                )
                return@suspendCancellableCoroutine
            }

            motionManager.accelerometerUpdateInterval = 0.016  // ≈ 60 Hz :contentReference[oaicite:1]{index=1}

            motionManager.startAccelerometerUpdatesToQueue(
                NSOperationQueue.mainQueue()                   // queue – fine for one sample :contentReference[oaicite:2]{index=2}
            ) { data, error ->

                when {
                    error != null -> {
                        motionManager.stopAccelerometerUpdates()
                        cont.resumeWithException(
                            IllegalStateException(error.localizedDescription ?: "CoreMotion error")
                        )
                    }

                    data != null -> {
                        motionManager.stopAccelerometerUpdates()

                        // Safely unwrap CMAcceleration struct (x, y, z) :contentReference[oaicite:3]{index=3}
                        val (x, y, z) = data.acceleration.useContents {
                            Triple(x.toFloat(), y.toFloat(), z.toFloat())
                        }

                        cont.resume(
                            AccelerationData(
                                x  = x,
                                y  = y,
                                z  = z,
                                ts = (NSDate().timeIntervalSince1970 * 1000).toLong() // matches System.currentTimeMillis - android implementation
                            )
                        )
                    }
                }
            }

            cont.invokeOnCancellation { motionManager.stopAccelerometerUpdates() }
        }
}
