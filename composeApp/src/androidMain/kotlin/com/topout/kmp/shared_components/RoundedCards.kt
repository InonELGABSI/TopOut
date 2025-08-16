package com.topout.kmp.shared_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun TopRoundedCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val baseColor = containerColor
    val lightColor = Color(
        red = (baseColor.red + 0.15f).coerceAtMost(1f),
        green = (baseColor.green + 0.15f).coerceAtMost(1f),
        blue = (baseColor.blue + 0.15f).coerceAtMost(1f),
        alpha = 1f
    )
    val darkColor = Color(
        red = (baseColor.red - 0.15f).coerceAtLeast(0f),
        green = (baseColor.green - 0.15f).coerceAtLeast(0f),
        blue = (baseColor.blue - 0.15f).coerceAtLeast(0f),
        alpha = 1f
    )

    val gradientColors = listOf(
        darkColor,
        baseColor,
        lightColor
    )

    val shape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = cornerRadius,
        bottomEnd = cornerRadius
    )

    Box(
        modifier = modifier
            // Multiple shadow layers for depth
            .shadow(
                elevation = elevation + 12.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .shadow(
                elevation = elevation + 8.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .shadow(
                elevation = elevation + 4.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = 1500f
                ),
                shape = shape
            )
    ) {
        Box(
        ) {
            content()
        }
    }
}

/**
 * Bottom rounded card - only top corners are rounded for stacking
 */
@Composable
fun BottomRoundedCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val baseColor = containerColor
    val lightColor = Color(
        red = (baseColor.red + 0.15f).coerceAtMost(1f),
        green = (baseColor.green + 0.15f).coerceAtMost(1f),
        blue = (baseColor.blue + 0.15f).coerceAtMost(1f),
        alpha = 1f
    )
    val darkColor = Color(
        red = (baseColor.red - 0.15f).coerceAtLeast(0f),
        green = (baseColor.green - 0.15f).coerceAtLeast(0f),
        blue = (baseColor.blue - 0.15f).coerceAtLeast(0f),
        alpha = 1f
    )

    val gradientColors = listOf(
        lightColor,
        baseColor,
        darkColor
    )

    val shape = RoundedCornerShape(
        topStart = cornerRadius,
        topEnd = cornerRadius,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    Box(
        modifier = modifier
            // Multiple shadow layers for depth
            .shadow(
                elevation = elevation + 12.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .shadow(
                elevation = elevation + 8.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .shadow(
                elevation = elevation + 4.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = 1500f
                ),
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier.padding(4.dp)
        ) {
            content()
        }
    }
}