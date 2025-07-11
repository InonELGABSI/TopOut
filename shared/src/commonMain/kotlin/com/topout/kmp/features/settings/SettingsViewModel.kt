package com.topout.kmp.features.settings

import com.topout.kmp.features.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.topout.kmp.data.Result
import com.topout.kmp.models.User

class SettingsViewModel (
    val useCases: SettingsUseCases
) : BaseViewModel() {

    private var _uiState: MutableStateFlow<SettingsState> = MutableStateFlow<SettingsState>(SettingsState.Loading)
    val uiState: StateFlow<SettingsState> get() = _uiState

    init {
        loadUser()
    }

    private fun loadUser() {
        scope.launch {
            when (val result = useCases.getSettings()) {
                is Result.Success -> {
                    _uiState.emit(SettingsState.Loaded(result.data as User ))
                }
                is Result.Failure -> {
                    _uiState.emit(SettingsState.Error(errorMessage = result.error?.message ?: "N/A"))
                }
            }
        }
    }
}