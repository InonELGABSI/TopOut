package com.topout.kmp.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import com.topout.kmp.data.dao.DatabaseDriverFactory
import com.topout.kmp.data.sensors.SensorDataSource
import com.topout.kmp.features.live_session.LiveSessionViewModel
import com.topout.kmp.features.session_details.SessionDetailsViewModel
import com.topout.kmp.features.sessions.SessionsViewModel
import com.topout.kmp.features.settings.SettingsViewModel
import com.topout.kmp.platform.NotificationController
import com.topout.kmp.domain.SessionBackgroundManager
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.LocationProvider
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<AccelerometerProvider> { AccelerometerProvider(context = get() )}
    single<BarometerProvider> { BarometerProvider(context = get() ) }
    single<LocationProvider>{ LocationProvider(context = get() ) }

    single<SensorDataSource> { SensorDataSource(
        context = get(),
        accelProvider = get(),
        baroProvider = get(),
        locProvider = get()
    )}

    single<SessionBackgroundManager> { SessionBackgroundManager(context = get()) }

    single { NotificationController(context = get()) }

    viewModelOf(::SessionsViewModel)
    viewModelOf(::SessionDetailsViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::LiveSessionViewModel)

    single<SqlDriver> { DatabaseDriverFactory(get()).createDriver() }
}