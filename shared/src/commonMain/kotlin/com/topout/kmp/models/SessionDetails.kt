package com.topout.kmp.models

import kotlinx.serialization.Serializable

/** Combined object used only in app logic / UI */
@Serializable
data class SessionDetails(
    val session: Session,
    val points : List<TrackPoint>
)
