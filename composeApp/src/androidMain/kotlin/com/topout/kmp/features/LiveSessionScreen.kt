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
import androidx.compose.ui.draw.alpha
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.topout.kmp.shared_components.SessionToast
import com.topout.kmp.shared_components.SessionToastType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveSessionScreen(
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onNavigateToSessionDetails: (String) -> Unit = {},
    viewModel: LiveSessionViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    var lastUiState by remember { mutableStateOf<LiveSessionState?>(null) }
    var toastType by remember { mutableStateOf<SessionToastType?>(null) }
    var showSessionToast by remember { mutableStateOf(false) }

    var showDangerToast by remember { mutableStateOf(false) }
    var currentAlertType by remember { mutableStateOf(AlertType.NONE) }

    LaunchedEffect(uiState) {
        when {
            uiState is LiveSessionState.Loaded && lastUiState is LiveSessionState.Paused -> {
                toastType = SessionToastType.SESSION_RESUMED
                showSessionToast = true
            }
            uiState is LiveSessionState.Paused && lastUiState is LiveSessionState.Loaded -> {
                toastType = SessionToastType.SESSION_PAUSED
                showSessionToast = true
            }
            uiState is LiveSessionState.Loaded && lastUiState !is LiveSessionState.Loaded -> {
                toastType = SessionToastType.SESSION_STARTED
                showSessionToast = true
            }
            uiState is LiveSessionState.SessionStopped && lastUiState !is LiveSessionState.SessionStopped -> {
                toastType = SessionToastType.SESSION_SAVED
                showSessionToast = true
            }
        }
        lastUiState = uiState
    }

    LaunchedEffect(uiState) {
        if (uiState is LiveSessionState.SessionStopped) {
            kotlinx.coroutines.delay(1500)
            onNavigateToSessionDetails(uiState.sessionId)
            viewModel.resetToInitialState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        when(uiState) {
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
                isPaused = false,
                onPauseClick = {
                    val success = viewModel.onPauseClicked()
                    if (!success) {
                        toastType = SessionToastType.SESSION_PAUSE_FAILED
                        showSessionToast = true
                    }
                    success
                },
                onResumeClick = {
                    val success = viewModel.onResumeClicked()
                    if (!success) {
                        toastType = SessionToastType.SESSION_RESUME_FAILED
                        showSessionToast = true
                    }
                    success
                },
                onStopClick = {
                    val success = viewModel.onStopClicked(uiState.trackPoint.sessionId)
                    if (!success) {
                        toastType = SessionToastType.SESSION_SAVE_FAILED
                        showSessionToast = true
                    }
                    success
                },
                onCancelClick = {
                    val success = viewModel.onCancelClicked(uiState.trackPoint.sessionId)
                    if (success) {
                        toastType = SessionToastType.SESSION_CANCELLED
                        showSessionToast = true
                    } else {
                        toastType = SessionToastType.SESSION_CANCEL_FAILED
                        showSessionToast = true
                    }
                    success
                }
            )
            is LiveSessionState.Paused -> ActiveSessionContent(
                trackPoint = uiState.trackPoint,
                historyTrackPoints = uiState.historyTrackPoints,
                isPaused = true,
                onPauseClick = {
                    val success = viewModel.onPauseClicked()
                    if (!success) {
                        toastType = SessionToastType.SESSION_PAUSE_FAILED
                        showSessionToast = true
                    }
                    success
                },
                onResumeClick = {
                    val success = viewModel.onResumeClicked()
                    if (!success) {
                        toastType = SessionToastType.SESSION_RESUME_FAILED
                        showSessionToast = true
                    }
                    success
                },
                onStopClick = {
                    val success = viewModel.onStopClicked(uiState.trackPoint.sessionId)
                    if (!success) {
                        toastType = SessionToastType.SESSION_SAVE_FAILED
                        showSessionToast = true
                    }
                    success
                },
                onCancelClick = {
                    val success = viewModel.onCancelClicked(uiState.trackPoint.sessionId)
                    if (success) {
                        toastType = SessionToastType.SESSION_CANCELLED
                        showSessionToast = true
                    } else {
                        toastType = SessionToastType.SESSION_CANCEL_FAILED
                        showSessionToast = true
                    }
                    success
                }
            )

            is LiveSessionState.Stopping -> StoppingSessionContent()
            is LiveSessionState.SessionStopped -> Unit
            is LiveSessionState.Error -> ErrorContent(
                errorMessage = uiState.errorMessage,
                onRetryClick = { viewModel.onStartClicked() }
            )
        }

        // Danger Toast
        DangerToast(
            alertType = currentAlertType,
            isVisible = showDangerToast,
            onDismiss = { showDangerToast = false },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        )

        // SessionToast for action feedback (including pause/resume/stop/cancel failures)
        SessionToast(
            toastType = toastType,
            isVisible = showSessionToast && toastType != null,
            onDismiss = {
                showSessionToast = false
                toastType = null
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(bottom = 12.dp)
        )


    }
}

@Composable
fun StartSessionContent(
    hasLocationPermission: Boolean,
    onStartClick: () -> Boolean,
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
                    val success = onStartClick()
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
    isPaused: Boolean,
    onPauseClick: () -> Boolean,
    onResumeClick: () -> Boolean,
    onStopClick: () -> Boolean,
    onCancelClick: () -> Boolean
) {
    // State for stop confirmation dialog
    var showStopDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // State for handling action feedback
    var toastType by remember { mutableStateOf<SessionToastType?>(null) }
    var showToast by remember { mutableStateOf(false) }

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
                        .padding( bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel chip button
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFE57373),
                                        Color(0xFFD32F2F)
                                    )
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 24.dp,
                                    bottomStart = 0.dp,
                                    bottomEnd = 24.dp
                                )
                            )
                            .alpha(if (isPaused) 0.45f else 1f)
                            .clickable(enabled = !isPaused) { showCancelDialog = true }
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

                    // Pause/Resume
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (!isPaused)
                                        listOf(Color(0xFF757575), Color(0xFF9E9E9E))   // Pause (אפור)
                                    else
                                        listOf(Color(0xFF1976D2), Color(0xFF64B5F6))   // Resume (כחול)
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable {
                                val success = if (!isPaused) onPauseClick() else onResumeClick()
                                if (!success) {
                                    toastType = if (!isPaused) SessionToastType.SESSION_PAUSE_FAILED else SessionToastType.SESSION_RESUME_FAILED
                                    showToast = true
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (!isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (!isPaused) "Pause" else "Resume",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    // Stop & Save chip button
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF388E3C),
                                        Color(0xFF66BB6A)
                                    )
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 24.dp,
                                    topEnd = 0.dp,
                                    bottomStart = 24.dp,
                                    bottomEnd = 0.dp
                                )
                            )
                            .alpha(if (isPaused) 0.45f else 1f)
                            .clickable(enabled = !isPaused) { showStopDialog = true }
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

        // SessionToast for action feedback (including pause/resume/stop/cancel failures)
        SessionToast(
            toastType = toastType,
            isVisible = showToast && toastType != null,
            onDismiss = {
                showToast = false
                toastType = null
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(bottom = 12.dp)
        )
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
        onConfirm = {
            val success = onStopClick()
            showStopDialog = false
            if (!success) {
                toastType = SessionToastType.SESSION_SAVE_FAILED
                showToast = true
            }
        },
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
        onConfirm = {
            val success = onCancelClick()
            showCancelDialog = false
            if (!success) {
                toastType = SessionToastType.SESSION_CANCEL_FAILED
                showToast = true
            }
        },
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
                        text = "MSE",
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
                                text = "%.1f".format(trackPoint.avgHorizontal),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Avg-H",
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
