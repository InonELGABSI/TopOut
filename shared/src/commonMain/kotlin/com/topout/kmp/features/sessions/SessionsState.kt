package com.topout.kmp.features.sessions

import com.topout.kmp.models.Sessions

public sealed class SessionsState {
    data object Loading : SessionsState()
    data class Loaded(
        val sessions: Sessions
    ) : SessionsState()
    data class Error(
        var errorMessage: String
    ) : SessionsState()
}