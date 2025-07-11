package com.topout.kmp.features.live_session

import com.topout.kmp.domain.StartSession
import com.topout.kmp.domain.StopSession
import com.topout.kmp.features.BaseViewModel
import com.topout.kmp.features.settings.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class LiveSessionViewModel(
    private val startSession: StartSession,
    private val stopSessionUC: StopSession, // existing
) : BaseViewModel() {

    private var _uiState: MutableStateFlow<LiveSessionState> = MutableStateFlow<LiveSessionState>(LiveSessionState.Loading)
    val uiState: StateFlow<LiveSessionState> get() = _uiState

    fun onStartClicked() = launch {
        startSession().collect { m ->
            setState { copy(metrics = m) }
        }
    }

    fun onStopClicked() {
        startSession.stop()   // stop tracker
        stopSessionUC()       // finalize session
    }
}


