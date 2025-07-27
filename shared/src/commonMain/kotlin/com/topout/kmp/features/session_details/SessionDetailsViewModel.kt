package com.topout.kmp.features.session_details

import com.topout.kmp.data.Result
import com.topout.kmp.features.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionDetailsViewModel (
    private val useCases: SessionDetailsUseCases
) : BaseViewModel() {
    private val _uiState: MutableStateFlow<SessionDetailsState> = MutableStateFlow<SessionDetailsState>(SessionDetailsState.Loading)
    val uiState:StateFlow<SessionDetailsState> get()= _uiState

    fun loadSession(sessionId: String) {
        scope.launch {
            _uiState.emit(SessionDetailsState.Loading)
            val result = useCases.getSessionDetails(sessionId)
            when(result) {
                is Result.Success -> {
                    result.data?.let { sessionDetails ->
                        //test animated loader
//                        _uiState.emit(SessionDetailsState.Loading)
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

    fun deleteSession(sessionId: String) {
        scope.launch {
            val result = useCases.deleteSession(sessionId)
            when(result) {
                is Result.Success -> {
                    // Navigate back or show success message
                }
                is Result.Failure -> {
                    _uiState.emit(SessionDetailsState.Error(result.error?.message ?: "Failed to delete session"))
                }
            }
        }
    }

    fun updateSessionTitle(sessionId: String, newTitle: String) {
        scope.launch {
            val result = useCases.updateSessionTitle(sessionId, newTitle)
            when(result) {
                is Result.Success -> {
                    // Update the UI state directly instead of reloading
                    val currentState = _uiState.value
                    if (currentState is SessionDetailsState.Loaded) {
                        val updatedSessionDetails = currentState.sessionDetails.copy(
                            session = currentState.sessionDetails.session.copy(title = newTitle)
                        )
                        _uiState.emit(SessionDetailsState.Loaded(updatedSessionDetails))
                    }
                }
                is Result.Failure -> {
                    _uiState.emit(SessionDetailsState.Error(result.error?.message ?: "Failed to update session title"))
                }
            }
        }
    }
}
