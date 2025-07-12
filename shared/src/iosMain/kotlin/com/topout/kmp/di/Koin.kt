package com.topout.kmp.di

import com.topout.kmp.features.live_session.LiveSessionViewModel
import com.topout.kmp.features.session_details.SessionViewModel
import com.topout.kmp.features.sessions.SessionsViewModel
import com.topout.kmp.features.settings.SettingsViewModel
import org.koin.mp.KoinPlatform


fun SessionViewModel(): SessionViewModel = KoinPlatform.getKoin().get()
fun SessionsViewModel(): SessionsViewModel = KoinPlatform.getKoin().get()
fun SettingsViewModel(): SettingsViewModel = KoinPlatform.getKoin().get()
fun liveSessionViewModel(): LiveSessionViewModel = KoinPlatform.getKoin().get()

fun doInitKoin() = initKoin()
