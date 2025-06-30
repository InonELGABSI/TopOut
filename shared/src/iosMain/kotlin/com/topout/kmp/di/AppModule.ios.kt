package com.topout.kmp.di

import app.cash.sqldelight.db.SqlDriver
import com.topout.kmp.data.dao.DatabaseDriverFactory
import com.topout.kmp.utils.providers.AccelerometerProvider
import com.topout.kmp.utils.providers.BarometerProvider
import com.topout.kmp.utils.providers.GPSProvider
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { Darwin.create() }
    singleOf(::AccelerometerProvider)
    singleOf(::BarometerProvider)
    singleOf(::GPSProvider)

    factoryOf(::SessionsViewModel)
    factoryOf(::SessionViewModel)

    single<SqlDriver> { DatabaseDriverFactory().createDriver() }
}
