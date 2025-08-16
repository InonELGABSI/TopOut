package com.topout.kmp.shared_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipControlBar(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    isTransparent: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(TopAppBarDefaults.windowInsets)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = if (isTransparent) {
                        val baseColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(
                                    red = (baseColor.red + 0.1f).coerceAtMost(1f),
                                    green = (baseColor.green + 0.1f).coerceAtMost(1f),
                                    blue = (baseColor.blue + 0.1f).coerceAtMost(1f),
                                    alpha = baseColor.alpha
                                ),
                                baseColor,
                                Color(
                                    red = (baseColor.red - 0.1f).coerceAtLeast(0f),
                                    green = (baseColor.green - 0.1f).coerceAtLeast(0f),
                                    blue = (baseColor.blue - 0.1f).coerceAtLeast(0f),
                                    alpha = baseColor.alpha
                                )
                            )
                        )
                    } else {
                        val baseColor = MaterialTheme.colorScheme.primaryContainer
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(
                                    red = (baseColor.red + 0.1f).coerceAtMost(1f),
                                    green = (baseColor.green + 0.1f).coerceAtMost(1f),
                                    blue = (baseColor.blue + 0.1f).coerceAtMost(1f),
                                    alpha = 1f
                                ),
                                baseColor,
                                Color(
                                    red = (baseColor.red - 0.1f).coerceAtLeast(0f),
                                    green = (baseColor.green - 0.1f).coerceAtLeast(0f),
                                    blue = (baseColor.blue - 0.1f).coerceAtLeast(0f),
                                    alpha = 1f
                                )
                            )
                        )
                    }
                )
        ) {
            if (showBackButton) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isTransparent)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isTransparent) {
                        val baseColor = MaterialTheme.colorScheme.primary
                        Color(
                            red = (baseColor.red * 0.3f).coerceAtLeast(0f),
                            green = (baseColor.green * 0.3f).coerceAtLeast(0f),
                            blue = (baseColor.blue * 0.3f).coerceAtLeast(0f),
                            alpha = 1f
                        )
                    } else {
                        val baseColor = MaterialTheme.colorScheme.primaryContainer
                        Color(
                            red = (baseColor.red * 0.3f).coerceAtLeast(0f),
                            green = (baseColor.green * 0.3f).coerceAtLeast(0f),
                            blue = (baseColor.blue * 0.3f).coerceAtLeast(0f),
                            alpha = 1f
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = if (showBackButton) 32.dp else 0.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChipControlBarPreview() {
    MaterialTheme {
        ChipControlBar(
            title = "Sample Title",
            showBackButton = true,
            onBackClick = {},
        )
    }
}
