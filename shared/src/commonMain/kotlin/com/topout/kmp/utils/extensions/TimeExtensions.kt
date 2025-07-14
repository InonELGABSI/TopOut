package com.topout.kmp.utils.extensions

import dev.gitlive.firebase.firestore.Timestamp

/* Long millis ➜ Firestore Timestamp (dev.gitlive) */
fun Long.toTimestamp(): Timestamp =
    Timestamp(this / 1_000, ((this % 1_000) * 1_000_000).toInt())

/* Firestore Timestamp ➜ Long millis */
fun Timestamp.toEpochMillis(): Long =
    seconds * 1_000 + nanoseconds / 1_000_000
