package com.topout.kmp.di

import app.cash.sqldelight.db.SqlDriver
import com.topout.kmp.data.dao.DatabaseDriverFactory
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.LocationProvider
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.data.sensors.SensorDataSource
import com.topout.kmp.features.live_session.LiveSessionViewModel
import com.topout.kmp.features.session_details.SessionDetailsViewModel
import com.topout.kmp.features.sessions.SessionsViewModel
import com.topout.kmp.features.settings.SettingsViewModel
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { Darwin.create() }

    // iOS providers have empty constructors, so we use explicit factory functions
    single<AccelerometerProvider> { AccelerometerProvider() }
    single<BarometerProvider> { BarometerProvider() }
    single<LocationProvider> { LocationProvider() }

    // iosMain/di/platformModule.kt
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    // Sensors - Platform-specific SensorDataSource
    single { SensorDataSource(get(),get(),get()) }

    // ViewModels - Add these to match Android platform module
    single { SessionsViewModel(get()) }
    single { SessionDetailsViewModel(get()) }
    single { SettingsViewModel(get()) }
    single { LiveSessionViewModel(get()) }

    single<SqlDriver> { DatabaseDriverFactory().createDriver() }
}
