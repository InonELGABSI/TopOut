package com.topout.kmp.di

import com.topout.kmp.features.session_details.SessionViewModel
import com.topout.kmp.features.sessions.SessionsViewModel
import org.koin.mp.KoinPlatform


fun SessionViewModel(): SessionViewModel = KoinPlatform.getKoin().get()
fun SessionsViewModel(): SessionsViewModel = KoinPlatform.getKoin().get()

fun doInitKoin() = initKoin()
