package com.topout.kmp.platform

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

actual class SessionBackgroundManager(private val context: Context) {

    private var backgroundScope: CoroutineScope? = null
    private var wakeLock: PowerManager.WakeLock? = null

    actual fun startBackgroundSession() {
        // 1. Start foreground service for background location access
        try {
            val serviceIntent = Intent(context, Class.forName("com.topout.kmp.service.SessionTrackingService"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            // Service not found, continue without it (fallback to wake lock only)
        }

        // 2. Acquire wake lock for additional CPU protection
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TopOut::SessionWakeLock"
        )
        wakeLock?.acquire(2 * 60 * 60 * 1000L) // 2 hours max for climbing sessions

        // 3. Create application-scoped background scope that survives app lifecycle changes
        backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    actual fun stopBackgroundSession() {
        // Stop foreground service
        try {
            val serviceIntent = Intent(context, Class.forName("com.topout.kmp.service.SessionTrackingService"))
            context.stopService(serviceIntent)
        } catch (e: Exception) {
            // Service not found, ignore
        }

        // Release wake lock
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null

        // Cancel background scope
        backgroundScope?.cancel()
        backgroundScope = null
    }

    // Provide access to background scope for tracking
    actual fun getBackgroundScope(): CoroutineScope? = backgroundScope
}