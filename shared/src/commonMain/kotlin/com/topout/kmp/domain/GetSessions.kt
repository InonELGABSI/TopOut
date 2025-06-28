package com.topout.kmp.domain

import com.topout.kmp.data.sessions.SessionsRepository

class GetSessions (
    private val sessionsRepository: SessionsRepository
) {
    suspend operator fun invoke() = SessionsRepository.getSessions()

}