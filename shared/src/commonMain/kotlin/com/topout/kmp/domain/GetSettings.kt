package com.topout.kmp.domain

import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.user.UserRepository

class GetSettings (
    private val userRepository: UserRepository,
    private val remoteFirebaseRepository: FirebaseRepository

) {
//    suspend operator fun invoke() = userRepository.getUser()
suspend operator fun  invoke() = remoteFirebaseRepository.getUser()
}