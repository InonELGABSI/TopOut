package com.topout.kmp.shared_components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

@Composable
fun MountainAnimation(
    modifier: Modifier = Modifier,
    animationAsset: String = "Travel_Mountain.json",
    speed: Float = 1.0f,
    animationSize: Dp = 200.dp,
    iterations: Int = 1
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(animationAsset))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        speed = speed
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(animationSize)
    )
}
