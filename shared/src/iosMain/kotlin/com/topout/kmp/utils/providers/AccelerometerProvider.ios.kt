package com.topout.kmp.utils.providers

import kotlinx.cinterop.useContents          // <-- NEW
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class AccelerometerProvider {

    private val motionManager = CMMotionManager()

    actual suspend fun getAccelerometerData(): List<Float> =
        suspendCancellableCoroutine { cont ->

            if (!motionManager.isAccelerometerAvailable()) {            // availability test
                cont.resumeWithException(
                    IllegalStateException("Accelerometer not available")
                )
                return@suspendCancellableCoroutine
            }

            motionManager.accelerometerUpdateInterval = 0.016           // ~60 Hz

            motionManager.startAccelerometerUpdatesToQueue(
                NSOperationQueue.mainQueue()
            ) { data, error ->

                when {
                    error != null -> {
                        motionManager.stopAccelerometerUpdates()
                        cont.resumeWithException(
                            IllegalStateException(
                                error.localizedDescription ?: "CoreMotion error"
                            )
                        )
                    }

                    data != null -> {
                        motionManager.stopAccelerometerUpdates()

                        // ✅ unwrap CMAcceleration safely
                        val triple = data.acceleration.useContents {
                            listOf(x.toFloat(), y.toFloat(), z.toFloat())
                        }

                        cont.resume(triple)
                    }
                }
            }

            cont.invokeOnCancellation { motionManager.stopAccelerometerUpdates() }
        }
}
