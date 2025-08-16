package com.topout.kmp.domain

import kotlinx.coroutines.*
import co.touchlab.kermit.Logger
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.UIKit.*
import platform.darwin.NSObjectProtocol

actual class SessionBackgroundManager {
    private val log = Logger.withTag("SessionBackgroundManager")
    private var backgroundScope: CoroutineScope? = null

    private var bgTaskId: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
    private var didEnterBgObs: NSObjectProtocol? = null
    private var didBecomeActiveObs: NSObjectProtocol? = null

    actual fun startBackgroundSession() {
        if (backgroundScope != null) {
            log.d { "Background session already started" }
            return
        }
        log.i { "Starting background session" }

        backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        val center = NSNotificationCenter.defaultCenter
        didEnterBgObs = center.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = null
        ) { _: NSNotification? ->
            beginBackgroundWindow()
        }

        didBecomeActiveObs = center.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null
        ) { _: NSNotification? ->
            endBackgroundWindow()
        }
    }

    actual fun stopBackgroundSession() {
        log.i { "Stopping background session" }
        backgroundScope?.cancel()
        backgroundScope = null
        endBackgroundWindow()

        val center = NSNotificationCenter.defaultCenter
        didEnterBgObs?.let { center.removeObserver(it); didEnterBgObs = null }
        didBecomeActiveObs?.let { center.removeObserver(it); didBecomeActiveObs = null }
    }

    private fun beginBackgroundWindow() {
        if (bgTaskId != UIBackgroundTaskInvalid) return
        bgTaskId = UIApplication.sharedApplication.beginBackgroundTaskWithName("session-location") {
            log.w { "Background task expired by system" }
            endBackgroundWindow()
        }
        log.d { "Background window started (id=$bgTaskId)" }
    }

    private fun endBackgroundWindow() {
        if (bgTaskId != UIBackgroundTaskInvalid) {
            UIApplication.sharedApplication.endBackgroundTask(bgTaskId)
            log.d { "Background window ended (id=$bgTaskId)" }
            bgTaskId = UIBackgroundTaskInvalid
        }
    }

    actual fun getBackgroundScope(): CoroutineScope? = backgroundScope
}
