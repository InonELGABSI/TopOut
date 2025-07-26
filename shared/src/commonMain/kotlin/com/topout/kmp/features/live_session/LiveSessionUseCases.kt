package com.topout.kmp.features.live_session

import com.topout.kmp.domain.GetLocalTrackPointsFlow
import com.topout.kmp.domain.session.FinishSession
import com.topout.kmp.domain.CancelLocalSession
import com.topout.kmp.domain.GetCurrentMSLHeight

data class LiveSessionUseCases(
    val finishSession: FinishSession,
    val cancelLocalSession: CancelLocalSession,
    val getLocalTrackPointsFlow: GetLocalTrackPointsFlow,
    val getCurrentMSLHeight: GetCurrentMSLHeight
)
