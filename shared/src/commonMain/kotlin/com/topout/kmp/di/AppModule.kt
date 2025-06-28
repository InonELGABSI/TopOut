package com.topout.kmp.di

fun initkoin(config: KoinApplication? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModules())
    }
}
fun appModules() = listOf(domainModule, databaseModule, platformModule)

expect val platformModule
