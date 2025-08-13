package com.topout.kmp.platform

import kotlinx.coroutines.CoroutineScope

actual class SessionBackgroundManager {

    actual fun startBackgroundSession() {
        // iOS handles background sessions differently - no action needed
    }

    actual fun stopBackgroundSession() {
        // iOS handles background sessions differently - no action needed
    }

    actual fun getBackgroundScope(): CoroutineScope? {
        // iOS doesn't need a background scope - return null
        return null
    }
}