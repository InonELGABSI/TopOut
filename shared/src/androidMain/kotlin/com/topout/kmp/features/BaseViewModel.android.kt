package com.topout.kmp.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

actual abstract class BaseViewModel<S: UiState>: ViewModel(), KoinComponent, ViewStateHolder<S> {
    actual val scope: CoroutineScope = viewModelScope
    abstract override val uiState: StateFlow<S>
}