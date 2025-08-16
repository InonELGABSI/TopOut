package com.topout.kmp.shared_components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.topout.kmp.models.AlertType
import kotlinx.coroutines.delay

@Composable
fun DangerToast(
    alertType: AlertType,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(10000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(),
        modifier = modifier.zIndex(1000f)
    ) {
        val (icon, title, message, backgroundColor) = when (alertType) {
            AlertType.RAPID_ASCENT -> {
                Quadruple(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    "Rapid Ascent Alert",
                    "You are ascending too quickly. Consider slowing down for safety.",
                    Color(0xFFFF9800) // Orange
                )
            }
            AlertType.RAPID_DESCENT -> {
                Quadruple(
                    Icons.AutoMirrored.Filled.TrendingDown,
                    "Rapid Descent Alert",
                    "You are descending too quickly. Please reduce your speed.",
                    Color(0xFFE53935) // Red
                )
            }
            AlertType.RELATIVE_HEIGHT_EXCEEDED -> {
                Quadruple(
                    Icons.Default.Height,
                    "Height Limit Alert",
                    "You've exceeded your relative height threshold from start point.",
                    Color(0xFF9C27B0) // Purple
                )
            }
            AlertType.TOTAL_HEIGHT_EXCEEDED -> {
                Quadruple(
                    Icons.Default.Landscape,
                    "Altitude Limit Alert",
                    "You've reached your maximum altitude threshold.",
                    Color(0xFF2196F3) // Blue
                )
            }
            AlertType.NONE -> {
                Quadruple(
                    Icons.Default.Warning,
                    "Safety Alert",
                    "Please be cautious",
                    Color(0xFFFF9800)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
