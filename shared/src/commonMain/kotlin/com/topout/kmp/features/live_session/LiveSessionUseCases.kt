package com.topout.kmp.features.live_session

import com.topout.kmp.domain.GetLiveMetrics
import com.topout.kmp.domain.session.FinishSession

data class LiveSessionUseCases(
    val finishSession: FinishSession,
    val getLiveMetrics: GetLiveMetrics
)
