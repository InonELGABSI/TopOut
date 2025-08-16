package com.topout.kmp.features.session_details

import com.topout.kmp.data.Result
import com.topout.kmp.features.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionDetailsViewModel (
    private val useCases: SessionDetailsUseCases
) : BaseViewModel<SessionDetailsState>() {
    private val _uiState: MutableStateFlow<SessionDetailsState> = MutableStateFlow(SessionDetailsState.Loading)
    override val uiState:StateFlow<SessionDetailsState> get()= _uiState

    fun loadSession(sessionId: String) {
        scope.launch {
            _uiState.emit(SessionDetailsState.Loading)
            val result = useCases.getSessionDetails(sessionId)
            when(result) {
                is Result.Success -> {
                    result.data?.let { sessionDetails ->
                        _uiState.emit(SessionDetailsState.Loaded(sessionDetails))
                    } ?: run {
                        _uiState.emit(SessionDetailsState.Error("Session details not found"))
                    }
                }
                is Result.Failure -> {
                    _uiState.emit(SessionDetailsState.Error(result.error?.message ?: "Failed to load session"))
                }
            }
        }
    }

    fun deleteSession(sessionId: String, onResult: (Boolean) -> Unit) {
        scope.launch {
            val result = useCases.deleteSession(sessionId)
            val success = when(result) {
                is Result.Success -> {
                    true
                }
                is Result.Failure -> {
                    _uiState.emit(SessionDetailsState.Error(result.error?.message ?: "Failed to delete session"))
                    false
                }
            }
            onResult(success)
        }
    }

    fun updateSessionTitle(sessionId: String, newTitle: String, onResult: (Boolean) -> Unit) {
        scope.launch {
            val result = useCases.updateSessionTitle(sessionId, newTitle)
            val success = when(result) {
                is Result.Success -> {
                    val currentState = _uiState.value
                    if (currentState is SessionDetailsState.Loaded) {
                        val updatedSessionDetails = currentState.sessionDetails.copy(
                            session = currentState.sessionDetails.session.copy(title = newTitle)
                        )
                        _uiState.emit(SessionDetailsState.Loaded(updatedSessionDetails))
                    }
                    true
                }
                is Result.Failure -> {
                    _uiState.emit(SessionDetailsState.Error(result.error?.message ?: "Failed to update session title"))
                    false
                }
            }
            onResult(success)
        }
    }
}
