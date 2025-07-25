package com.topout.kmp.features.live_session
import com.topout.kmp.models.TrackPoint

sealed class LiveSessionState {
    object Loading : LiveSessionState()
    data class Loaded(
        val trackPoint: TrackPoint,
        val historyTrackPoints: List<TrackPoint> = emptyList()
    ) : LiveSessionState()
    object Stopping : LiveSessionState()
    data class SessionStopped(
        val sessionId: String
    ) : LiveSessionState()
    data class Error(
        var errorMessage: String
    ) : LiveSessionState()
}
