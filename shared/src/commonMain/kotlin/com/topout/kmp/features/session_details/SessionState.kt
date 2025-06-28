package com.topout.kmp.features.session_details

import com.topout.kmp.models.Session

public sealed class SessionState {
    data object Loading : SessionState()
    data class Loaded(
        val session: Session
    ) : SessionState()
    data class Error(
        var errorMessage: String
    ) : SessionState()
}