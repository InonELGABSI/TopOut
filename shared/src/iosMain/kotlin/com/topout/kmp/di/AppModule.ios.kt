package com.topout.kmp.di

import app.cash.sqldelight.db.SqlDriver
import com.topout.kmp.data.dao.DatabaseDriverFactory
import com.topout.kmp.data.sensors.AppStateMonitor
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.LocationProvider
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.data.sensors.SensorDataSource
import com.topout.kmp.features.live_session.LiveSessionViewModel
import com.topout.kmp.features.session_details.SessionDetailsViewModel
import com.topout.kmp.features.sessions.SessionsViewModel
import com.topout.kmp.features.settings.SettingsViewModel
import com.topout.kmp.platform.NotificationController
import com.topout.kmp.domain.SessionBackgroundManager
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { Darwin.create() }

    single<AccelerometerProvider> { AccelerometerProvider() }
    single<BarometerProvider> { BarometerProvider() }
    single<LocationProvider> { LocationProvider() }

    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single<SessionBackgroundManager> { SessionBackgroundManager() }
    single { NotificationController() }
    single<SensorDataSource> { SensorDataSource(
        accelProvider = get(),
        baroProvider = get(),
        locProvider = get(),
        appStateMonitor = get<AppStateMonitor>()
    )}

    single { AppStateMonitor().apply { startMonitoring() } }

    single { SessionsViewModel(get()) }
    single { SessionDetailsViewModel(get()) }
    single { SettingsViewModel(get()) }
    single { LiveSessionViewModel(get()) }

    single<SqlDriver> { DatabaseDriverFactory().createDriver() }
}