package com.topout.kmp.shared_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

@Composable
fun LoadingAnimation(
    text: String,
    modifier: Modifier = Modifier,
    animationAsset: String = "outdoor_boots_animation.json",
    speed: Float = 1.5f,
    animationSize: Dp = 350.dp,
    containerWidth: Dp = 350.dp,
    containerHeight: Dp = 200.dp,
    spacing: Dp = 16.dp
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(animationAsset))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = speed
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(containerWidth)
                    .height(containerHeight),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(animationSize)
                )
            }

            Spacer(modifier = Modifier.height(spacing))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
