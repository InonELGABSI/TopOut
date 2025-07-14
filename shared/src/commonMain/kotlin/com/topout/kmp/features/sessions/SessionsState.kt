package com.topout.kmp.features.sessions

import com.topout.kmp.models.Session

public sealed class SessionsState {
    data object Loading : SessionsState()
    data class Loaded(
        val sessions: List<Session>?
    ) : SessionsState()
    data class Error(
        var errorMessage: String
    ) : SessionsState()
}