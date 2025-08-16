package com.topout.kmp.shared_components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

@Composable
fun LottieToggleButton(
    isToggled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("Dark Mode Button.json")
    )
    val startFrame = 0f
    val endFrame = 40f
    val totalFrames = 320f

    val startProgress = startFrame / totalFrames
    val endProgress = endFrame / totalFrames

    val targetProgress = if (isToggled) startProgress else endProgress

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "lottie_toggle_animation"
    )

    val aspectRatio = remember(composition) {
        val w = composition?.bounds?.width()?.toFloat() ?: 1f
        val h = composition?.bounds?.height()?.toFloat() ?: 1f
        if (h > 0f) w / h else 1f
    }

    Box(
        modifier = modifier
            .height(height)
            .aspectRatio(aspectRatio)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle(!isToggled) },
        contentAlignment = Alignment.Center
    ) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


