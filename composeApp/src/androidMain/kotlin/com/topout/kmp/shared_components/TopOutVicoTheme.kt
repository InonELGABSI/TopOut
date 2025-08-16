package com.topout.kmp.shared_components

import android.annotation.SuppressLint
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.common.VicoTheme
import com.patrykandpatrick.vico.compose.common.getDefaultColors

@SuppressLint("RestrictedApi")
@Composable
fun rememberTopOutVicoTheme(
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
        primaryColor,
        secondaryColor,
        tertiaryColor,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        errorColor
    )

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

@Composable
fun rememberTopOutDarkVicoTheme(showGrid: Boolean = false): VicoTheme {
    return rememberTopOutVicoTheme(showGrid = showGrid)
}

@Composable
fun rememberTopOutHighContrastVicoTheme(showGrid: Boolean = false): VicoTheme {
    return rememberTopOutVicoTheme(
        primaryColor = MaterialTheme.colorScheme.onSurface,
        secondaryColor = MaterialTheme.colorScheme.primary,
        tertiaryColor = MaterialTheme.colorScheme.error,
        errorColor = MaterialTheme.colorScheme.error,
        surfaceColor = MaterialTheme.colorScheme.surface,
        onSurfaceColor = MaterialTheme.colorScheme.onSurface,
        outlineColor = MaterialTheme.colorScheme.onSurface,
        showGrid = showGrid
    )
}
