package com.topout.kmp.features

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.topout.kmp.features.sessions.SessionsState
import com.topout.kmp.features.sessions.SessionsViewModel
import com.topout.kmp.features.sessions.SortOption
import com.topout.kmp.models.Session
import com.topout.kmp.shared_components.TopRoundedCard
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import com.topout.kmp.shared_components.BottomRoundedCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: SessionsViewModel = koinViewModel(),
    onSessionClick: (Session) -> Unit
) {

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
                        // Show empty state with search/sort controls still visible
                        StackedSessionCards(
                            sessions = emptyList(),
                            onSessionClick = onSessionClick,
                            viewModel = viewModel,
                            showEmptyState = true
                        )
                    } else {
                        StackedSessionCards(
                            sessions = uiState.sessions ?: emptyList(),
                            onSessionClick = onSessionClick,
                            viewModel = viewModel
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
    viewModel: SessionsViewModel,
    modifier: Modifier = Modifier
) {
    val searchText by viewModel.currentSearchTextState.collectAsState()
    val currentSortOption by viewModel.currentSortOptionState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    val sortOptions = listOf(
        "Date (Newest)",
        "Date (Oldest)",
        "Duration (Longest)",
        "Duration (Shortest)",
        "Ascent (Highest)",
        "Ascent (Lowest)"
    )

    fun sortOptionToString(sortOption: SortOption): String {
        return when (sortOption) {
            SortOption.DATE_NEWEST -> "Date (Newest)"
            SortOption.DATE_OLDEST -> "Date (Oldest)"
            SortOption.DURATION_LONGEST -> "Duration (Longest)"
            SortOption.DURATION_SHORTEST -> "Duration (Shortest)"
            SortOption.ASCENT_HIGHEST -> "Ascent (Highest)"
            SortOption.ASCENT_LOWEST -> "Ascent (Lowest)"
        }
    }

    fun stringToSortOption(sortString: String): SortOption {
        return when (sortString) {
            "Date (Newest)" -> SortOption.DATE_NEWEST
            "Date (Oldest)" -> SortOption.DATE_OLDEST
            "Duration (Longest)" -> SortOption.DURATION_LONGEST
            "Duration (Shortest)" -> SortOption.DURATION_SHORTEST
            "Ascent (Highest)" -> SortOption.ASCENT_HIGHEST
            "Ascent (Lowest)" -> SortOption.ASCENT_LOWEST
            else -> SortOption.DATE_NEWEST
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                OutlinedButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(48.dp),
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
                        text = sortOptionToString(currentSortOption),
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
                                showSortMenu = false
                                viewModel.sortSessions(stringToSortOption(option))
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { newText ->
                    viewModel.searchSessions(newText)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
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
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.searchSessions("")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 18.sp
                ),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
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
    viewModel: SessionsViewModel,
    overlap: Dp = 40.dp,
    modifier: Modifier = Modifier,
    showEmptyState: Boolean = false
) {
    val palette = listOf(
        MaterialTheme.colorScheme.surfaceContainerHigh,
        MaterialTheme.colorScheme.surfaceContainer
    )
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val headerHeight = 200.dp

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
        Layout(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxWidth(),
            content = {
                Spacer(Modifier.height(headerHeight))
                sessions.asReversed().forEachIndexed { revIndex, session ->
                    val color = palette[revIndex % palette.size]
                    val elevation = 6.dp + (revIndex * 2).dp
                    val cardContent: @Composable () -> Unit = {
                        SessionCardContent(
                            session = session,
                            onSessionClick = onSessionClick,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .zIndex(revIndex.toFloat())
                    ) {
                        BottomRoundedCard(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = elevation,
                            containerColor = color,
                            content = cardContent
                        )
                    }
                }
            }
        ) { measurables, constraints ->
            val overlapPx = overlap.roundToPx()
            var y = 0
            val headerPlaceable = measurables[0].measure(constraints)
            y += headerPlaceable.height

            val cardPlacements = mutableListOf<Pair<Int, Placeable>>()
            for (i in 1 until measurables.size) {
                val placeable = measurables[i].measure(constraints)
                cardPlacements.add(y to placeable)
                y += placeable.height - overlapPx
            }
            val layoutHeight = if (cardPlacements.isEmpty()) y else y + overlapPx

            layout(constraints.maxWidth, layoutHeight) {
                headerPlaceable.place(0, 0)
                cardPlacements.forEach { (yy, pl) -> pl.place(0, yy) }
            }
        }

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
                        viewModel = viewModel,
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

        if (showEmptyState) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = headerHeight)
                    .zIndex((sessions.size + 2).toFloat()),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateContent()
            }
        }
    }
}


@Composable
fun SessionCardContent(
    session: Session,
    onSessionClick: (Session) -> Unit,
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
        SessionItem(
            session = session,
            onSessionClick = onSessionClick
        )
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