package com.topout.kmp.features.live_session
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.domain.MSLHeightData

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

sealed class MSLHeightState {
    object Loading : MSLHeightState()
    data class Success(val data: MSLHeightData) : MSLHeightState()
    data class Error(val message: String) : MSLHeightState()
}
