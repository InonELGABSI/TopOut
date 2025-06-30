package com.topout.kmp.features.sessions

import com.topout.kmp.features.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.topout.kmp.data.Result
import com.topout.kmp.models.Sessions

class SessionsViewModel (
    private val useCases: SessionsUseCases
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<SessionsState> = MutableStateFlow<SessionsState>(SessionsState.Loading)
    val uiState:StateFlow<SessionsState> get()= _uiState

    init {
        fetchSessions()
    }

    private fun fetchSessions() {
        scope.launch {
            val result = useCases.getSessions()
            when (result) {
                is Result.Success<*> -> {
                    _uiState.emit(SessionsState.Loaded(result.data as? Sessions ?: Sessions(emptyList())))
                }
                is Result.Failure<*> -> {
                    _uiState.emit(SessionsState.Error(errorMessage = result.error?.message ?: "N/A"))
                }
            }
        }
    }
}
