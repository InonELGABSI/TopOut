package com.topout.kmp.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.topout.kmp.features.session_details.SessionDetailsState
import com.topout.kmp.features.session_details.SessionDetailsViewModel
import com.topout.kmp.models.SessionDetails
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.map.LiveMap
import com.topout.kmp.utils.extensions.latLngOrNull
import com.topout.kmp.shared_components.ConfirmationDialog
import com.topout.kmp.shared_components.LoadingAnimation
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailsScreen(
    sessionId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: SessionDetailsViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    when (uiState) {
        is SessionDetailsState.Loading -> SessionLoadingContent()
        is SessionDetailsState.Loaded -> SessionDetailsContent(
            sessionDetails = uiState.sessionDetails
        )
        is SessionDetailsState.Error -> SessionErrorContent(
            errorMessage = uiState.errorMessage,
            onRetryClick = { viewModel.loadSession(sessionId) }
        )
    }

    // Delete confirmation dialog
    ConfirmationDialog(
        isVisible = showDeleteDialog,
        title = "Delete Session",
        message = "Are you sure you want to delete this climbing session? This action cannot be undone.",
        confirmText = "Delete",
        cancelText = "Cancel",
        icon = Icons.Default.Delete,
        isDestructive = true,
        onConfirm = {
            viewModel.deleteSession(sessionId)
            onNavigateBack() // Navigate back after deletion
        },
        onDismiss = { showDeleteDialog = false }
    )
}

@Composable
fun SessionLoadingContent() {
    LoadingAnimation(
        text = "Loading session details..."
    )
}

@Composable
fun SessionDetailsContent(
    sessionDetails: SessionDetails
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Session Overview Card
        item {
            SessionOverviewCard(sessionDetails = sessionDetails)
        }

        // Map with track points
        item {
            if (sessionDetails.points.isNotEmpty()) {
                SessionMapCard(trackPoints = sessionDetails.points)
            }
        }

        // Session Statistics
        item {
            SessionStatisticsCard(sessionDetails = sessionDetails)
        }

        // Track Points List
        item {
            TrackPointsCard(trackPoints = sessionDetails.points)
        }
    }
}

@Composable
fun SessionOverviewCard(sessionDetails: SessionDetails) {
    val session = sessionDetails.session
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session Overview",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Session ID",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = session.id.take(8) + "...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = session.startTime?.let { dateFormat.format(Date(it)) } ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Start Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = session.startTime?.let { timeFormat.format(Date(it)) } ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = calculateSessionDuration(sessionDetails),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SessionMapCard(trackPoints: List<TrackPoint>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Route Map",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Show map with the first point that has location data
            if (trackPoints.any { it.latLngOrNull() != null }) {
                LiveMap(
                    location = null, // Not needed in route mode
                    trackPoints = trackPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    showTrackFocus = true
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No location data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SessionStatisticsCard(sessionDetails: SessionDetails) {
    val points = sessionDetails.points

    if (points.isEmpty()) return

    val maxAltitude = points.mapNotNull { it.altitude }.maxOrNull() ?: 0.0
    val minAltitude = points.mapNotNull { it.altitude }.minOrNull() ?: 0.0
    val totalGain = points.lastOrNull()?.gain ?: 0.0
    val totalLoss = points.lastOrNull()?.loss ?: 0.0
    val maxSpeed = points.maxOfOrNull { it.vTotal } ?: 0.0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Session Statistics",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // First row of stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    label = "Max Altitude",
                    value = "%.1f m".format(maxAltitude),
                    modifier = Modifier.weight(1f)
                )
                StatisticItem(
                    label = "Min Altitude",
                    value = "%.1f m".format(minAltitude),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Second row of stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    label = "Total Gain",
                    value = "%.1f m".format(totalGain),
                    textColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatisticItem(
                    label = "Total Loss",
                    value = "%.1f m".format(totalLoss),
                    textColor = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Third row of stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    label = "Max Speed",
                    value = "%.1f m/s".format(maxSpeed),
                    modifier = Modifier.weight(1f)
                )
                StatisticItem(
                    label = "Track Points",
                    value = points.size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TrackPointsCard(trackPoints: List<TrackPoint>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Track Points (${trackPoints.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (trackPoints.isEmpty()) {
                Text(
                    text = "No track points recorded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Show first few track points as preview
                trackPoints.take(3).forEach { point ->
                    TrackPointItem(trackPoint = point)
                    if (point != trackPoints.take(3).last()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                if (trackPoints.size > 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "... and ${trackPoints.size - 3} more points",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun TrackPointItem(trackPoint: TrackPoint) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = timeFormat.format(Date(trackPoint.timestamp)),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "Alt: ${trackPoint.altitude?.let { "%.1f m".format(it) } ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Speed: %.1f m/s".format(trackPoint.vTotal),
                    style = MaterialTheme.typography.bodySmall
                )
                if (trackPoint.danger) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = trackPoint.alertType.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF5722)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SessionErrorContent(
    errorMessage: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Failed to Load Session",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

private fun calculateSessionDuration(sessionDetails: SessionDetails): String {
    val points = sessionDetails.points
    if (points.isEmpty()) return "N/A"

    val startTime = points.firstOrNull()?.timestamp ?: 0L
    val endTime = points.lastOrNull()?.timestamp ?: 0L
    val durationMs = endTime - startTime

    return formatDuration(durationMs)
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = (durationMs / (1000 * 60 * 60))

    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

