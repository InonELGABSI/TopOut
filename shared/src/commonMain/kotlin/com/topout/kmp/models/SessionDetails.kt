package com.topout.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class SessionDetails(
    val session: Session,
    val points : List<TrackPoint>
)
