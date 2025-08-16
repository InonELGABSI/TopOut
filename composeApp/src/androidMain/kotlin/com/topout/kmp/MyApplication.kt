package com.topout.kmp

import android.app.Application
import com.topout.kmp.di.initKoin
import com.topout.kmp.domain.EnsureAnonymousUser
import kotlinx.coroutines.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.ext.android.getKoin   // <-- correct import
import org.koin.dsl.module

class MyApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@MyApplication)

            modules(module { single<CoroutineScope> { appScope } })
        }

        appScope.launch {
            getKoin().get<EnsureAnonymousUser>()()
        }
    }
}
