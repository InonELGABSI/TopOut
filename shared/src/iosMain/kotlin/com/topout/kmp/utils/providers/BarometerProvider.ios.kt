package com.topout.kmp.utils.providers

import com.topout.kmp.models.sensor.AltitudeData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import platform.CoreMotion.CMAltimeter
import platform.Foundation.NSDate
import platform.Foundation.NSOperationQueue
import platform.Foundation.timeIntervalSince1970
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import co.touchlab.kermit.Logger
import platform.UIKit.UIDevice
import platform.Foundation.NSProcessInfo

@OptIn(ExperimentalForeignApi::class)
actual class BarometerProvider {

    private val altimeter = CMAltimeter()
    private val log = Logger.withTag("BarometerProvider")

    private fun isSimulator(): Boolean {
        return NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null
    }
    actual suspend fun getBarometerReading(): AltitudeData {
        if (isSimulator()) {
            return AltitudeData(
                altitude = 42.0,
                pressure = 1013.25f,
                ts = (platform.Foundation.NSDate().timeIntervalSince1970 * 1000).toLong()
            )
        }

        return withTimeout(3000) {
            suspendCancellableCoroutine { cont ->
                if (!CMAltimeter.isRelativeAltitudeAvailable()) {
                    cont.resumeWithException(
                        IllegalStateException("Barometer not available on this device")
                    )
                    return@suspendCancellableCoroutine
                }
                altimeter.startRelativeAltitudeUpdatesToQueue(
                    NSOperationQueue()
                ) { data, error ->
                    when {
                        error != null -> {
                            altimeter.stopRelativeAltitudeUpdates()
                            log.e { "Barometer error: ${error.localizedDescription}" }
                            cont.resumeWithException(
                                IllegalStateException(error.localizedDescription ?: "CoreMotion error")
                            )
                        }
                        data != null -> {
                            altimeter.stopRelativeAltitudeUpdates()
                            val altitudeM = data.relativeAltitude?.doubleValue ?: 0.0
                            val pressureHpa = (data.pressure?.doubleValue ?: 0.0) * 10.0
                            cont.resume(
                                AltitudeData(
                                    altitude = altitudeM,
                                    pressure = pressureHpa.toFloat(),
                                    ts = (NSDate().timeIntervalSince1970 * 1000).toLong()
                                )
                            )
                        }
                        else -> {
                            altimeter.stopRelativeAltitudeUpdates()
                            log.e { "Received null data from barometer" }
                            cont.resumeWithException(
                                IllegalStateException("Received null data from barometer")
                            )
                        }
                    }
                }
                cont.invokeOnCancellation {
                    log.d { "Barometer reading cancelled, stopping updates" }
                    altimeter.stopRelativeAltitudeUpdates()
                }
            }
        }
    }
}