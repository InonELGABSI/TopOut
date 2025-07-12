package com.topout.kmp.features.live_session
import com.topout.kmp.features.BaseViewModel
import com.topout.kmp.models.TrackPoint
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

    private var trackPointJob: Job? = null

    fun onStartClicked() {
        trackPointJob?.cancel()
        trackPointJob = scope.launch {
            try {
                _uiState.value = LiveSessionState.Loading
                val trackPointFlow = useCases.startSession()
                trackPointFlow.collect { point: TrackPoint ->
                    _uiState.value = LiveSessionState.Loaded(point)
                }
            } catch (ce: CancellationException) {
                // cancellation (user stopped) – ignore
            } catch (e: Exception) {
                _uiState.value = LiveSessionState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onStopClicked(sessionId: String) {
        trackPointJob?.cancel()
        trackPointJob = null
        useCases.startSession.stop()
        scope.launch {
            try {
                val details = useCases.finishSession(sessionId)
                _uiState.value = LiveSessionState.Loading
            } catch (e: Exception) {
                _uiState.value = LiveSessionState.Error(e.message ?: "Error finishing session")
            }
        }
    }
}
