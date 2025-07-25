package com.topout.kmp.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.topout.kmp.map.LiveMap
import com.topout.kmp.features.live_session.LiveSessionState
import com.topout.kmp.features.live_session.LiveSessionViewModel
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.shared_components.ConfirmationDialog
import com.topout.kmp.shared_components.MountainAnimation
import com.topout.kmp.shared_components.TopRoundedCard
import com.topout.kmp.shared_components.rememberTopContentSpacingDp
import com.topout.kmp.utils.extensions.latLngOrNull
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveSessionScreen(
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onNavigateToSessionDetails: (String) -> Unit = {},
    viewModel: LiveSessionViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    // Handle navigation when session is stopped
    LaunchedEffect(uiState) {
        if (uiState is LiveSessionState.SessionStopped) {
            onNavigateToSessionDetails(uiState.sessionId)
            // Reset to initial state when entering the screen
            viewModel.resetToInitialState()
        }
    }

    // Remove the screen-level top padding
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is LiveSessionState.Loading -> StartSessionContent(
                hasLocationPermission = hasLocationPermission,
                onStartClick = { viewModel.onStartClicked() },
                onRequestLocationPermission = onRequestLocationPermission
            )
            is LiveSessionState.Loaded -> ActiveSessionContent(
                trackPoint = uiState.trackPoint,
                onStopClick = { viewModel.onStopClicked(uiState.trackPoint.sessionId) }
            )
            is LiveSessionState.Stopping -> StoppingSessionContent()
            is LiveSessionState.SessionStopped -> {
                // Navigation handled in LaunchedEffect
            }
            is LiveSessionState.Error -> ErrorContent(
                errorMessage = uiState.errorMessage,
                onRetryClick = { viewModel.onStartClicked() }
            )
        }
    }
}

@Composable
fun StartSessionContent(
    hasLocationPermission: Boolean,
    onStartClick: () -> Unit,
    onRequestLocationPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mountain Animation - larger and no background
        MountainAnimation(
            modifier = Modifier.size(200.dp),
            animationAsset = "Travel_Mountain.json",
            speed = 1.0f,
            iterations = 1
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Ready to Track",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Start your climbing session to track altitude, speed, and get real-time alerts",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (hasLocationPermission) {
                    onStartClick()
                } else {
                    onRequestLocationPermission()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Start Session",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun ActiveSessionContent(
    trackPoint: TrackPoint,
    onStopClick: () -> Unit
) {
    // State for stop confirmation dialog
    var showStopDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Live Data Card - positioned first so it appears behind the map
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 450.dp), // Start 50dp before map ends to create overlap
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Live data overview card
            TopRoundedCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 2.dp, // Lower elevation to appear behind map
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                LiveDataOverviewCard(trackPoint = trackPoint)
            }

            // Content area with detailed metrics
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Two-column layout for better use of space
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Session Status
                        CompactSessionStatusCard(trackPoint = trackPoint)

                        // Location & GPS Data
                        CompactLocationDataCard(trackPoint = trackPoint)

                        // Accelerometer Data
                        CompactAccelerometerDataCard(trackPoint = trackPoint)
                    }

                    // Right column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Speed & Movement Metrics
                        CompactSpeedMetricsCard(trackPoint = trackPoint)

                        // Altitude & Climbing Data
                        CompactAltitudeMetricsCard(trackPoint = trackPoint)

//                        // Alert Status
//                        CompactAlertStatusCard(trackPoint = trackPoint)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Stop Button
                Button(
                    onClick = { showStopDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stop Session",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Map section in TopRoundedCard - positioned last so it appears above the live data card
        TopRoundedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            cornerRadius = 24.dp,
            elevation = 6.dp // Higher elevation to appear above live data card
        ) {
            LiveMap(
                location = trackPoint.latLngOrNull(),
                modifier = Modifier.fillMaxSize(),
                showLocationFocus = true,
                useTopContentSpacing = true
            )
        }
    }

    // Stop session confirmation dialog
    ConfirmationDialog(
        isVisible = showStopDialog,
        title = "Stop Session",
        message = "Are you sure you want to stop this climbing session? Your progress will be saved.",
        confirmText = "Stop Session",
        cancelText = "Continue",
        icon = Icons.Default.Stop,
        isDestructive = true,
        onConfirm = onStopClick,
        onDismiss = { showStopDialog = false }
    )
}

