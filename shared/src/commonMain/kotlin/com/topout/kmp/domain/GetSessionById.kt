package com.topout.kmp.domain
import com.topout.kmp.data.sessions.SessionsRepository

class GetSessionById (
    private val sessionsRepository: SessionsRepository
) {
    suspend operator fun invoke(id: String)  = sessionsRepository.getSessionById(id)
}
