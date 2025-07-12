package com.topout.kmp.features.live_session

import com.topout.kmp.domain.GetLiveMetrics
import com.topout.kmp.domain.LiveSessionManager
import com.topout.kmp.domain.session.FinishSession

data class LiveSessionUseCases(
    val startSession: LiveSessionManager,
    val finishSession: FinishSession,
    val getLiveMetrics: GetLiveMetrics
//        val pauseSession: StartSession,
//        val resumeSession: StartSession
    )
