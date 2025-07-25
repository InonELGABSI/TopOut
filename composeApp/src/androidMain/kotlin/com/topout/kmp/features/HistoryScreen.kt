package com.topout.kmp.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.topout.kmp.shared_components.TopRoundedCard
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

    Column(modifier = Modifier.fillMaxSize()) {
        // Top rounded card with controls
        TopRoundedCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            elevation = 2.dp,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            HistoryControlsSection(
                modifier = Modifier.padding(
                    top = topContentSpacing + 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
            )
        }

        // Content area
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                is SessionsState.Error -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 8.dp,
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
                                top = 8.dp,
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
                                top = 8.dp,
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
                            top = 8.dp,
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
}

@Composable
fun HistoryControlsSection(
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedSortOption by remember { mutableStateOf("Date (Newest)") }

    val sortOptions = listOf(
        "Date (Newest)",
        "Date (Oldest)",
        "Duration (Longest)",
        "Duration (Shortest)",
        "Ascent (Highest)",
        "Ascent (Lowest)"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort button on the left
            Box {
                OutlinedButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(48.dp), // Match the search input height
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedSortOption,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedSortOption = option
                                showSortMenu = false
                                // TODO: Apply sorting logic
                            }
                        )
                    }
                }
            }

            // Search input on the right
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp), // Control overall height for smaller padding
                placeholder = {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(18.dp) // Smaller icon
                    )
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(
                            onClick = { searchText = "" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                modifier = Modifier.size(16.dp) // Smaller clear icon
                            )
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 18.sp // Reduce line height for tighter text
                ),
                singleLine = true,
                shape = RoundedCornerShape(24.dp), // Smaller radius for more compact look
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
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

