package com.topout.kmp.features

import androidx.compose.runtime.Composable
import com.topout.kmp.features.home.HomeViewModel
import com.topout.kmp.features.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {

}