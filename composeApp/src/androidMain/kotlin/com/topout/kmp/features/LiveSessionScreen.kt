package com.topout.kmp.features

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.topout.kmp.map.LiveMap
import com.topout.kmp.features.live_session.LiveSessionState
import com.topout.kmp.features.live_session.LiveSessionViewModel
import com.topout.kmp.models.AlertType
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.shared_components.ConfirmationDialog
import com.topout.kmp.shared_components.DangerToast
import com.topout.kmp.shared_components.MountainAnimation
import com.topout.kmp.shared_components.TopRoundedCard
import com.topout.kmp.shared_components.WaveAnimation
import com.topout.kmp.utils.extensions.latLngOrNull
import org.koin.androidx.compose.koinViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveSessionScreen(
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onNavigateToSessionDetails: (String) -> Unit = {},
    viewModel: LiveSessionViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current

    // Toast for session started
    LaunchedEffect(uiState) {
        if (uiState is LiveSessionState.Loaded) {
            Toast.makeText(context, "Session started!", Toast.LENGTH_SHORT).show()
        }
    }

    // Toast state management
    var showDangerToast by remember { mutableStateOf(false) }
    var currentAlertType by remember { mutableStateOf(AlertType.NONE) }
    var lastToastTimestamp by remember { mutableStateOf(0L) }

    // Monitor danger alerts from track points
    LaunchedEffect(uiState) {
        if (uiState is LiveSessionState.Loaded && uiState.trackPoint.danger) {
            val currentTime = System.currentTimeMillis()

            // Only show toast if no toast is currently active (10 second window)
            if (!showDangerToast && (currentTime - lastToastTimestamp) > 10000) {
                currentAlertType = uiState.trackPoint.alertType
                showDangerToast = true
                lastToastTimestamp = currentTime
            }
        }
    }

    // Handle navigation when session is stopped
    LaunchedEffect(uiState) {
        if (uiState is LiveSessionState.SessionStopped) {
            onNavigateToSessionDetails(uiState.sessionId)
            viewModel.resetToInitialState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        when (uiState) {
            is LiveSessionState.Loading -> StartSessionContent(
                hasLocationPermission = hasLocationPermission,
                onStartClick = { viewModel.onStartClicked() },
                onRequestLocationPermission = onRequestLocationPermission,
                mslHeightState = viewModel.mslHeightState.collectAsState().value,
                onRefreshMSLHeight = { viewModel.refreshMSLHeight() }
            )
            is LiveSessionState.Loaded -> ActiveSessionContent(
                trackPoint = uiState.trackPoint,
                historyTrackPoints = uiState.historyTrackPoints,
                onStopClick = { viewModel.onStopClicked(uiState.trackPoint.sessionId) },
                onCancelClick = { viewModel.onCancelClicked(uiState.trackPoint.sessionId) }
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

        // Danger Toast - positioned at the bottom with proper spacing
        DangerToast(
            alertType = currentAlertType,
            isVisible = showDangerToast,
            onDismiss = { showDangerToast = false },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp) // Above the chip buttons
        )
    }
}

@Composable
fun StartSessionContent(
    hasLocationPermission: Boolean,
    onStartClick: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    mslHeightState: com.topout.kmp.features.live_session.MSLHeightState,
    onRefreshMSLHeight: () -> Unit
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

        Spacer(modifier = Modifier.height(32.dp))

        // Current Mean Sea Level section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top=16.dp,bottom=8.dp,start=16.dp,end=16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Height,
                            contentDescription = "MSL Height",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Current Mean Sea Level",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = onRefreshMSLHeight,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (mslHeightState) {
                        is com.topout.kmp.features.live_session.MSLHeightState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Getting location...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is com.topout.kmp.features.live_session.MSLHeightState.Success -> {
                            val data = mslHeightState.data
                            Text(
                                text = "${data.mslHeight.toInt()}m",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "GPS: ${data.ellipsoidHeight.toInt()}m | Geoid: ${data.geoidHeight.toInt()}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = data.accuracy,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        is com.topout.kmp.features.live_session.MSLHeightState.Error -> {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mslHeightState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Add some space for the wave animation
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Wave animation positioned at the bottom with overflow clipping
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                ) {
                    WaveAnimation(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .offset(y = 20.dp), // Offset to create overflow effect
                        speed = 1f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

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
    historyTrackPoints: List<TrackPoint>,
    onStopClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    // State for stop confirmation dialog
    var showStopDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

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

            // Content area with chip buttons
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Chip buttons row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Cancel chip button with gradient (light left, dark right)
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .width(160.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFE57373), // Light red
                                        Color(0xFFD32F2F)  // Dark red
                                    )
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 24.dp,
                                    bottomStart = 0.dp,
                                    bottomEnd = 24.dp
                                )
                            )
                            .clickable { showCancelDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }

                    // Stop & Save chip button with gradient (dark left, light right)
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .width(160.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF388E3C), // Dark green
                                        Color(0xFF66BB6A)  // Light green
                                    )
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 24.dp,
                                    topEnd = 0.dp,
                                    bottomStart = 24.dp,
                                    bottomEnd = 0.dp
                                )
                            )
                            .clickable { showStopDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Stop & Save",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
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
                trackPoints = historyTrackPoints,
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

    // Cancel session confirmation dialog
    ConfirmationDialog(
        isVisible = showCancelDialog,
        title = "Cancel Session",
        message = "Are you sure you want to cancel this session? All tracking data will be permanently deleted and cannot be recovered.",
        confirmText = "Cancel Session",
        cancelText = "Keep Session",
        icon = Icons.Default.Cancel,
        isDestructive = true,
        onConfirm = onCancelClick,
        onDismiss = { showCancelDialog = false }
    )
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
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Session Error",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Try Again",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
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
        // First Row: Live data title and clock
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Second Row: Location data with icon and title on left, coordinates with space around
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trackPoint.latitude?.let { "%.4f°".format(it) } ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Lat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trackPoint.longitude?.let { "%.4f°".format(it) } ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Lon",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trackPoint.altitude?.let { "%.1fm".format(it) } ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Alt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Third Row: Split into two sections - Speed and Altitude
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left side: Speed section
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Speed",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "%.1f".format(trackPoint.vHorizontal),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "H",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "%.1f".format(trackPoint.vVertical),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "V",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "%.1f".format(trackPoint.avgVertical),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Avg-V",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Right side: Altitude section
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = "Altitude",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Altitude",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${trackPoint.gain.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = "Gain",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${trackPoint.loss.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFFFF5722)
                            )
                            Text(
                                text = "Loss",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${trackPoint.relAltitude.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Rel",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}



