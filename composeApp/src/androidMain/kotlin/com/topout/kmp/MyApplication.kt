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

    // keep one application-wide scope instead of GlobalScope
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@MyApplication)

            // expose appScope so modules can `get<CoroutineScope>()`
            modules(module { single<CoroutineScope> { appScope } })
        }

        // optional fire-and-forget task
        appScope.launch {
            getKoin().get<EnsureAnonymousUser>()()
        }
    }
}
