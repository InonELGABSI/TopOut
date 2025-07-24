package com.topout.kmp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemePalette {
    CLASSIC_CLIMBING,
    MOUNTAIN_BLUE,
    FOREST_ADVENTURE,
    DARK_MOUNTAIN,
    SUNSET_PEAK
}

object TopOutTheme {
    fun getColorScheme(palette: ThemePalette, isDark: Boolean = false): ColorScheme {
        return when (palette) {
            ThemePalette.CLASSIC_CLIMBING -> {
                if (isDark) getDarkColorScheme(ClassicClimbingColors)
                else getLightColorScheme(ClassicClimbingColors)
            }
            ThemePalette.MOUNTAIN_BLUE -> {
                if (isDark) getDarkColorScheme(MountainBlueColors)
                else getLightColorScheme(MountainBlueColors)
            }
            ThemePalette.FOREST_ADVENTURE -> {
                if (isDark) getDarkColorScheme(ForestAdventureColors)
                else getLightColorScheme(ForestAdventureColors)
            }
            ThemePalette.DARK_MOUNTAIN -> {
                getDarkColorScheme(DarkMountainColors)
            }
            ThemePalette.SUNSET_PEAK -> {
                if (isDark) getDarkColorScheme(SunsetPeakColors)
                else getLightColorScheme(SunsetPeakColors)
            }
        }
    }
}

private fun getLightColorScheme(colors: Any): ColorScheme {
    return when (colors) {
        is ClassicClimbingColors -> lightColorScheme(
            primary = ClassicClimbingColors.primary,
            onPrimary = ClassicClimbingColors.onPrimary,
            primaryContainer = ClassicClimbingColors.primaryContainer,
            onPrimaryContainer = ClassicClimbingColors.onPrimaryContainer,
            secondary = ClassicClimbingColors.secondary,
            onSecondary = ClassicClimbingColors.onSecondary,
            secondaryContainer = ClassicClimbingColors.secondaryContainer,
            onSecondaryContainer = ClassicClimbingColors.onSecondaryContainer,
            background = ClassicClimbingColors.background,
            onBackground = ClassicClimbingColors.onBackground,
            surface = ClassicClimbingColors.surface,
            onSurface = ClassicClimbingColors.onSurface,
            surfaceVariant = ClassicClimbingColors.surfaceVariant,
            onSurfaceVariant = ClassicClimbingColors.onSurfaceVariant,
            error = ClassicClimbingColors.error,
            onError = ClassicClimbingColors.onError,
            errorContainer = ClassicClimbingColors.errorContainer,
            onErrorContainer = ClassicClimbingColors.onErrorContainer
        )
        is MountainBlueColors -> lightColorScheme(
            primary = MountainBlueColors.primary,
            onPrimary = MountainBlueColors.onPrimary,
            primaryContainer = MountainBlueColors.primaryContainer,
            onPrimaryContainer = MountainBlueColors.onPrimaryContainer,
            secondary = MountainBlueColors.secondary,
            onSecondary = MountainBlueColors.onSecondary,
            secondaryContainer = MountainBlueColors.secondaryContainer,
            onSecondaryContainer = MountainBlueColors.onSecondaryContainer,
            background = MountainBlueColors.background,
            onBackground = MountainBlueColors.onBackground,
            surface = MountainBlueColors.surface,
            onSurface = MountainBlueColors.onSurface,
            surfaceVariant = MountainBlueColors.surfaceVariant,
            onSurfaceVariant = MountainBlueColors.onSurfaceVariant,
            error = MountainBlueColors.error,
            onError = MountainBlueColors.onError,
            errorContainer = MountainBlueColors.errorContainer,
            onErrorContainer = MountainBlueColors.onErrorContainer
        )
        is ForestAdventureColors -> lightColorScheme(
            primary = ForestAdventureColors.primary,
            onPrimary = ForestAdventureColors.onPrimary,
            primaryContainer = ForestAdventureColors.primaryContainer,
            onPrimaryContainer = ForestAdventureColors.onPrimaryContainer,
            secondary = ForestAdventureColors.secondary,
            onSecondary = ForestAdventureColors.onSecondary,
            secondaryContainer = ForestAdventureColors.secondaryContainer,
            onSecondaryContainer = ForestAdventureColors.onSecondaryContainer,
            background = ForestAdventureColors.background,
            onBackground = ForestAdventureColors.onBackground,
            surface = ForestAdventureColors.surface,
            onSurface = ForestAdventureColors.onSurface,
            surfaceVariant = ForestAdventureColors.surfaceVariant,
            onSurfaceVariant = ForestAdventureColors.onSurfaceVariant,
            error = ForestAdventureColors.error,
            onError = ForestAdventureColors.onError,
            errorContainer = ForestAdventureColors.errorContainer,
            onErrorContainer = ForestAdventureColors.onErrorContainer
        )
        is SunsetPeakColors -> lightColorScheme(
            primary = SunsetPeakColors.primary,
            onPrimary = SunsetPeakColors.onPrimary,
            primaryContainer = SunsetPeakColors.primaryContainer,
            onPrimaryContainer = SunsetPeakColors.onPrimaryContainer,
            secondary = SunsetPeakColors.secondary,
            onSecondary = SunsetPeakColors.onSecondary,
            secondaryContainer = SunsetPeakColors.secondaryContainer,
            onSecondaryContainer = SunsetPeakColors.onSecondaryContainer,
            background = SunsetPeakColors.background,
            onBackground = SunsetPeakColors.onBackground,
            surface = SunsetPeakColors.surface,
            onSurface = SunsetPeakColors.onSurface,
            surfaceVariant = SunsetPeakColors.surfaceVariant,
            onSurfaceVariant = SunsetPeakColors.onSurfaceVariant,
            error = SunsetPeakColors.error,
            onError = SunsetPeakColors.onError,
            errorContainer = SunsetPeakColors.errorContainer,
            onErrorContainer = SunsetPeakColors.onErrorContainer
        )
        else -> lightColorScheme()
    }
}

