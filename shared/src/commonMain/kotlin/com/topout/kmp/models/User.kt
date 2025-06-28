package com.topout.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,

    val unitPreference: String = "meters",
    val dangerSettings: DangerSettings = DangerSettings(),

    val createdAt: Long = 0L
)

