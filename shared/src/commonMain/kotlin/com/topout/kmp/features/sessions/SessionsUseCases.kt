package com.topout.kmp.features.sessions

import com.topout.kmp.domain.DeleteSession
import com.topout.kmp.domain.GetSessionById
import com.topout.kmp.domain.GetSessions
import com.topout.kmp.domain.SaveSession

data class SessionsUseCases (
    val getSessions: GetSessions,
    val getSessionById : GetSessionById,
    val saveSession: SaveSession,
    val deleteSession: DeleteSession
)