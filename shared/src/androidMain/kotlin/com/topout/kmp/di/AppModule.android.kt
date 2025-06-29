package com.topout.kmp.di

import com.topout.kmp.features.session_details.SessionViewModel
import com.topout.kmp.features.sessions.SessionsViewModel
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.GPSProvider
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<AccelerometerProvider> { AccelerometerProvider(context = get() )}
    single<BarometerProvider> { BarometerProvider(context = get() ) }
    single<GPSProvider>{ GPSProvider(context = get() ) }

    viewModelOf(::SessionsViewModel)
    viewModelOf(::SessionViewModel)


}