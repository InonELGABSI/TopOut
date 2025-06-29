package com.topout.kmp.domain

import com.topout.kmp.data.firebase.FirebaseRepository

class SignInAnonymously(
    private val firebaseRepository: FirebaseRepository
) {
    suspend operator fun invoke() {
        firebaseRepository.signInAnonymously()
    }
}