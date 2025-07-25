package com.topout.kmp.shared_components

import android.annotation.SuppressLint
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.common.VicoTheme
import com.patrykandpatrick.vico.compose.common.getDefaultColors

/**
 * Custom Vico theme for TopOut app using Material Theme colors
 */
@SuppressLint("RestrictedApi")
@Composable
fun rememberTopOutVicoTheme(
    // Use Material Theme colors for consistency
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    tertiaryColor: Color = MaterialTheme.colorScheme.tertiary,
    errorColor: Color = MaterialTheme.colorScheme.error,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    onSurfaceColor: Color = MaterialTheme.colorScheme.onSurface,
    outlineColor: Color = MaterialTheme.colorScheme.outline,
    showGrid: Boolean = false, // Add option to hide grid
): VicoTheme {

    val climbingColors = listOf(
        primaryColor,                           // Primary theme color
        secondaryColor,                         // Secondary theme color
        tertiaryColor,                          // Tertiary theme color
        MaterialTheme.colorScheme.primaryContainer,   // Container variants
        MaterialTheme.colorScheme.secondaryContainer,
        errorColor                              // Error/warning color
    )

    // Get default colors outside of remember block
    val defaultColors = getDefaultColors()

    return remember(
        primaryColor,
        secondaryColor,
        tertiaryColor,
        errorColor,
        surfaceColor,
        onSurfaceColor,
        outlineColor,
        showGrid,
        defaultColors
    ) {
        VicoTheme(
            candlestickCartesianLayerColors = VicoTheme.CandlestickCartesianLayerColors.fromDefaultColors(
                defaultColors
            ),
            columnCartesianLayerColors = climbingColors,
            lineCartesianLayerColors = climbingColors,
            lineColor = if (showGrid) outlineColor.copy(alpha = 0.6f) else Color.Transparent,
            textColor = onSurfaceColor,
        )
    }
}

/**
 * Alternative dark theme for TopOut charts - now uses Material Theme automatically
 */
@Composable
fun rememberTopOutDarkVicoTheme(showGrid: Boolean = false): VicoTheme {
    // Material Theme automatically handles dark mode, so we just use the same function
    return rememberTopOutVicoTheme(showGrid = showGrid)
}

/**
 * High contrast theme using Material Theme colors with enhanced contrast
 */
@Composable
fun rememberTopOutHighContrastVicoTheme(showGrid: Boolean = false): VicoTheme {
    return rememberTopOutVicoTheme(
        primaryColor = MaterialTheme.colorScheme.onSurface,      // High contrast
        secondaryColor = MaterialTheme.colorScheme.primary,      // Strong primary
        tertiaryColor = MaterialTheme.colorScheme.error,         // Error for visibility
        errorColor = MaterialTheme.colorScheme.error,
        surfaceColor = MaterialTheme.colorScheme.surface,
        onSurfaceColor = MaterialTheme.colorScheme.onSurface,
        outlineColor = MaterialTheme.colorScheme.onSurface,      // Strong outline
        showGrid = showGrid
    )
}
