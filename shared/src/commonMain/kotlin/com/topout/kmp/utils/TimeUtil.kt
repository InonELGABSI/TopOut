package com.topout.kmp.utils

import kotlinx.datetime.Clock

fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()
