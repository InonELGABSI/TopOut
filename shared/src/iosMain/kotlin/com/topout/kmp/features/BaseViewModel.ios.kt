package com.topout.kmp.features

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent


actual abstract class BaseViewModel<S: UiState> : KoinComponent, ViewStateHolder<S> {

    actual val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    abstract override val uiState: StateFlow<S>

    fun clear() {
        scope.cancel()
    }
}