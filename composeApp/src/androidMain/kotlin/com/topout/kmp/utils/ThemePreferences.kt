package com.topout.kmp.utils

import android.content.Context
import android.content.SharedPreferences
import com.topout.kmp.ui.theme.ThemePalette
import androidx.core.content.edit

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "theme_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_THEME_PALETTE = "theme_palette"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
    }

    fun saveThemePalette(palette: ThemePalette) {
        prefs.edit() {
            putString(KEY_THEME_PALETTE, palette.name)
        }
    }

    fun getThemePalette(): ThemePalette {
        val paletteName = prefs.getString(KEY_THEME_PALETTE, ThemePalette.CLASSIC_RED.name)
        return try {
            ThemePalette.valueOf(paletteName ?: ThemePalette.CLASSIC_RED.name)
        } catch (e: IllegalArgumentException) {
            ThemePalette.CLASSIC_RED
        }
    }

    fun saveDarkMode(isDarkMode: Boolean) {
        prefs.edit() {
            putBoolean(KEY_IS_DARK_MODE, isDarkMode)
        }
    }

    fun getDarkMode(defaultValue: Boolean): Boolean {
        return prefs.getBoolean(KEY_IS_DARK_MODE, defaultValue)
    }

    fun hasDarkModePreference(): Boolean {
        return prefs.contains(KEY_IS_DARK_MODE)
    }
}
