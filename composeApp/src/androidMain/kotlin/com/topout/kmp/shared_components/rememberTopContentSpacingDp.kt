package com.topout.kmp.shared_components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

// Utility function to get the total top spacing
@Composable
fun rememberTopContentSpacingDp(): Dp {
    // App bar height + additional fade
    val chipBarHeight = 56.dp         // Or actual ChipControlBar height
    val gradientHeight = 80.dp       // Should be same as TopFadeGradient height

    // Add system status bar height for real edge-to-edge
    val density = LocalDensity.current
    val statusBarHeightPx = WindowInsets.statusBars.getTop(density)
    val statusBarHeightDp = with(density) { statusBarHeightPx.toDp() }

    // Option 1: Just appbar + gradient (if NOT drawing under system bars)
    // return chipBarHeight + (gradientHeight - chipBarHeight)

    // Option 2: If using edge-to-edge (drawing under system bars):
    return statusBarHeightDp + chipBarHeight + (gradientHeight - chipBarHeight)
}
