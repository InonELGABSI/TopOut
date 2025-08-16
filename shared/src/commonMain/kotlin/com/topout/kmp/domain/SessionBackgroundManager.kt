package com.topout.kmp.domain

import kotlinx.coroutines.CoroutineScope

expect class SessionBackgroundManager {
    fun startBackgroundSession()
    fun stopBackgroundSession()
    fun getBackgroundScope(): CoroutineScope?
}