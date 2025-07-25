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
                    // Cancel chip button
                    FilterChip(
                        onClick = { showCancelDialog = true },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cancel",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        },
                        selected = false,
                        modifier = Modifier
                            .height(48.dp)
                            .width(160.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            labelColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 24.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 24.dp
                        )
                    )

                    // Stop & Save chip button
                    FilterChip(
                        onClick = { showStopDialog = true },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Stop & Save",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        },
                        selected = false,
                        modifier = Modifier
                            .height(48.dp)
                            .width(160.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            labelColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 0.dp,
                            bottomStart = 24.dp,
                            bottomEnd = 0.dp
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