@Composable
fun LiveDataOverviewCard(trackPoint: TrackPoint) {
    val topContentSpacing = 50.dp // Use a fixed top content spacing

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = topContentSpacing + 20.dp, // Add top content spacing plus card padding
                start = 20.dp,
                end = 20.dp,
                bottom = 20.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title with live indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = "Live",
                    tint = Color.Red,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Live Data",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date(trackPoint.timestamp)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Key metrics in a horizontal layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Location
            LiveDataItem(
                icon = Icons.Default.LocationOn,
                label = "Location",
                value = if (trackPoint.latitude != null && trackPoint.longitude != null)
                    "%.4f°, %.4f°".format(trackPoint.latitude, trackPoint.longitude)
                else "No GPS",
                modifier = Modifier.weight(1f)
            )

            // Speed
            LiveDataItem(
                icon = Icons.Default.Speed,
                label = "Speed",
                value = "%.1f m/s".format(trackPoint.vTotal),
                modifier = Modifier.weight(1f)
            )

            // Altitude
            LiveDataItem(
                icon = Icons.Default.Terrain,
                label = "Altitude",
                value = trackPoint.altitude?.let { "%.1f m".format(it) } ?: "N/A",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun LiveDataItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
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
fun StoppingSessionContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Stopping Session...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

// Compact Cards for two-column layout

@Composable
fun CompactSessionStatusCard(trackPoint: TrackPoint) {
    val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        .format(java.util.Date(trackPoint.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = "Recording",
                    tint = Color.Red,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Session Active",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "ID: ${trackPoint.sessionId.take(8)}...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CompactLocationDataCard(trackPoint: TrackPoint) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Data grid
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactMetricItem(
                    label = "Lat",
                    value = trackPoint.latitude?.let { "%.5f°".format(it) } ?: "N/A",
                    modifier = Modifier.weight(1f)
                )
                CompactMetricItem(
                    label = "Lon",
                    value = trackPoint.longitude?.let { "%.5f°".format(it) } ?: "N/A",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CompactMetricItem(
                    label = "Altitude",
                    value = trackPoint.altitude?.let { "%.1f m".format(it) } ?: "N/A"
                )
            }
        }
    }
}

@Composable
fun CompactAccelerometerDataCard(trackPoint: TrackPoint) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Accelerometer",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Data grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CompactMetricItem(
                    label = "X",
                    value = trackPoint.accelerationX?.let { "%.2f".format(it) } ?: "N/A",
                    modifier = Modifier.weight(1f)
                )
                CompactMetricItem(
                    label = "Y",
                    value = trackPoint.accelerationY?.let { "%.2f".format(it) } ?: "N/A",
                    modifier = Modifier.weight(1f)
                )
                CompactMetricItem(
                    label = "Z",
                    value = trackPoint.accelerationZ?.let { "%.2f".format(it) } ?: "N/A",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CompactSpeedMetricsCard(trackPoint: TrackPoint) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Speed",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Data grid
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactMetricItem(
                    label = "V-Speed",
                    value = "%.1f m/min".format(trackPoint.vVertical),
                    modifier = Modifier.weight(1f)
                )
                CompactMetricItem(
                    label = "H-Speed",
                    value = "%.1f m/s".format(trackPoint.vHorizontal),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactMetricItem(
                    label = "Total",
                    value = "%.1f m/s".format(trackPoint.vTotal),
                    modifier = Modifier.weight(1f)
                )
                CompactMetricItem(
                    label = "Avg-V",
                    value = "%.1f m/min".format(trackPoint.avgVertical),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CompactAltitudeMetricsCard(trackPoint: TrackPoint) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Terrain,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Altitude",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Data grid
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactMetricItem(
                    label = "Gain",
                    value = "%.1f m".format(trackPoint.gain),
                    textColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                CompactMetricItem(
                    label = "Loss",
                    value = "%.1f m".format(trackPoint.loss),
                    textColor = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CompactMetricItem(
                    label = "Rel. Alt",
                    value = "%.1f m".format(trackPoint.relAltitude)
                )
            }
        }
    }
}

@Composable
fun CompactAlertStatusCard(trackPoint: TrackPoint) {
    val alertColor = if (trackPoint.danger) Color(0xFFFF5722) else Color(0xFF4CAF50)
    val alertText = if (trackPoint.danger) "⚠️ ${trackPoint.alertType.name}" else "✅ OK"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (trackPoint.danger)
                Color(0xFFFF5722).copy(alpha = 0.1f)
            else
                Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (trackPoint.danger) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = "Alert Status",
                tint = alertColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = alertText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = alertColor
                )
            )
        }
    }
}

