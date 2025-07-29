package com.topout.kmp.di

import com.topout.kmp.features.live_session.LiveSessionViewModel
import com.topout.kmp.features.session_details.SessionDetailsViewModel
import com.topout.kmp.features.sessions.SessionsViewModel
import com.topout.kmp.features.settings.SettingsViewModel
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier
import org.koin.mp.KoinPlatform

lateinit var sharedKoin: Koin

fun doInitKoin() {
    initKoin()
    sharedKoin = KoinPlatform.getKoin()
}

fun SessionDetailsViewModel(): SessionDetailsViewModel = KoinPlatform.getKoin().get()
fun SessionsViewModel(): SessionsViewModel = KoinPlatform.getKoin().get()
fun SettingsViewModel(): SettingsViewModel = KoinPlatform.getKoin().get()
fun liveSessionViewModel(): LiveSessionViewModel = KoinPlatform.getKoin().get()

@OptIn(BetaInteropApi::class)
fun Koin.get(objCClass: ObjCClass): Any? {
    val kClazz = getOriginalKotlinClass(objCClass) ?: return null
    return get(kClazz, null, null)
}

@OptIn(BetaInteropApi::class)
fun Koin.get(
    objCClass: ObjCClass,
    qualifier: Qualifier?,
    parameter: Any
): Any? {
    val kClazz = getOriginalKotlinClass(objCClass) ?: return null
    return get(kClazz, qualifier) { parametersOf(parameter) }
}