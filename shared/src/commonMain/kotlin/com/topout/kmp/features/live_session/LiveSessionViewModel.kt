package com.topout.kmp.features.live_session

import com.topout.kmp.features.BaseViewModel
import com.topout.kmp.models.Metrics
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class LiveSessionViewModel(
    private val useCases: LiveSessionUseCases
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<LiveSessionState>(LiveSessionState.Loading)
    val uiState: StateFlow<LiveSessionState> = _uiState

    private var metricsJob: Job? = null

    fun onStartClicked() {
        metricsJob?.cancel()
        metricsJob = scope.launch {
            try {
                _uiState.value = LiveSessionState.Loading
                val metricsFlow = useCases.startSession()
                metricsFlow.collect { m: Metrics ->
                    _uiState.value = LiveSessionState.Loaded(m)
                }
            } catch (ce: CancellationException) {
                // cancellation (user stopped) – ignore
            } catch (e: Exception) {
                _uiState.value = LiveSessionState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onStopClicked() {
        metricsJob?.cancel()
        metricsJob = null

        useCases.startSession.stop() // stops tracker/aggregator

        scope.launch {
            useCases.stopSession()
            _uiState.value = LiveSessionState.Loading
        }
    }

//    override fun onCleared() {
//        super.onCleared()
//        metricsJob?.cancel()
//        useCases.startSession.stop()
//    }
}
