package com.topout.kmp.shared_components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

@Composable
fun LottieToggleButton(
    isToggled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("Dark Mode Button.json")
    )

    // Calculate frame-based progress values
    val startFrame = 0f
    val endFrame = 40f
    val totalFrames = 320f

    val startProgress = startFrame / totalFrames      // 0.0
    val endProgress = endFrame / totalFrames          // 0.125

    // Animate between the two
    val targetProgress = if (isToggled) startProgress else endProgress

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "lottie_toggle_animation"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable { onToggle(!isToggled) },
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize()
        )
    }
}


@Preview(showBackground = true)
@Composable
fun LottieToggleButtonPreview() {
    var isToggled by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Dark Mode Toggle")

            LottieToggleButton(
                isToggled = isToggled,
                onToggle = { isToggled = it },
                size = 64.dp
            )

            Text(
                text = if (isToggled) "Dark Mode: ON" else "Dark Mode: OFF",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
