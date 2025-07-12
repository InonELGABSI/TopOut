package com.topout.kmp.features.live_session


import StopSession
import com.topout.kmp.domain.GetLiveMetrics
import com.topout.kmp.domain.LiveSessionManager
import com.topout.kmp.domain.StopSession

data class LiveSessionUseCases(
    val startSession: LiveSessionManager,
    val stopSession: StopSession,
    val getLiveMetrics: GetLiveMetrics
//        val pauseSession: StartSession,
//        val resumeSession: StartSession
    )
