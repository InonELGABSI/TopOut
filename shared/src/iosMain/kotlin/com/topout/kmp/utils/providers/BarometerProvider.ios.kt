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

/**
 * One-shot barometer read that returns both the raw pressure (hPa) and the
 * derived altitude (m) in an AltitudeData record.
 *
 * Uses CoreMotion's CMAltimeter to get barometric data on iOS devices.
 *
 * @throws IllegalStateException if the device has no barometer or barometer access is unavailable.
 */
@OptIn(ExperimentalForeignApi::class)
actual class BarometerProvider {

    private val altimeter = CMAltimeter()
    private val log = Logger.withTag("BarometerProvider")

    /** One-shot read of relative altitude (m) + raw pressure (hPa). */
    actual suspend fun getBarometerReading(): AltitudeData =
        // Add a reasonable timeout to prevent hanging
        withTimeout(3000) {
            suspendCancellableCoroutine { cont ->
                log.d { "getBarometerReading()" }

                // Hardware/OS availability check (iOS best practice)
                if (!CMAltimeter.isRelativeAltitudeAvailable()) {
                    log.e { "Barometer not available on this device" }
                    cont.resumeWithException(
                        IllegalStateException("Barometer not available on this device")
                    )
                    return@suspendCancellableCoroutine
                }

                // Start barometer stream on background queue (iOS best practice)
                // Use a background queue instead of main queue to avoid blocking UI
                altimeter.startRelativeAltitudeUpdatesToQueue(
                    NSOperationQueue()
                ) { data, error ->
                    when {
                        // Runtime error from Core Motion
                        error != null -> {
                            altimeter.stopRelativeAltitudeUpdates()
                            log.e { "Barometer error: ${error.localizedDescription}" }
                            cont.resumeWithException(
                                IllegalStateException(error.localizedDescription ?: "CoreMotion error")
                            )
                        }

                        // First valid CMAltitudeData sample
                        data != null -> {
                            // Stop stream immediately (iOS best practice)
                            altimeter.stopRelativeAltitudeUpdates()
                            log.d { "Received barometer data" }

                            // CMAltitudeData fields: relativeAltitude (m) & pressure (kPa)
                            // Safely extract values with null handling
                            val altitudeM = data.relativeAltitude?.doubleValue ?: 0.0
                            // Convert kPa to hPa (iOS returns kPa, Android uses hPa)
                            val pressureHpa = (data.pressure?.doubleValue ?: 0.0) * 10.0

                            cont.resume(
                                AltitudeData(
                                    altitude = altitudeM,
                                    pressure = pressureHpa.toFloat(),
                                    // Match Android's System.currentTimeMillis()
                                    ts = (NSDate().timeIntervalSince1970 * 1000).toLong()
                                )
                            )
                        }

                        // No data and no error (shouldn't happen, but handle gracefully)
                        else -> {
                            altimeter.stopRelativeAltitudeUpdates()
                            log.e { "Received null data from barometer" }
                            cont.resumeWithException(
                                IllegalStateException("Received null data from barometer")
                            )
                        }
                    }
                }

                // Ensure the stream is stopped if caller cancels
                cont.invokeOnCancellation {
                    log.d { "Barometer reading cancelled, stopping updates" }
                    altimeter.stopRelativeAltitudeUpdates()
                }
            }
        }
}