@Composable
fun CompactMetricItem(
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
            style = MaterialTheme.typography.bodyMedium.copy(
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
fun SessionStatusCard(trackPoint: TrackPoint) {
    val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        .format(java.util.Date(trackPoint.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = "Recording",
                tint = Color.Red,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Session Active",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Session ID: ${trackPoint.sessionId.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Timestamp: $timestamp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LocationDataCard(trackPoint: TrackPoint) {
    MetricCard(
        title = "Location & GPS",
        icon = Icons.Default.LocationOn
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(
                label = "Latitude",
                value = trackPoint.latitude?.let { "%.6f°".format(it) } ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            MetricItem(
                label = "Longitude",
                value = trackPoint.longitude?.let { "%.6f°".format(it) } ?: "N/A",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        MetricItem(
            label = "GPS Altitude",
            value = trackPoint.altitude?.let { "%.1f m".format(it) } ?: "N/A"
        )
    }
}

@Composable
fun AccelerometerDataCard(trackPoint: TrackPoint) {
    MetricCard(
        title = "Accelerometer Data",
        icon = Icons.Default.Vibration // Using a suitable icon from Material Icons
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(
                label = "X-Axis",
                value = trackPoint.accelerationX?.let { "%.2f".format(it) } ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            MetricItem(
                label = "Y-Axis",
                value = trackPoint.accelerationY?.let { "%.2f".format(it) } ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            MetricItem(
                label = "Z-Axis",
                value = trackPoint.accelerationZ?.let { "%.2f".format(it) } ?: "N/A",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SpeedMetricsCard(trackPoint: TrackPoint) {
    MetricCard(
        title = "Speed & Movement",
        icon = Icons.Default.Speed
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(
                label = "Vertical Speed",
                value = "%.1f m/min".format(trackPoint.vVertical),
                modifier = Modifier.weight(1f)
            )
            MetricItem(
                label = "Horizontal Speed",
                value = "%.1f m/s".format(trackPoint.vHorizontal),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(
                label = "Total Speed",
                value = "%.1f m/s".format(trackPoint.vTotal),
                modifier = Modifier.weight(1f)
            )
            MetricItem(
                label = "Avg Vertical",
                value = "%.1f m/min".format(trackPoint.avgVertical),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AltitudeMetricsCard(trackPoint: TrackPoint) {
    MetricCard(
        title = "Altitude & Climbing",
        icon = Icons.Default.Terrain
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(
                label = "Total Gain",
                value = "%.1f m".format(trackPoint.gain),
                textColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            MetricItem(
                label = "Total Loss",
                value = "%.1f m".format(trackPoint.loss),
                textColor = Color(0xFFFF5722),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        MetricItem(
            label = "Relative Altitude",
            value = "%.1f m".format(trackPoint.relAltitude)
        )
    }
}

@Composable
fun AlertStatusCard(trackPoint: TrackPoint) {
    val alertColor = if (trackPoint.danger) Color(0xFFFF5722) else Color(0xFF4CAF50)
    val alertText = if (trackPoint.danger) "⚠️ ${trackPoint.alertType.name}" else "✅ All Good"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (trackPoint.danger)
                Color(0xFFFF5722).copy(alpha = 0.1f)
            else
                Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (trackPoint.danger) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = "Alert Status",
                tint = alertColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = alertText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = alertColor
                )
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
fun MetricItem(
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
            style = MaterialTheme.typography.titleLarge.copy(
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
fun ErrorContent(
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
            text = "Session Error",
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
