package com.topout.kmp.di

import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.LocationProvider
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.data.sensors.SensorDataSource
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { Darwin.create() }
    singleOf(::AccelerometerProvider)
    singleOf(::BarometerProvider)
    singleOf(::LocationProvider)

    // iosMain/di/platformModule.kt
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    // Sensors - Platform-specific SensorDataSource
    single { SensorDataSource(get(),get(),get()) }
}
