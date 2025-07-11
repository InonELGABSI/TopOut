package com.topout.kmp.utils
import platform.Foundation.NSUUID

actual fun generateId () : String = NSUUID().UUIDString()