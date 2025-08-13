package com.topout.kmp.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import co.touchlab.kermit.Logger

actual class SessionBackgroundManager {
    private val log = Logger.withTag("SessionBackgroundManager")
    private var backgroundScope: CoroutineScope? = null

    actual fun startBackgroundSession() {
        log.i { "Starting background session" }
        // Create a background scope that can survive app lifecycle changes
        backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    actual fun stopBackgroundSession() {
        log.i { "Stopping background session" }
        backgroundScope?.coroutineContext?.get(kotlinx.coroutines.Job)?.cancel()
        backgroundScope = null
    }

    actual fun getBackgroundScope(): CoroutineScope? {
        return backgroundScope
    }
}