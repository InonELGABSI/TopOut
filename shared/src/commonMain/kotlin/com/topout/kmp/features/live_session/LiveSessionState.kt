package com.topout.kmp.features.live_session
import com.topout.kmp.models.TrackPoint

sealed class LiveSessionState {
    object Loading : LiveSessionState()
    data class Loaded(
        val trackPoint: TrackPoint
    ) : LiveSessionState()
    data class Error(
        var errorMessage: String
    ) : LiveSessionState()
}
