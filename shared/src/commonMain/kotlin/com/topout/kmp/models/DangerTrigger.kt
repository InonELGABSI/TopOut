package com.topout.kmp.models

enum class DangerTrigger {
    INSTANT_RATE,   // vᵢ
    AVERAGE_RATE,   // avgV
    RELATIVE_ALT,   // hᵢ − h₀
    TOTAL_GAIN      // gain
}