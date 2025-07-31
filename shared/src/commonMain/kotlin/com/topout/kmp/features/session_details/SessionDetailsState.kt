package com.topout.kmp.features.session_details

import com.topout.kmp.features.UiState
import com.topout.kmp.models.SessionDetails

public sealed class SessionDetailsState : UiState{
    data object Loading : SessionDetailsState()
    data class Loaded(
        val sessionDetails: SessionDetails
    ) : SessionDetailsState()
    data class Error(
        var errorMessage: String
    ) : SessionDetailsState()
}

