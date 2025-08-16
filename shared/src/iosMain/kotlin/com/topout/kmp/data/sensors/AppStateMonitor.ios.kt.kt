package com.topout.kmp.data.sensors

import platform.Foundation.*
import platform.UIKit.*
import co.touchlab.kermit.Logger
import platform.darwin.NSObjectProtocol

actual class AppStateMonitor {

    private val log = Logger.withTag("AppStateMonitor")
    private val center = NSNotificationCenter.defaultCenter

    private var didEnterBackgroundObserver: NSObjectProtocol? = null
    private var didBecomeActiveObserver: NSObjectProtocol? = null

    var isForeground: Boolean = true
        private set

    actual fun startMonitoring() {
        stopMonitoring()

        didEnterBackgroundObserver = center.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = null
        ) { _ ->
            log.i { "App moved to background" }
            isForeground = false
        }

        didBecomeActiveObserver = center.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null
        ) { _ ->
            log.i { "App became active (foreground)" }
            isForeground = true
        }
    }

    actual fun stopMonitoring() {
        didEnterBackgroundObserver?.let {
            center.removeObserver(it)
            didEnterBackgroundObserver = null
        }

        didBecomeActiveObserver?.let {
            center.removeObserver(it)
            didBecomeActiveObserver = null
        }
    }
}
