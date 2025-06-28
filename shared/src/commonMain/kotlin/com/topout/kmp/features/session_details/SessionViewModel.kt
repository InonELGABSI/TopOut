package com.topout.kmp.features.session_details

import com.topout.kmp.features.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionViewModel (
    val useCases: SessionUseCases
) : BaseViewModel() {
    private val _uiState: MutableStateFlow<SessionState> = MutableStateFlow<SessionState>(SessionState.Loading)
    val uiState:StateFlow<SessionState> get()= _uiState

    init {
    }

    private fun fetchSession(sessionId: String) {
        scope.launch {
            //val result = useCases.getSession(sessionId)
//            when(result) {
//                is Result.Success -> {
//                    _uiState.emit(SessionState.Loaded(result.data ?: Session()))
//                }
//                is Result.Failure -> {
//                    _uiState.emit(SessionState.Error(errorMessage = result.error?.message ?: "N/A"))
//                }
//            }
        }
    }
}

