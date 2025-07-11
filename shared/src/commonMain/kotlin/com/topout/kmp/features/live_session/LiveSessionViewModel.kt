package com.topout.kmp.features.live_session

import com.topout.kmp.features.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class LiveSessionViewModel (
    private val useCases: LiveSessionUseCases,
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<LiveSessionState> = MutableStateFlow<LiveSessionState>(LiveSessionState.Loading)
    val uiState: StateFlow<LiveSessionState> get()= _uiState

    init {
    }

    private fun startLiveSession() {
    }

    private fun stopLiveSession() {
    }
}

