package com.topout.kmp

import android.app.Application
import com.topout.kmp.di.initKoin
import com.topout.kmp.domain.EnsureAnonymousUser
import kotlinx.coroutines.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.ext.android.getKoin   // <-- correct import

class MyApplication : Application() {

    // keep one application-wide scope instead of GlobalScope
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // 1. start DI
        initKoin {
            androidLogger()
            androidContext(this@MyApplication)
        }

        // 2. fire-and-forget anonymous sign-in (only creates an account on first run)
        // run the suspend use-case once, off the main thread
        appScope.launch {
            getKoin()                     // <- NOT composable
                .get<EnsureAnonymousUser>()()   // invoke() operator
        }
    }
}
