package com.topout.kmp.platform

import kotlinx.coroutines.CoroutineScope

/**
 * Platform-specific session background manager
 * Keeps sessions alive when app goes to background
 */
expect class SessionBackgroundManager {
    fun startBackgroundSession()
    fun stopBackgroundSession()
    fun getBackgroundScope(): CoroutineScope?
}