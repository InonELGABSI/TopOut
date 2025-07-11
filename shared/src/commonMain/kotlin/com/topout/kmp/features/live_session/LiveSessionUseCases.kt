package com.topout.kmp.features.live_session


import com.topout.kmp.domain.GetLiveMetrics
import com.topout.kmp.domain.StartSession
import com.topout.kmp.domain.StopSession

data class LiveSessionUseCases(
        val startSession: StartSession,
        val stopSession: StopSession,
        val getLiveMetrics: GetLiveMetrics
//        val pauseSession: StartSession,
//        val resumeSession: StartSession
    )
