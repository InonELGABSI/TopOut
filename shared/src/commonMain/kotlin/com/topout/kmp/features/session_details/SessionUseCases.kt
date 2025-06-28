package com.topout.kmp.features.session_details

import com.topout.kmp.domain.DeleteSession
import com.topout.kmp.domain.GetSessionDetails
import com.topout.kmp.domain.SaveSession

data class SessionUseCases (
    val getSession: GetSessionDetails,
    val DeleteSession: DeleteSession,
    val SaveSession: SaveSession,
)