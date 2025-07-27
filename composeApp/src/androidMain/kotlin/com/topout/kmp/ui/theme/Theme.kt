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
    CLASSIC_RED,       // Red/Gray theme (was CRIMSON_PEAK)
    OCEAN_BLUE,        // Blue/Navy theme (was AZURE_HEIGHTS)
    FOREST_GREEN,      // Green/Brown theme (was EMERALD_TRAIL)
    STORM_GRAY,        // Blue/Steel theme (was STEEL_SUMMIT)
    SUNSET_ORANGE      // Orange/Purple theme (was SUNSET_RIDGE)
}

object TopOutTheme {
    fun getColorScheme(palette: ThemePalette, isDark: Boolean = false): ColorScheme {
        return when (palette) {
            ThemePalette.CLASSIC_RED -> {
                if (isDark) darkColorScheme(
                    primary = ClassicRedDarkColors.primary,
                    onPrimary = ClassicRedDarkColors.onPrimary,
                    primaryContainer = ClassicRedDarkColors.primaryContainer,
                    onPrimaryContainer = ClassicRedDarkColors.onPrimaryContainer,
                    secondary = ClassicRedDarkColors.secondary,
                    onSecondary = ClassicRedDarkColors.onSecondary,
                    secondaryContainer = ClassicRedDarkColors.secondaryContainer,
                    onSecondaryContainer = ClassicRedDarkColors.onSecondaryContainer,
                    background = ClassicRedDarkColors.background,
                    onBackground = ClassicRedDarkColors.onBackground,
                    surface = ClassicRedDarkColors.surface,
                    onSurface = ClassicRedDarkColors.onSurface,
                    surfaceVariant = ClassicRedDarkColors.surfaceVariant,
                    onSurfaceVariant = ClassicRedDarkColors.onSurfaceVariant,
                    error = ClassicRedDarkColors.error,
                    onError = ClassicRedDarkColors.onError,
                    errorContainer = ClassicRedDarkColors.errorContainer,
                    onErrorContainer = ClassicRedDarkColors.onErrorContainer
                )
                else lightColorScheme(
                    primary = ClassicRedLightColors.primary,
                    onPrimary = ClassicRedLightColors.onPrimary,
                    primaryContainer = ClassicRedLightColors.primaryContainer,
                    onPrimaryContainer = ClassicRedLightColors.onPrimaryContainer,
                    secondary = ClassicRedLightColors.secondary,
                    onSecondary = ClassicRedLightColors.onSecondary,
                    secondaryContainer = ClassicRedLightColors.secondaryContainer,
                    onSecondaryContainer = ClassicRedLightColors.onSecondaryContainer,
                    background = ClassicRedLightColors.background,
                    onBackground = ClassicRedLightColors.onBackground,
                    surface = ClassicRedLightColors.surface,
                    onSurface = ClassicRedLightColors.onSurface,
                    surfaceVariant = ClassicRedLightColors.surfaceVariant,
                    onSurfaceVariant = ClassicRedLightColors.onSurfaceVariant,
                    error = ClassicRedLightColors.error,
                    onError = ClassicRedLightColors.onError,
                    errorContainer = ClassicRedLightColors.errorContainer,
                    onErrorContainer = ClassicRedLightColors.onErrorContainer
                )
            }
            ThemePalette.OCEAN_BLUE -> {
                if (isDark) darkColorScheme(
                    primary = OceanBlueDarkColors.primary,
                    onPrimary = OceanBlueDarkColors.onPrimary,
                    primaryContainer = OceanBlueDarkColors.primaryContainer,
                    onPrimaryContainer = OceanBlueDarkColors.onPrimaryContainer,
                    secondary = OceanBlueDarkColors.secondary,
                    onSecondary = OceanBlueDarkColors.onSecondary,
                    secondaryContainer = OceanBlueDarkColors.secondaryContainer,
                    onSecondaryContainer = OceanBlueDarkColors.onSecondaryContainer,
                    background = OceanBlueDarkColors.background,
                    onBackground = OceanBlueDarkColors.onBackground,
                    surface = OceanBlueDarkColors.surface,
                    onSurface = OceanBlueDarkColors.onSurface,
                    surfaceVariant = OceanBlueDarkColors.surfaceVariant,
                    onSurfaceVariant = OceanBlueDarkColors.onSurfaceVariant,
                    error = OceanBlueDarkColors.error,
                    onError = OceanBlueDarkColors.onError,
                    errorContainer = OceanBlueDarkColors.errorContainer,
                    onErrorContainer = OceanBlueDarkColors.onErrorContainer
                )
                else lightColorScheme(
                    primary = OceanBlueLightColors.primary,
                    onPrimary = OceanBlueLightColors.onPrimary,
                    primaryContainer = OceanBlueLightColors.primaryContainer,
                    onPrimaryContainer = OceanBlueLightColors.onPrimaryContainer,
                    secondary = OceanBlueLightColors.secondary,
                    onSecondary = OceanBlueLightColors.onSecondary,
                    secondaryContainer = OceanBlueLightColors.secondaryContainer,
                    onSecondaryContainer = OceanBlueLightColors.onSecondaryContainer,
                    background = OceanBlueLightColors.background,
                    onBackground = OceanBlueLightColors.onBackground,
                    surface = OceanBlueLightColors.surface,
                    onSurface = OceanBlueLightColors.onSurface,
                    surfaceVariant = OceanBlueLightColors.surfaceVariant,
                    onSurfaceVariant = OceanBlueLightColors.onSurfaceVariant,
                    error = OceanBlueLightColors.error,
                    onError = OceanBlueLightColors.onError,
                    errorContainer = OceanBlueLightColors.errorContainer,
                    onErrorContainer = OceanBlueLightColors.onErrorContainer
                )
            }
            ThemePalette.FOREST_GREEN -> {
                if (isDark) darkColorScheme(
                    primary = ForestGreenDarkColors.primary,
                    onPrimary = ForestGreenDarkColors.onPrimary,
                    primaryContainer = ForestGreenDarkColors.primaryContainer,
                    onPrimaryContainer = ForestGreenDarkColors.onPrimaryContainer,
                    secondary = ForestGreenDarkColors.secondary,
                    onSecondary = ForestGreenDarkColors.onSecondary,
                    secondaryContainer = ForestGreenDarkColors.secondaryContainer,
                    onSecondaryContainer = ForestGreenDarkColors.onSecondaryContainer,
                    background = ForestGreenDarkColors.background,
                    onBackground = ForestGreenDarkColors.onBackground,
                    surface = ForestGreenDarkColors.surface,
                    onSurface = ForestGreenDarkColors.onSurface,
                    surfaceVariant = ForestGreenDarkColors.surfaceVariant,
                    onSurfaceVariant = ForestGreenDarkColors.onSurfaceVariant,
                    error = ForestGreenDarkColors.error,
                    onError = ForestGreenDarkColors.onError,
                    errorContainer = ForestGreenDarkColors.errorContainer,
                    onErrorContainer = ForestGreenDarkColors.onErrorContainer
                )
                else lightColorScheme(
                    primary = ForestGreenLightColors.primary,
                    onPrimary = ForestGreenLightColors.onPrimary,
                    primaryContainer = ForestGreenLightColors.primaryContainer,
                    onPrimaryContainer = ForestGreenLightColors.onPrimaryContainer,
                    secondary = ForestGreenLightColors.secondary,
                    onSecondary = ForestGreenLightColors.onSecondary,
                    secondaryContainer = ForestGreenLightColors.secondaryContainer,
                    onSecondaryContainer = ForestGreenLightColors.onSecondaryContainer,
                    background = ForestGreenLightColors.background,
                    onBackground = ForestGreenLightColors.onBackground,
                    surface = ForestGreenLightColors.surface,
                    onSurface = ForestGreenLightColors.onSurface,
                    surfaceVariant = ForestGreenLightColors.surfaceVariant,
                    onSurfaceVariant = ForestGreenLightColors.onSurfaceVariant,
                    error = ForestGreenLightColors.error,
                    onError = ForestGreenLightColors.onError,
                    errorContainer = ForestGreenLightColors.errorContainer,
                    onErrorContainer = ForestGreenLightColors.onErrorContainer
                )
            }
            ThemePalette.STORM_GRAY -> {
                if (isDark) darkColorScheme(
                    primary = StormGrayDarkColors.primary,
                    onPrimary = StormGrayDarkColors.onPrimary,
                    primaryContainer = StormGrayDarkColors.primaryContainer,
                    onPrimaryContainer = StormGrayDarkColors.onPrimaryContainer,
                    secondary = StormGrayDarkColors.secondary,
                    onSecondary = StormGrayDarkColors.onSecondary,
                    secondaryContainer = StormGrayDarkColors.secondaryContainer,
                    onSecondaryContainer = StormGrayDarkColors.onSecondaryContainer,
                    background = StormGrayDarkColors.background,
                    onBackground = StormGrayDarkColors.onBackground,
                    surface = StormGrayDarkColors.surface,
                    onSurface = StormGrayDarkColors.onSurface,
                    surfaceVariant = StormGrayDarkColors.surfaceVariant,
                    onSurfaceVariant = StormGrayDarkColors.onSurfaceVariant,
                    error = StormGrayDarkColors.error,
                    onError = StormGrayDarkColors.onError,
                    errorContainer = StormGrayDarkColors.errorContainer,
                    onErrorContainer = StormGrayDarkColors.onErrorContainer
                )
                else lightColorScheme(
                    primary = StormGrayLightColors.primary,
                    onPrimary = StormGrayLightColors.onPrimary,
                    primaryContainer = StormGrayLightColors.primaryContainer,
                    onPrimaryContainer = StormGrayLightColors.onPrimaryContainer,
                    secondary = StormGrayLightColors.secondary,
                    onSecondary = StormGrayLightColors.onSecondary,
                    secondaryContainer = StormGrayLightColors.secondaryContainer,
                    onSecondaryContainer = StormGrayLightColors.onSecondaryContainer,
                    background = StormGrayLightColors.background,
                    onBackground = StormGrayLightColors.onBackground,
                    surface = StormGrayLightColors.surface,
                    onSurface = StormGrayLightColors.onSurface,
                    surfaceVariant = StormGrayLightColors.surfaceVariant,
                    onSurfaceVariant = StormGrayLightColors.onSurfaceVariant,
                    error = StormGrayLightColors.error,
                    onError = StormGrayLightColors.onError,
                    errorContainer = StormGrayLightColors.errorContainer,
                    onErrorContainer = StormGrayLightColors.onErrorContainer
                )
            }
            ThemePalette.SUNSET_ORANGE -> {
                if (isDark) darkColorScheme(
                    primary = SunsetOrangeDarkColors.primary,
                    onPrimary = SunsetOrangeDarkColors.onPrimary,
                    primaryContainer = SunsetOrangeDarkColors.primaryContainer,
                    onPrimaryContainer = SunsetOrangeDarkColors.onPrimaryContainer,
                    secondary = SunsetOrangeDarkColors.secondary,
                    onSecondary = SunsetOrangeDarkColors.onSecondary,
                    secondaryContainer = SunsetOrangeDarkColors.secondaryContainer,
                    onSecondaryContainer = SunsetOrangeDarkColors.onSecondaryContainer,
                    background = SunsetOrangeDarkColors.background,
                    onBackground = SunsetOrangeDarkColors.onBackground,
                    surface = SunsetOrangeDarkColors.surface,
                    onSurface = SunsetOrangeDarkColors.onSurface,
                    surfaceVariant = SunsetOrangeDarkColors.surfaceVariant,
                    onSurfaceVariant = SunsetOrangeDarkColors.onSurfaceVariant,
                    error = SunsetOrangeDarkColors.error,
                    onError = SunsetOrangeDarkColors.onError,
                    errorContainer = SunsetOrangeDarkColors.errorContainer,
                    onErrorContainer = SunsetOrangeDarkColors.onErrorContainer
                )
                else lightColorScheme(
                    primary = SunsetOrangeLightColors.primary,
                    onPrimary = SunsetOrangeLightColors.onPrimary,
                    primaryContainer = SunsetOrangeLightColors.primaryContainer,
                    onPrimaryContainer = SunsetOrangeLightColors.onPrimaryContainer,
                    secondary = SunsetOrangeLightColors.secondary,
                    onSecondary = SunsetOrangeLightColors.onSecondary,
                    secondaryContainer = SunsetOrangeLightColors.secondaryContainer,
                    onSecondaryContainer = SunsetOrangeLightColors.onSecondaryContainer,
                    background = SunsetOrangeLightColors.background,
                    onBackground = SunsetOrangeLightColors.onBackground,
                    surface = SunsetOrangeLightColors.surface,
                    onSurface = SunsetOrangeLightColors.onSurface,
                    surfaceVariant = SunsetOrangeLightColors.surfaceVariant,
                    onSurfaceVariant = SunsetOrangeLightColors.onSurfaceVariant,
                    error = SunsetOrangeLightColors.error,
                    onError = SunsetOrangeLightColors.onError,
                    errorContainer = SunsetOrangeLightColors.errorContainer,
                    onErrorContainer = SunsetOrangeLightColors.onErrorContainer
                )
            }
        }
    }
}

// Current theme palette provider
val LocalThemePalette = staticCompositionLocalOf { ThemePalette.CLASSIC_RED }

@Composable
fun TopOutAppTheme(
    palette: ThemePalette = ThemePalette.CLASSIC_RED,
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

