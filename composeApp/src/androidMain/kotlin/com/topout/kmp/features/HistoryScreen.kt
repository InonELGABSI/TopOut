package com.topout.kmp.features

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.topout.kmp.features.sessions.SessionsState
import com.topout.kmp.features.sessions.SessionsViewModel
import com.topout.kmp.models.Session
import com.topout.kmp.shared_components.rememberTopContentSpacingDp
import com.topout.kmp.shared_components.TopRoundedCard
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import com.topout.kmp.shared_components.BottomRoundedCard
import com.topout.kmp.shared_components.FullRoundedCard
import com.topout.kmp.shared_components.MiddleRoundedCard
import kotlin.math.roundToInt

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
                        StackedSessionCards(
                            sessions = uiState.sessions ?: emptyList(),
                            onSessionClick = onSessionClick
                        )
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
fun StackedSessionCards(
    sessions: List<Session>,
    onSessionClick: (Session) -> Unit,
    overlap: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val palette = listOf(
        MaterialTheme.colorScheme.surfaceContainerHigh,
        MaterialTheme.colorScheme.surfaceContainer
    )
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val headerHeight = 200.dp

    // State for header visibility (keep your animation logic as before)
    var headerOffset by remember { mutableFloatStateOf(0f) }
    var lastScrollValue by remember { mutableIntStateOf(0) }
    val headerHeightPx = with(density) { headerHeight.toPx() }
    val animatedHeaderOffset by animateFloatAsState(
        targetValue = headerOffset,
        animationSpec = tween(300, easing = EaseInOutCubic),
        label = "headerOffset"
    )

    LaunchedEffect(scrollState.value) {
        val currentScroll = scrollState.value
        val scrollDelta = currentScroll - lastScrollValue
        when {
            scrollDelta > 0 -> headerOffset = -headerHeightPx
            scrollDelta < 0 -> headerOffset = 0f
        }
        lastScrollValue = currentScroll
    }

    Box(modifier = modifier) {
        // The stacking layout, scrollable
        Layout(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxWidth(),
            content = {
                // 1. Add the spacer for the header
                Spacer(Modifier.height(headerHeight))
                // 2. Add a spacer after the header for overlap (fine-tune as needed)
                Spacer(Modifier.height(overlap + 24.dp))
                // 3. All session cards (stacked)
                sessions.asReversed().forEachIndexed { revIndex, session ->
                    val color = palette[revIndex % palette.size]
                    val elevation = 6.dp + (revIndex * 2).dp
                    val cardContent: @Composable () -> Unit = {
                        SessionCardContent(
                            session = session,
                            onSessionClick = onSessionClick,
                            topContentSpacing = 0.dp,
                            isFirstItem = revIndex == sessions.lastIndex
                        )
                    }
                    Box(
                        modifier = Modifier
                            .zIndex(revIndex.toFloat())
                            .padding(horizontal = 16.dp)
                    ) {
                        when {
                            sessions.size == 1 -> FullRoundedCard(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = elevation,
                                containerColor = color,
                                content = cardContent
                            )
                            revIndex == sessions.lastIndex -> TopRoundedCard(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = elevation,
                                containerColor = color,
                                content = cardContent
                            )
                            else -> BottomRoundedCard(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = elevation,
                                containerColor = color,
                                content = cardContent
                            )
                        }
                    }
                }
            }
        ) { measurables, constraints ->
            val overlapPx = overlap.roundToPx()
            var y = 0
            // 1. Header spacer
            val headerPlaceable = measurables[0].measure(constraints)
            val overlapSpacer = measurables[1].measure(constraints)
            y += headerPlaceable.height
            y += overlapSpacer.height

            val cardPlacements = mutableListOf<Pair<Int, Placeable>>()
            // 2. Cards
            for (i in 2 until measurables.size) {
                val placeable = measurables[i].measure(constraints)
                cardPlacements.add(y to placeable)
                // Each card overlaps the previous
                y += placeable.height - overlapPx
            }
            val layoutHeight = if (cardPlacements.isEmpty()) y else y + overlapPx

            layout(constraints.maxWidth, layoutHeight) {
                // Place header spacer (not visible, just offsets)
                headerPlaceable.place(0, 0)
                overlapSpacer.place(0, headerPlaceable.height)
                // Place cards
                cardPlacements.forEach { (yy, pl) -> pl.place(0, yy) }
            }
        }

        // Sticky/fixed header (as before)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = with(density) { animatedHeaderOffset.toDp() })
                .zIndex((sessions.size + 1).toFloat())
        ) {
            TopRoundedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight),
                elevation = 8.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomStart
                ) {
                    HistoryControlsSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun SessionCardContent(
    session: Session,
    onSessionClick: (Session) -> Unit,
    topContentSpacing: Dp,
    isFirstItem: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 20.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 20.dp
            )
    ) {
        // Session content - reuse existing SessionItem content or create new layout
        SessionItem(
            session = session,
            onSessionClick = onSessionClick
        )
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

