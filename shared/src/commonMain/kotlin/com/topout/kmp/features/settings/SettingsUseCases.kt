package com.topout.kmp.features.settings

import com.topout.kmp.domain.GetSettings
import com.topout.kmp.domain.UpdateUser

data class SettingsUseCases (
    val getSettings: GetSettings,
    val updateUser: UpdateUser
)

