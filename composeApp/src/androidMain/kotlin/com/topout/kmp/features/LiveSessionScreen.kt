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
import com.topout.kmp.utils.extensions.latLngOrNull
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveSessionScreen(
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    viewModel: LiveSessionViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Live Session",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                is LiveSessionState.Error -> ErrorContent(
                    errorMessage = uiState.errorMessage,
                    onRetryClick = { viewModel.onStartClicked() }
                )
            }
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
        // Session Icon
        Card(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(60.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Session",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        LiveMap(
            location = trackPoint.latLngOrNull(),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        // Session Status Card
        SessionStatusCard(trackPoint = trackPoint)

        // Location & GPS Data
        LocationDataCard(trackPoint = trackPoint)

        // Speed & Movement Metrics
        SpeedMetricsCard(trackPoint = trackPoint)

        // Altitude & Climbing Data
        AltitudeMetricsCard(trackPoint = trackPoint)

        // Alert Status
        AlertStatusCard(trackPoint = trackPoint)

        Spacer(modifier = Modifier.weight(1f))

        // Stop Button
        Button(
            onClick = onStopClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Stop Session",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun SessionStatusCard(trackPoint: TrackPoint) {
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
            Text(
                text = "Session ID: ${trackPoint.sessionId.take(8)}...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
