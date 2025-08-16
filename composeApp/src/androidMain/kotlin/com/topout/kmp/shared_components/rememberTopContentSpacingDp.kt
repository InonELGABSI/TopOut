package com.topout.kmp.shared_components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp


@Composable
fun rememberTopContentSpacingDp(): Dp {
    val chipBarHeight = 56.dp
    val gradientHeight = 80.dp

    val density = LocalDensity.current
    val statusBarHeightPx = WindowInsets.statusBars.getTop(density)
    val statusBarHeightDp = with(density) { statusBarHeightPx.toDp() }

    return statusBarHeightDp + chipBarHeight + (gradientHeight - chipBarHeight)
}
