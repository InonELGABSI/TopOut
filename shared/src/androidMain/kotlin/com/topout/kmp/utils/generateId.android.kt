package com.topout.kmp.utils

import java.util.UUID

actual fun generateId () : String = UUID.randomUUID().toString()
