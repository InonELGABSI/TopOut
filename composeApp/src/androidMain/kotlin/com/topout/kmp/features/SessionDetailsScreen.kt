package com.topout.kmp.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.topout.kmp.features.session_details.SessionDetailsState
import com.topout.kmp.features.session_details.SessionDetailsViewModel
import com.topout.kmp.models.SessionDetails
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.map.LiveMap
import com.topout.kmp.utils.extensions.latLngOrNull
import com.topout.kmp.shared_components.*
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

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

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is SessionDetailsState.Loading -> SessionLoadingContent()
            is SessionDetailsState.Loaded -> NewSessionDetailsContent(
                sessionDetails = uiState.sessionDetails,
                onDeleteClick = { showDeleteDialog = true }
            )
            is SessionDetailsState.Error -> SessionErrorContent(
                errorMessage = uiState.errorMessage,
                onRetryClick = { viewModel.loadSession(sessionId) }
            )
        }
    }

    // Delete confirmation dialog
    ConfirmationDialog(
        isVisible = showDeleteDialog,
        title = "Delete Session",
        message = "Are you sure you want to delete this climbing session? This action cannot be undone.",
        confirmText = "Delete",
        cancelText = "Cancel",
        icon = Icons.Outlined.DeleteOutline,
        isDestructive = true,
        onConfirm = {
            viewModel.deleteSession(sessionId)
            onNavigateBack() // Navigate back after deletion
        },
        onDismiss = { showDeleteDialog = false }
    )
}

@Composable
fun NewSessionDetailsContent(
    sessionDetails: SessionDetails,
    onDeleteClick: () -> Unit
) {
    // Make the entire content scrollable
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Map section
        item {
            TopRoundedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                cornerRadius = 24.dp
            ) {
                // Map takes full card space
                if (sessionDetails.points.isNotEmpty()) {
                    LiveMap(
                        location = null, // Not needed in route mode
                        trackPoints = sessionDetails.points,
                        modifier = Modifier.fillMaxSize(),
                        showTrackFocus = true,
                        useTopContentSpacing = true
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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

        // Session info section
        item {
            SessionInfoSection(
                sessionDetails = sessionDetails,
                onDeleteClick = onDeleteClick
            )
        }

        // Session name with statistics and track points wrapped together
        item {
            BottomRoundedCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 6.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Title
                    Text(
                        text = "Climbing Session", // You can make this dynamic based on session data
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                    )

                    // Horizontal divider under title
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Statistics (without background)
                    SessionStatisticsCard(sessionDetails = sessionDetails)

                    // Horizontal divider under statistics
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    // Add spacing before the chart section
                    Spacer(modifier = Modifier.height(16.dp))

                    // Time-Height Chart
                    if (sessionDetails.points.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Altitude over Time",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TimeHeightChart(
                                samples = prepareChartData(sessionDetails.points),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }

                    // Track Points (without background)
                    TrackPointsCardContent(trackPoints = sessionDetails.points)
                }
            }
        }
    }
}

@Composable
fun SessionInfoSection(
    sessionDetails: SessionDetails,
    onDeleteClick: () -> Unit
) {
    val session = sessionDetails.session
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date + Start Time
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = session.startTime?.let { dateFormat.format(Date(it)) } ?: "N/A",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = session.startTime?.let { timeFormat.format(Date(it)) } ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Duration
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = calculateSessionDuration(sessionDetails),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Duration",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Delete Button
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete Session"
                )
            }
            Text(
                text = "Delete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun SessionLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingAnimation(
            text = "Loading session details..."
        )
    }
}

@Composable
fun SessionErrorContent(
    errorMessage: String,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
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

@Composable
fun SessionStatisticsCard(sessionDetails: SessionDetails) {
    val points = sessionDetails.points

    if (points.isEmpty()) return

    val maxAltitude = points.mapNotNull { it.altitude }.maxOrNull() ?: 0.0
    val minAltitude = points.mapNotNull { it.altitude }.minOrNull() ?: 0.0
    val totalGain = points.lastOrNull()?.gain ?: 0.0
    val totalLoss = points.lastOrNull()?.loss ?: 0.0
    val maxSpeed = points.maxOfOrNull { it.vTotal } ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // First row - 3 items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItemWithIcon(
                icon = Icons.Default.KeyboardArrowUp,
                label = "Max Altitude",
                value = "%.1f m".format(maxAltitude)
            )
            StatisticItemWithIcon(
                icon = Icons.Default.KeyboardArrowDown,
                label = "Min Altitude",
                value = "%.1f m".format(minAltitude)
            )
            StatisticItemWithIcon(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = "Total Gain",
                value = "%.1f m".format(totalGain),
                textColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
            )
        }

        // Second row - 2 items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItemWithIcon(
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                label = "Total Loss",
                value = "%.1f m".format(totalLoss),
                textColor = androidx.compose.ui.graphics.Color(0xFFFF5722)
            )
            StatisticItemWithIcon(
                icon = Icons.Default.Speed,
                label = "Max Speed",
                value = "%.1f m/s".format(maxSpeed)
            )
        }
    }
}

@Composable
fun StatisticItemWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TrackPointsCardContent(trackPoints: List<TrackPoint>) {
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

        // Removed the detailed track points table - only showing the count above
    }
}

@Composable
fun StatisticItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
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

/**
 * Prepares chart data from track points with a maximum of 50 points.
 * Calculates time from session start and aggregates points if necessary.
 */
private fun prepareChartData(trackPoints: List<TrackPoint>): List<Pair<Float, Float>> {
    if (trackPoints.isEmpty()) return emptyList()

    // Filter points that have altitude or relAltitude data
    val pointsWithAltitude = trackPoints.filter { it.altitude != null || it.relAltitude != 0.0 }
    if (pointsWithAltitude.isEmpty()) return emptyList()

    val sessionStartTime = pointsWithAltitude.first().timestamp

    // If we have 50 or fewer points, use all of them
    if (pointsWithAltitude.size <= 50) {
        return pointsWithAltitude.map { point ->
            val timeFromStart = ((point.timestamp - sessionStartTime) / 1000f) // Convert to seconds
            // Use altitude if available, otherwise use relAltitude
            val altitude = (point.altitude ?: point.relAltitude).toFloat()
            Pair(timeFromStart, altitude)
        }
    }

    // If we have more than 50 points, aggregate them
    val step = pointsWithAltitude.size / 50
    val aggregatedPoints = mutableListOf<Pair<Float, Float>>()

    for (i in 0 until 50) {
        val startIndex = i * step
        val endIndex = if (i == 49) pointsWithAltitude.size else (i + 1) * step

        // Take points in this range and average their values
        val pointsInRange = pointsWithAltitude.subList(startIndex, min(endIndex, pointsWithAltitude.size))

        if (pointsInRange.isNotEmpty()) {
            // Use the middle point's timestamp for time calculation
            val middlePoint = pointsInRange[pointsInRange.size / 2]
            val timeFromStart = ((middlePoint.timestamp - sessionStartTime) / 1000f)

            // Average the altitude values in this range (use altitude if available, otherwise relAltitude)
            val averageAltitude = pointsInRange.map { point ->
                point.altitude ?: point.relAltitude
            }.average().toFloat()

            aggregatedPoints.add(Pair(timeFromStart, averageAltitude))
        }
    }

    return aggregatedPoints
}
