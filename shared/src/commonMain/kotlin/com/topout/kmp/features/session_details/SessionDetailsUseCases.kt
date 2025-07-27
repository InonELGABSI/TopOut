package com.topout.kmp.features.session_details

import com.topout.kmp.domain.DeleteSession
import com.topout.kmp.domain.GetSessionDetails
import com.topout.kmp.domain.UpdateSessionTitle
import com.topout.kmp.domain.SaveSession

data class SessionDetailsUseCases(
    val getSessionDetails: GetSessionDetails,
    val deleteSession: DeleteSession,
    val saveSession: SaveSession,
    val updateSessionTitle: UpdateSessionTitle
)

