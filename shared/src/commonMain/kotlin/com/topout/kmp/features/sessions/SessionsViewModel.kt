package com.topout.kmp.features.sessions

import com.topout.kmp.features.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.topout.kmp.data.Result

class SessionsViewModel (
    private val useCases: SessionsUseCases
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<SessionsState> = MutableStateFlow<SessionsState>(SessionsState.Loading)
    val uiState:StateFlow<SessionsState> get()= _uiState

    init {
        fetchSessions()
    }

    fun fetchSessions() {
        scope.launch {
            when (val result = useCases.getSessions()) {
                is Result.Success -> {
                    _uiState.emit(SessionsState.Loaded(result.data?.items))
                }
                is Result.Failure -> {
                    _uiState.emit(SessionsState.Error(errorMessage = result.error?.message ?: "N/A"))
                }
            }
        }
    }
}
