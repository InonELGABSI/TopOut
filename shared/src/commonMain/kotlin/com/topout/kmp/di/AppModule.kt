package com.topout.kmp.di

import com.topout.kmp.AppDatabase
import com.topout.kmp.data.dao.SessionDao
import com.topout.kmp.data.dao.UserDao
import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.firebase.RemoteFirebaseRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import com.topout.kmp.data.sessions.RemoteSessionsRepository
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.data.user.RemoteUserRepository
import com.topout.kmp.data.user.UserRepository
import com.topout.kmp.domain.DeleteSession
import com.topout.kmp.domain.EnsureAnonymousUser
import com.topout.kmp.domain.GetSessionById
import com.topout.kmp.domain.GetSessions
import com.topout.kmp.domain.GetSessionDetails
import com.topout.kmp.domain.GetSettings
import com.topout.kmp.domain.SaveSession
import com.topout.kmp.domain.SignInAnonymously
import com.topout.kmp.features.session_details.SessionUseCases
import com.topout.kmp.features.sessions.SessionsUseCases
import com.topout.kmp.features.sessions.SessionsViewModel
import com.topout.kmp.features.settings.SettingsUseCases
import com.topout.kmp.features.settings.SettingsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin() {
        config?.invoke(this)
        modules(listOf(commonModule, domainModule, platformModule))
    }
}

fun initKoin() = initKoin { }

expect val platformModule:Module

// domain and use cases
val domainModule = module {
    factoryOf(::SignInAnonymously)
    factoryOf(::EnsureAnonymousUser)

    // Sessions
    factoryOf(::GetSessions)
    factoryOf(::GetSessionById)
    factoryOf(::GetSessionDetails)
    factoryOf(::SaveSession)
    factoryOf(::DeleteSession)

    factoryOf(::SessionsUseCases)
    factoryOf(::SessionUseCases)
    factoryOf(::SessionsViewModel)


    // Settings
    factoryOf(::GetSettings)
    factoryOf(::SettingsUseCases)
    factoryOf(::SettingsViewModel)
}

val commonModule = module {
    singleOf(::createJson)
    singleOf(::RemoteFirebaseRepository).bind<FirebaseRepository>()
    singleOf(::RemoteSessionsRepository).bind<SessionsRepository>()
    singleOf(::RemoteUserRepository).bind<UserRepository>()

    single { createHttpClient(get(), get()) }

    single { AppDatabase(get()) }

    single { get<AppDatabase>().sessionsQueries }
    single { SessionDao(get()) }

    single { get<AppDatabase>().userQueries}
    single { UserDao(get()) }

}

    fun createJson() = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    fun createHttpClient(clientEngine: HttpClientEngine, json: Json) = HttpClient(clientEngine) {
        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.DEFAULT
        }
        install(ContentNegotiation) {
            json(json)
        }
    }


