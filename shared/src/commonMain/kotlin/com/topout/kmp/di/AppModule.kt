package com.topout.kmp.di

import com.topout.kmp.AppDatabase
import com.topout.kmp.data.dao.SessionDao
import com.topout.kmp.data.dao.UserDao
import com.topout.kmp.data.dao.TrackPointsDao
import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.firebase.RemoteFirebaseRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import com.topout.kmp.data.sessions.LocalSessionsRepository
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.data.track_points.LocalTrackPointsRepository
import com.topout.kmp.data.track_points.TrackPointsRepository
import com.topout.kmp.data.user.LocalUserRepository
import com.topout.kmp.data.user.UserRepository
import com.topout.kmp.domain.DeleteSession
import com.topout.kmp.domain.EnsureAnonymousUser
import com.topout.kmp.domain.GetSessionById
import com.topout.kmp.domain.GetSessions
import com.topout.kmp.domain.GetSessionDetails
import com.topout.kmp.domain.GetSettings
import com.topout.kmp.domain.SaveSession
import com.topout.kmp.domain.SignInAnonymously
import com.topout.kmp.domain.LiveSessionManager
import com.topout.kmp.domain.SyncOfflineChanges
import com.topout.kmp.domain.session.FinishSession
import com.topout.kmp.domain.CancelLocalSession
import com.topout.kmp.domain.GetLocalTrackPointsFlow
import com.topout.kmp.domain.UpdateSessionTitle
import com.topout.kmp.domain.UpdateUser
import com.topout.kmp.domain.GetCurrentMSLHeight
import com.topout.kmp.features.live_session.LiveSessionUseCases
import com.topout.kmp.features.session_details.SessionDetailsUseCases
import com.topout.kmp.features.sessions.SessionsUseCases
import com.topout.kmp.features.settings.SettingsUseCases
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
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
    factoryOf(::SyncOfflineChanges)

    // Sessions
    factoryOf(::GetSessions)
    factoryOf(::GetSessionById)
    factoryOf(::GetSessionDetails)
    factoryOf(::SaveSession)
    factoryOf(::DeleteSession)
    factoryOf(::UpdateSessionTitle)

    factoryOf(::FinishSession)
    factoryOf(::CancelLocalSession)
    factoryOf(::GetLocalTrackPointsFlow)
    factory { (scope: CoroutineScope) ->
        LiveSessionManager(get(), get(), get(), scope, get<LocalUserRepository>())
    }

    factoryOf(::LiveSessionUseCases)
    factoryOf(::SessionsUseCases)
    factoryOf(::SessionDetailsUseCases)

    factoryOf(::GetCurrentMSLHeight)

    // User Settings
    factoryOf(::GetSettings)
    factoryOf(::UpdateUser)
    factoryOf(::SettingsUseCases)
}

val commonModule = module {
    singleOf(::createJson)
    singleOf(::RemoteFirebaseRepository).bind<FirebaseRepository>()
    singleOf(::LocalSessionsRepository).bind<SessionsRepository>()
    singleOf(::LocalUserRepository).bind<UserRepository>()
    singleOf(::LocalTrackPointsRepository).bind<TrackPointsRepository>()

    single { AppDatabase(get()) }

    single { get<AppDatabase>().sessionsQueries }
    singleOf(::SessionDao)
    single { get<AppDatabase>().userQueries }
    singleOf(::UserDao)
    single { get<AppDatabase>().track_pointsQueries }
    singleOf(::TrackPointsDao)

    single { createHttpClient(get(), get()) }

    // Location-based use cases
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

