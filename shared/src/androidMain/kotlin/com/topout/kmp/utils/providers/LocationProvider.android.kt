package com.topout.kmp.utils.providers

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.topout.kmp.models.sensor.LocationData
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import co.touchlab.kermit.Logger

actual class LocationProvider(private val context: Context) {

    private val fused = LocationServices.getFusedLocationProviderClient(context)
    private val log = Logger.withTag("LocationProvider")

    @SuppressLint("MissingPermission")
    actual suspend fun getLocation(): LocationData {
        /* 1️⃣ Preconditions */
        if (!hasLocationPermission()) {
            throw SecurityException("Location permission not granted")
        }

        if (Build.VERSION.SDK_INT >= 28) {
            val enabled = context.getSystemService(LocationManager::class.java)?.isLocationEnabled
            if (enabled == false) {
                throw IllegalStateException("Location services disabled")
            }
        }

        return withTimeout(5_000) {
            suspendCancellableCoroutine { cont ->
                val cts = CancellationTokenSource()

                fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val locationData = location.toModel()
                            cont.resume(locationData)
                        } else {
                            fused.lastLocation
                                .addOnSuccessListener { lastLoc ->
                                    if (lastLoc != null) {
                                        val locationData = lastLoc.toModel()
                                        log.i { "Got lastLocation: lat=${locationData.lat}, lon=${locationData.lon}, altitude=${locationData.altitude}" }
                                        cont.resume(locationData)
                                    } else {
                                        log.e { "lastLocation is also null" }
                                        cont.resumeWithException(
                                            IllegalStateException("No location available")
                                        )
                                    }
                                }
                                .addOnFailureListener { error ->
                                    log.e(error) { "Failed to get lastLocation" }
                                    cont.resumeWithException(error)
                                }
                        }
                    }
                    .addOnFailureListener { error ->
                        log.e(error) { "getCurrentLocation failed, falling back to lastLocation" }

                        fused.lastLocation
                            .addOnSuccessListener { lastLoc ->
                                if (lastLoc != null) {
                                    val locationData = lastLoc.toModel()
                                    log.i { "Got lastLocation fallback: lat=${locationData.lat}, lon=${locationData.lon}, altitude=${locationData.altitude}" }
                                    cont.resume(locationData)
                                } else {
                                    log.e(error) { "lastLocation fallback failed" }
                                    cont.resumeWithException(error)
                                }
                            }
                            .addOnFailureListener { fallbackError ->
                                log.e(fallbackError) { "Failed to get lastLocation on fallback" }
                                cont.resumeWithException(error)
                            }
                    }

                cont.invokeOnCancellation {
                    log.d { "invokeOnCancellation: cancelling CancellationTokenSource" }
                    cts.cancel()
                }
            }
        }
    }



    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun android.location.Location.toModel() = LocationData(
        lat = latitude,
        lon = longitude,
        altitude = if (hasAltitude()) altitude else 0.0,
        speed = speed,
        ts = System.currentTimeMillis()
    )
}
