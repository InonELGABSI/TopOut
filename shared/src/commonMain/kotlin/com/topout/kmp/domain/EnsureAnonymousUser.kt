package com.topout.kmp.domain

import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.user.UserError
import com.topout.kmp.data.Result

/**
 *  Run once at app launch – signs in anonymously if no cached user exists.
 */
class EnsureAnonymousUser(
    private val firebaseRepo: FirebaseRepository
) {
    suspend operator fun invoke() {
        try {
            println("Starting anonymous user setup...")

            when (val authResult = firebaseRepo.signInAnonymously()) {
                is Result.Success -> {
                    println("Anonymous sign-in successful")

                    when (val docResult = firebaseRepo.ensureUserDocument()) {
                        is Result.Success -> {
                            println("User document ensured successfully")
                        }
                        is Result.Failure -> {
                            println("Failed to ensure user document: ${docResult.error}")
                        }
                    }
                }
                is Result.Failure -> {
                    println("Failed to sign in anonymously: ${authResult.error}")
                    return
                }
            }
        } catch (e: Exception) {
            println("Failed to ensure anonymous user: ${e.message}")
        }
    }
}