package com.topout.kmp.utils.providers

import com.topout.kmp.models.sensor.AltitudeData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreMotion.CMAltimeter
import platform.Foundation.NSDate
import platform.Foundation.NSOperationQueue
import platform.Foundation.timeIntervalSince1970
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
actual class BarometerProvider {

    private val altimeter = CMAltimeter()

    /** One-shot read of relative altitude (m) + raw pressure (hPa). */
    actual suspend fun getBarometerReading(): AltitudeData =
        suspendCancellableCoroutine { cont ->

            // 1️⃣  Hardware/OS availability check
            if (!CMAltimeter.isRelativeAltitudeAvailable()) {          // Apple doc :contentReference[oaicite:0]{index=0}
                cont.resumeWithException(
                    IllegalStateException("Barometer not available on this device")
                )
                return@suspendCancellableCoroutine
            }

            // 2️⃣  Start barometer stream (we’ll stop after first sample)
            altimeter.startRelativeAltitudeUpdatesToQueue(
                NSOperationQueue.mainQueue()                           // run handler on main ﻿:contentReference[oaicite:1]{index=1}
            ) { data, error ->

                when {
                    // ▸ Runtime error from Core Motion
                    error != null -> {
                        altimeter.stopRelativeAltitudeUpdates()        // clean-up ﻿:contentReference[oaicite:2]{index=2}
                        cont.resumeWithException(
                            IllegalStateException(error.localizedDescription ?: "CoreMotion error")
                        )
                    }

                    // ▸ First valid CMAltitudeData sample
                    data != null -> {
                        altimeter.stopRelativeAltitudeUpdates()        // stop stream immediately

                        // CMAltitudeData fields: relativeAltitude (m) & pressure (kPa) ﻿:contentReference[oaicite:3]{index=3}
                        val (altitudeM, pressureHpa) = data.run {
                            val alt = relativeAltitude.doubleValue   ?: 0.0  // metres ﻿:contentReference[oaicite:4]{index=4}
                            val hpa = (pressure.doubleValue ?: 0.0) * 10     // kPa → hPa :contentReference[oaicite:5]{index=5}
                            alt to hpa
                        }

                        cont.resume(
                            AltitudeData(
                                altitude = altitudeM,
                                pressure = pressureHpa.toFloat(),
                                ts       = (NSDate().timeIntervalSince1970 * 1000).toLong()
                            )
                        )
                    }
                }
            }

            // 3️⃣  Ensure the stream is stopped if caller cancels
            cont.invokeOnCancellation { altimeter.stopRelativeAltitudeUpdates() }
        }
}
