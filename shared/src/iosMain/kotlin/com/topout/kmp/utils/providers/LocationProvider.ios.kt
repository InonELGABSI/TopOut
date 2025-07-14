package com.topout.kmp.utils.providers

import com.topout.kmp.models.sensor.LocationData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.*
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * One-shot, high-accuracy location fetch.
 *
 * • Requires `NSLocationWhenInUseUsageDescription` (or Always) in Info.plist.
 * • Returns latitude/longitude/altitude/speed + epoch-ms timestamp.
 * • Automatically stops Core Location once a single fix is delivered.
 */
actual class LocationProvider : NSObject(), CLLocationManagerDelegateProtocol {

    private val manager = CLLocationManager().apply {
        desiredAccuracy = kCLLocationAccuracyBest      // ≤10 m when hardware allows :contentReference[oaicite:0]{index=0}
        distanceFilter  = kCLDistanceFilterNone
    }

    private var cont: kotlinx.coroutines.CancellableContinuation<LocationData>? = null

    actual suspend fun getLocation(): LocationData =
        suspendCancellableCoroutine { continuation ->
            cont = continuation

            // 1️⃣ Make sure location services are enabled system-wide.
            if (!CLLocationManager.locationServicesEnabled()) {         // system toggle check :contentReference[oaicite:1]{index=1}
                continuation.resumeWithException(
                    IllegalStateException("Location services disabled in Settings")
                )
                return@suspendCancellableCoroutine
            }

            // 2️⃣ Handle runtime permission.
            when (CLLocationManager.authorizationStatus()) {            // static convenience API :contentReference[oaicite:2]{index=2}
                kCLAuthorizationStatusNotDetermined -> {
                    manager.delegate = this
                    manager.requestWhenInUseAuthorization()             // async prompt :contentReference[oaicite:3]{index=3}
                    // `requestLocation()` will be called in the delegate
                }
                kCLAuthorizationStatusDenied,
                kCLAuthorizationStatusRestricted -> {
                    continuation.resumeWithException(
                        IllegalStateException("Location permission denied")
                    )
                }
                else -> { // authorized
                    manager.delegate = this
                    manager.requestLocation()                           // one-shot API :contentReference[oaicite:4]{index=4}
                }
            }
        }

    /* ───────── CLLocationManagerDelegate ───────── */

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>
    ) {
        val loc = didUpdateLocations.last() as CLLocation

        // Safely unwrap the C-struct to reach lat/lon in Kotlin/Native.
        val (lat, lon) = loc.coordinate.useContents { latitude to longitude }  // idiomatic K/N ﻿:contentReference[oaicite:5]{index=5}

        cont?.resume(
            LocationData(
                lat      = lat,
                lon      = lon,
                altitude = loc.altitude,              // Core Location gives metres above sea level :contentReference[oaicite:6]{index=6}
                speed    = loc.speed.toFloat(),       // already metres-per-second :contentReference[oaicite:7]{index=7}
                ts       = (NSDate().timeIntervalSince1970 * 1000).toLong()
            )
        )
        cont = null
    }

    override fun locationManager(
        manager: CLLocationManager,
        didFailWithError: NSError
    ) {
        cont?.resumeWithException(
            IllegalStateException(didFailWithError.localizedDescription)
        )
        cont = null
    }

    // iOS 14+: handle the user’s response to the permission prompt.
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        if (cont == null) return
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> manager.requestLocation()
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted      -> cont?.resumeWithException(
                IllegalStateException("Location permission denied")
            )
            else -> Unit
        }
    }
}
