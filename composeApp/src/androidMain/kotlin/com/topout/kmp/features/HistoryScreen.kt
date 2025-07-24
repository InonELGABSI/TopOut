package com.topout.kmp.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.topout.kmp.features.sessions.SessionsState
import com.topout.kmp.features.sessions.SessionsViewModel
import com.topout.kmp.models.Session
import com.topout.kmp.shared_components.rememberTopContentSpacingDp
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: SessionsViewModel = koinViewModel(),
    onSessionClick: (Session) -> Unit
) {
    val topContentSpacing = rememberTopContentSpacingDp()

    LaunchedEffect(Unit) {
        viewModel.fetchSessions()
    }

    val uiState = viewModel.uiState.collectAsState().value

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is SessionsState.Error -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = topContentSpacing,
                        bottom = 8.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        ErrorContent(message = uiState.errorMessage)
                    }
                }
            }
            is SessionsState.Loaded -> {
                if (uiState.sessions.isNullOrEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = topContentSpacing,
                            bottom = 8.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            EmptyStateContent()
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = topContentSpacing,
                            bottom = 8.dp
                        )
                    ) {
                        items(uiState.sessions ?: emptyList()) { session ->
                            SessionItem(
                                session = session,
                                onSessionClick = onSessionClick
                            )
                        }
                    }
                }
            }
            SessionsState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = topContentSpacing,
                        bottom = 8.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        LoadingContent()
                    }
                }
            }
        }
    }
}

@Composable
fun SessionsListContent(
    sessions: List<Session>,
    onSessionClicked: (Session) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(sessions) { session ->
            SessionItem(
                session = session,
                onSessionClick = onSessionClicked
            )
        }
    }
}

@Composable
fun EmptyStateContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📊",
            style = TextStyle(fontSize = 64.sp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Sessions Yet",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start your first climbing session to see your history here",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun ErrorContent(message: String) {
    Text(
        text = message,
        style = TextStyle(
            fontSize = 28.sp,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
fun LoadingContent() {
    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        trackColor = MaterialTheme.colorScheme.secondary
    )
}

private fun createDummyUiState(type: String = "loaded"): SessionsState {
    return when (type) {
        "loading" -> SessionsState.Loading

        "error" -> SessionsState.Error("Failed to load sessions. Please check your connection.")

        "empty" -> SessionsState.Loaded(emptyList())

        "loaded" -> SessionsState.Loaded(
            listOf(
                Session(
                    id = "1",
                    userId = "1",
                    title = "Morning Climb",
                    startTime = null, // Will be handled by ViewModel when real data comes from shared module
                    endTime = null,
                    totalAscent = 120.0,
                    totalDescent = 100.0,
                    maxAltitude = 300.0,
                    minAltitude = 150.0,
                    avgRate = 1.5,
                    alertTriggered = 0L,
                    createdAt = null,
                ),
                Session(
                    id = "2",
                    userId = "2",
                    title = "Evening Session",
                    startTime = null,
                    endTime = null,
                    totalAscent = 80.0,
                    totalDescent = 80.0,
                    maxAltitude = 250.0,
                    minAltitude = 120.0,
                    avgRate = 1.2,
                    alertTriggered = 0L,
                    createdAt = null,
                ),
                Session(
                    id = "3",
                    userId = "3",
                    title = "Weekend Adventure",
                    startTime = null,
                    endTime = null,
                    totalAscent = 200.0,
                    totalDescent = 180.0,
                    maxAltitude = 450.0,
                    minAltitude = 200.0,
                    avgRate = 1.8,
                    alertTriggered = 0L,
                    createdAt = null,
                )
            )
        )

        else -> SessionsState.Loading
    }
}

