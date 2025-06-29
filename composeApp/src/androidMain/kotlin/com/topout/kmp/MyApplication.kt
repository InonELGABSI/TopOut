package com.topout.kmp

import android.app.Application
import com.topout.kmp.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin() {
            androidLogger()
            androidContext(this@MyApplication)
        }
    }
}