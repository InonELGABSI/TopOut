package com.topout.kmp.features

import kotlinx.coroutines.CoroutineScope

expect open class BaseViewModel() {
    val scope : CoroutineScope
}