private fun getDarkColorScheme(colors: Any): ColorScheme {
    return when (colors) {
        is DarkMountainColors -> darkColorScheme(
            primary = DarkMountainColors.primary,
            onPrimary = DarkMountainColors.onPrimary,
            primaryContainer = DarkMountainColors.primaryContainer,
            onPrimaryContainer = DarkMountainColors.onPrimaryContainer,
            secondary = DarkMountainColors.secondary,
            onSecondary = DarkMountainColors.onSecondary,
            secondaryContainer = DarkMountainColors.secondaryContainer,
            onSecondaryContainer = DarkMountainColors.onSecondaryContainer,
            background = DarkMountainColors.background,
            onBackground = DarkMountainColors.onBackground,
            surface = DarkMountainColors.surface,
            onSurface = DarkMountainColors.onSurface,
            surfaceVariant = DarkMountainColors.surfaceVariant,
            onSurfaceVariant = DarkMountainColors.onSurfaceVariant,
            error = DarkMountainColors.error,
            onError = DarkMountainColors.onError,
            errorContainer = DarkMountainColors.errorContainer,
            onErrorContainer = DarkMountainColors.onErrorContainer
        )
        else -> {
            // For other palettes in dark mode, create adapted dark versions
            darkColorScheme()
        }
    }
}

// Current theme palette provider
val LocalThemePalette = staticCompositionLocalOf { ThemePalette.CLASSIC_CLIMBING }

@Composable
fun TopOutAppTheme(
    palette: ThemePalette = ThemePalette.CLASSIC_CLIMBING,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = TopOutTheme.getColorScheme(palette, darkTheme)

    CompositionLocalProvider(LocalThemePalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Extension function to get current theme palette anywhere in the app
@Composable
fun getCurrentThemePalette(): ThemePalette = LocalThemePalette.current

