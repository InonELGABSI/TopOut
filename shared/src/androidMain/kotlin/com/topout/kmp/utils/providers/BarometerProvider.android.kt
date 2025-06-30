package com.topout.kmp.utils.providers

import android.content.Context

actual class BarometerProvider(private val context: Context) {
    actual suspend fun getPressureData(): Float {
        TODO("Not yet implemented")
    }

    actual suspend fun getAltitudeData(): Float {
        TODO("Not yet implemented")
    }

}