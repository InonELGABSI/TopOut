package com.topout.kmp.shared_components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

enum class SessionToastType {
    SESSION_STARTED,
    SESSION_START_FAILED,
    SESSION_SAVED,
    SESSION_SAVE_FAILED,
    SESSION_CANCELLED,
    SESSION_CANCEL_FAILED,
    SESSION_PAUSED,
    SESSION_PAUSE_FAILED,
    SESSION_RESUMED,
    SESSION_RESUME_FAILED,
    SESSION_TITLE_EDITED,
    SESSION_TITLE_EDIT_FAILED,
    SESSION_DELETED,
    SESSION_DELETE_FAILED,
    PROFILE_UPDATED,
    PROFILE_UPDATE_FAILED,
    PREFERENCES_UPDATED,
    PREFERENCES_UPDATE_FAILED,
    THRESHOLDS_UPDATED,
    THRESHOLDS_UPDATE_FAILED
}

@Composable
fun SessionToast(
    toastType: SessionToastType?,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(isVisible, toastType) {
        if (isVisible && toastType != null) {
            delay(3000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible && toastType != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier.zIndex(1000f)
    ) {
        if (toastType != null) {
            val (icon, msg, bg) = when (toastType) {
                SessionToastType.SESSION_STARTED -> Triple(
                    Icons.Default.PlayArrow,
                    "Live session started",
                    Color(0xFF4CAF50)
                )
                SessionToastType.SESSION_START_FAILED -> Triple(
                    Icons.Default.PlayArrow,
                    "Live session start failed",
                    Color(0xFFE57373)
                )
                SessionToastType.SESSION_SAVED -> Triple(
                    Icons.Default.CheckCircle,
                    "Live session saved",
                    Color(0xFF2196F3)
                )
                SessionToastType.SESSION_SAVE_FAILED -> Triple(
                    Icons.Default.Save,
                    "Session save failed",
                    Color(0xFFE57373)
                )
                SessionToastType.SESSION_CANCELLED -> Triple(
                    Icons.Default.Cancel,
                    "Live session cancelled",
                    Color(0xFFE57373)
                )
                SessionToastType.SESSION_CANCEL_FAILED -> Triple(
                    Icons.Default.Cancel,
                    "Live session cancel failed",
                    Color(0xFFE57373)
                )
                SessionToastType.SESSION_PAUSED -> Triple(
                    Icons.Default.Pause,
                    "Live session paused",
                    Color(0xFF9E9E9E)
                )
                SessionToastType.SESSION_PAUSE_FAILED -> Triple(
                    Icons.Default.Pause,
                    "Live session pause failed",
                    Color(0xFFE57373)
                )
                SessionToastType.SESSION_RESUMED -> Triple(
                    Icons.Default.PlayArrow,
                    "Live session resumed",
                    Color(0xFF64B5F6)
                )
                SessionToastType.SESSION_RESUME_FAILED -> Triple(
                    Icons.Default.PlayArrow,
                    "Live session resume failed",
                    Color(0xFFE57373)
                )
                SessionToastType.SESSION_TITLE_EDITED -> Triple(
                    Icons.Default.Edit,
                    "Session title edited",
                    Color(0xFFFFC107)
                )
                SessionToastType.SESSION_TITLE_EDIT_FAILED -> Triple(
                    Icons.Default.Edit,
                    "Session title edit failed",
                    Color(0xFFE57373)
                )
                SessionToastType.SESSION_DELETED -> Triple(
                    Icons.Default.Delete,
                    "Session deleted",
                    Color(0xFFE57373)
                )
                SessionToastType.SESSION_DELETE_FAILED -> Triple(
                    Icons.Default.Delete,
                    "Session delete failed",
                    Color(0xFFE57373)
                )
                SessionToastType.PROFILE_UPDATED -> Triple(
                    Icons.Default.Person,
                    "Profile updated",
                    Color(0xFF4CAF50)
                )
                SessionToastType.PROFILE_UPDATE_FAILED -> Triple(
                    Icons.Default.Person,
                    "Profile update failed",
                    Color(0xFFE57373)
                )
                SessionToastType.PREFERENCES_UPDATED -> Triple(
                    Icons.Default.Settings,
                    "Preferences updated",
                    Color(0xFF2196F3)
                )
                SessionToastType.PREFERENCES_UPDATE_FAILED -> Triple(
                    Icons.Default.Settings,
                    "Preferences update failed",
                    Color(0xFFE57373)
                )
                SessionToastType.THRESHOLDS_UPDATED -> Triple(
                    Icons.Default.Warning,
                    "Thresholds updated",
                    Color(0xFFFFC107)
                )
                SessionToastType.THRESHOLDS_UPDATE_FAILED -> Triple(
                    Icons.Default.Warning,
                    "Thresholds update failed",
                    Color(0xFFE57373)
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(6.dp, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = bg),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(msg, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                }
            }
        }
    }
}
