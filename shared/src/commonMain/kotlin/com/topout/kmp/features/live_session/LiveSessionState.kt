package com.topout.kmp.features.live_session
import com.topout.kmp.models.Metrics

public sealed class LiveSessionState {
    data object Loading : LiveSessionState()
    data class Loaded(
        val metrics: Metrics?
    ) : LiveSessionState()
    data class Error(
        var errorMessage: String
    ) : LiveSessionState()
}
