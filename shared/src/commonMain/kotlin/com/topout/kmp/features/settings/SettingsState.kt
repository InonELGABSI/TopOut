package com.topout.kmp.features.settings

import com.topout.kmp.models.User

public sealed class SettingsState {
    data object Loading : SettingsState()
    data class Loaded(
        val user: User
    ) : SettingsState()
    data class Error(
        var errorMessage: String
    ) : SettingsState()
}