package com.topout.kmp.utils.providers

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.topout.kmp.models.sensor.LocationData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * One-shot, high-accuracy location fetch using Google Play-Services FLP.
 *
 * • Caller must already hold FINE or COARSE permission.
 * • 15-second hard timeout to avoid UI hangs.
 * • Cancels radios immediately when coroutine is cancelled.
 */
actual class LocationProvider(private val context: Context) {

    private val fused = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")                  // we verify permission inside
    actual suspend fun getLocation(): LocationData = withTimeout(15_000) {   // Google docs suggest a timeout for single fixes :contentReference[oaicite:0]{index=0}
        /* 1️⃣ Preconditions */

        if (!hasLocationPermission()) {                 // runtime check per Google guide :contentReference[oaicite:1]{index=1}
            throw SecurityException("Location permission not granted")
        }

        // System-wide Location toggle (API 28+). Older OS versions return true.
        if (Build.VERSION.SDK_INT >= 28) {
            val enabled = context.getSystemService(LocationManager::class.java)?.isLocationEnabled
            if (enabled == false) {
                throw IllegalStateException("Location services disabled")     // Android UX guideline :contentReference[oaicite:2]{index=2}
            }
        }

        /* 2️⃣ One-shot request with cancellation */

        suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()

            fused
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)  // recommended API ﻿:contentReference[oaicite:3]{index=3}
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        cont.resume(loc.toModel())
                    } else {
                        /* 3️⃣ Quick fallback — cached last location */
                        fused.lastLocation
                            .addOnSuccessListener { last ->
                                if (last != null) cont.resume(last.toModel())
                                else cont.resumeWithException(
                                    IllegalStateException("Location unavailable")
                                )
                            }
                            .addOnFailureListener(cont::resumeWithException)
                    }
                }
                .addOnFailureListener(cont::resumeWithException)

            // Propagate coroutine cancellation to FLP
            cont.invokeOnCancellation {
                cts.cancel()                                                    // CancellationToken docs :contentReference[oaicite:4]{index=4}
                cont.resumeWithException(CancellationException("Location call cancelled"))
            }
        }
    }

    /* ---------- helpers ---------- */

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun android.location.Location.toModel() = LocationData(
        lat = latitude,
        lon = longitude,
        altitude = altitude,
        speed = speed,
        ts = System.currentTimeMillis()
    )
}
