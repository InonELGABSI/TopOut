package com.topout.kmp.data.sensors

actual class AppStateMonitor actual constructor() {
    private var foreground = true

    actual fun startMonitoring() {
        // Register lifecycle callbacks to update foreground flag
    }

    actual fun stopMonitoring() {
        // Unregister lifecycle callbacks
    }
}
