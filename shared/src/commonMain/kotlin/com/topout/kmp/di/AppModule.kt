package com.topout.kmp.di

import com.topout.kmp.data.firebase.FirebaseRepository
import com.topout.kmp.data.firebase.RemoteFirebaseRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import com.topout.kmp.data.sessions.RemoteSessionsRepository
import com.topout.kmp.data.sessions.SessionsRepository
import com.topout.kmp.domain.GetSessionDetails
import com.topout.kmp.domain.GetSessions
import com.topout.kmp.domain.SignInAnonymously
import com.topout.kmp.features.session_details.SessionUseCases
import com.topout.kmp.features.session_details.SessionViewModel
import com.topout.kmp.features.sessions.SessionsUseCases
import com.topout.kmp.features.sessions.SessionsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin() {
        config?.invoke(this)
        modules(listOf(commonModule, platformModule))
    }
}

expect val platformModule:Module

// domain and use cases
val domainModule = module {
    factoryOf(::SignInAnonymously)


    factory {
        GetSessions(get())
    }
    factory {
        GetSessionDetails(get())
    }
    factory {
        com.topout.kmp.domain.SaveSession(get())
    }
    factory {
        com.topout.kmp.domain.DeleteSession(get())
    }
    factory{
        SessionsUseCases(get())
    }
    factory {
        SessionUseCases(get(), get(), get())
    }


}

val commonModule = module {
    singleOf(::createJson)
    singleOf(::RemoteFirebaseRepository).bind<FirebaseRepository>()
    singleOf(::RemoteSessionsRepository).bind<SessionsRepository>()

    single { createHttpClient(clientEngine = get(), json = get()) }
    factoryOf(::SessionViewModel)
    factoryOf(::SessionsViewModel)

}
//fun appModules() = listOf(domainModule, databaseModule, platformModule)
//

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