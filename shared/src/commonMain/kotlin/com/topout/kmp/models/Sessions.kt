package com.topout.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class Sessions(
    val items: List<Session>,
)
