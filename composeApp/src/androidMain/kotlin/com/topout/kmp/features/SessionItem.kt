package com.topout.kmp.features

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.topout.kmp.models.Session
import kotlinx.datetime.*

@Composable
fun SessionItem(
    session: Session,
    onSessionClick: (Session) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onSessionClick(session) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title and date
            SessionHeader(
                title = session.title ?: "Unnamed Session",
                startTime = session.startTime
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            SessionStats(session = session)

            // Duration if available
            session.startTime?.let { start ->
                session.endTime?.let { end ->
                    if (end > start) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SessionDuration(startTime = start, endTime = end)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionHeader(
    title: String,
    startTime: Long?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        startTime?.let { time ->
            Text(
                text = formatDate(time),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun SessionStats(session: Session) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Total Ascent
        session.totalAscent?.let { ascent ->
            if (ascent > 0) {
                StatItem(
                    icon = Icons.Default.KeyboardArrowUp,
                    value = "${ascent.toInt()}m",
                    label = "Ascent",
                    iconTint = Color(0xFF4CAF50)
                )
            }
        }

        // Total Descent
        session.totalDescent?.let { descent ->
            if (descent > 0) {
                StatItem(
                    icon = Icons.Default.KeyboardArrowDown,
                    value = "${descent.toInt()}m",
                    label = "Descent",
                    iconTint = Color(0xFFFF5722)
                )
            }
        }

        // Max Altitude
        session.maxAltitude?.let { altitude ->
            if (altitude > 0) {
                StatItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    value = "${altitude.toInt()}m",
                    label = "Max Alt",
                    iconTint = Color(0xFF2196F3)
                )
            }
        }

        // Average Rate
        session.avgRate?.let { rate ->
            if (rate > 0) {
                StatItem(
                    icon = Icons.Default.Schedule,
                    value = String.format("%.1f", rate),
                    label = "Avg Rate",
                    iconTint = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            fontSize = 14.sp
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SessionDuration(
    startTime: Long,
    endTime: Long
) {
    val durationMs = endTime - startTime
    val durationText = formatDuration(durationMs)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Duration",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Duration: $durationText",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.month.name.take(3)} ${dateTime.dayOfMonth.toString().padStart(2, '0')}, ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}
