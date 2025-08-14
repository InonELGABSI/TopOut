package com.topout.kmp.features

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.zIndex
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.topout.kmp.ui.theme.ThemePalette
import com.topout.kmp.ui.theme.TopOutTheme
import com.topout.kmp.features.settings.SettingsState
import com.topout.kmp.features.settings.SettingsViewModel
import com.topout.kmp.models.User
import com.topout.kmp.platform.NotificationController
import com.topout.kmp.shared_components.rememberTopContentSpacingDp
import com.topout.kmp.shared_components.BottomRoundedCard
import com.topout.kmp.shared_components.SessionToast
import com.topout.kmp.shared_components.SessionToastType
import com.topout.kmp.LocalThemeState
import com.topout.kmp.LocalThemeUpdater
import com.topout.kmp.shared_components.LottieToggleButton
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    // State for session toast
    var toastType by remember { mutableStateOf<SessionToastType?>(null) }
    var showSessionToast by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // TODO: viewModel.fetchUserSettings()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                is SettingsState.Loading -> SettingsLoadingContent()
                is SettingsState.Error -> SettingsErrorContent(uiState.errorMessage)
                is SettingsState.Loaded -> StackedSettingsCards(
                    uiState.user,
                    viewModel = viewModel,
                    onShowToast = { type ->
                        toastType = type
                        showSessionToast = true
                    }
                )
            }

            // Session Toast
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
}

@Composable
fun StackedSettingsCards(
    user: User,
    modifier: Modifier = Modifier,
    overlap: Dp = 20.dp,
    viewModel: SettingsViewModel = koinViewModel(),
    onShowToast: (SessionToastType) -> Unit
) {
    val palette = listOf(
        MaterialTheme.colorScheme.surfaceContainerHigh,
        MaterialTheme.colorScheme.surfaceContainer,
        MaterialTheme.colorScheme.surfaceContainerLow
    )
    val scrollState = rememberScrollState()

    var editableUser by remember { mutableStateOf(user) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isEditingPreferences by remember { mutableStateOf(false) }
    var isEditingThresholds by remember { mutableStateOf(false) }

    // Define the 4 cards (added Theme)
    val settingsCards = listOf(
        "Profile",
        "Preferences",
        "Alert Thresholds",
        "Theme"

    )

    Layout(
        modifier = modifier
            .verticalScroll(scrollState)
            .fillMaxWidth(),
        content = {
            // Add spacer for top content spacing
            Spacer(Modifier.height(rememberTopContentSpacingDp()))
            // All settings cards (stacked)
            settingsCards.forEachIndexed { index, cardType ->
                val color = palette[index % palette.size]
                val elevation = 6.dp + (index * 2).dp
                val cardContent: @Composable () -> Unit = {
                    when (cardType) {
                        "Profile" -> ProfileCardContent(
                            user = editableUser,
                            onUserChange = { editableUser = it },
                            isEditing = isEditingProfile,
                            onToggleEdit = { isEditingProfile = it },
                            viewModel = viewModel,
                            onShowToast = onShowToast
                        )
                        "Preferences" -> PreferencesCardContent(
                            user = editableUser,
                            onUserChange = { editableUser = it },
                            isEditing = isEditingPreferences,
                            onToggleEdit = { isEditingPreferences = it },
                            viewModel = viewModel,
                            onShowToast = onShowToast
                        )
                        "Alert Thresholds" -> ThresholdsCardContent(
                            user = editableUser,
                            onUserChange = { editableUser = it },
                            isEditing = isEditingThresholds,
                            onToggleEdit = { isEditingThresholds = it },
                            viewModel = viewModel,
                            onShowToast = onShowToast
                        )
                        "Theme" -> ThemeCardContent()
                    }
                }

                Box(
                    modifier = Modifier.zIndex(index.toFloat())
                ) {
                    BottomRoundedCard(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = elevation,
                            containerColor = color,
                        content = {
                            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                cardContent()
                            }
                        }
                    )
                }
            }
        }
    ) { measurables, constraints ->
        val overlapPx = overlap.roundToPx()
        var y = 0

        // Header spacer
        val headerPlaceable = measurables[0].measure(constraints)
        val overlapSpacer = measurables[1].measure(constraints)
        y += headerPlaceable.height
        y += overlapSpacer.height

        val cardPlacements = mutableListOf<Pair<Int, androidx.compose.ui.layout.Placeable>>()
        // Cards
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
}

@Composable
fun ProfileCardContent(
    user: User,
    onUserChange: (User) -> Unit,
    isEditing: Boolean,
    onToggleEdit: (Boolean) -> Unit,
    viewModel: SettingsViewModel,
    onShowToast: (SessionToastType) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            EditableTextField(
                label = "Name",
                value = user.name ?: "",
                onValueChange = { onUserChange(user.copy(name = it.ifEmpty { null })) },
                isEditing = isEditing,
                icon = Icons.Default.Person
            )

            EditableTextField(
                label = "Email",
                value = user.email ?: "",
                onValueChange = { onUserChange(user.copy(email = it.ifEmpty { null })) },
                isEditing = isEditing,
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            EditableTextField(
                label = "Image URL",
                value = user.imgUrl ?: "",
                onValueChange = { onUserChange(user.copy(imgUrl = it.ifEmpty { null })) },
                isEditing = isEditing,
                icon = Icons.Default.Image
            )

            ReadOnlyField(
                label = "User ID",
                value = user.id,
                icon = Icons.Default.Key
            )

            ReadOnlyField(
                label = "Created At",
                value = formatDate(user.createdAt ?: 0L),
                icon = Icons.Default.CalendarMonth
            )

            // Save/Cancel buttons when editing
            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onToggleEdit(false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            viewModel.updateUser(user)
                            onToggleEdit(false)
                            onShowToast(SessionToastType.PROFILE_UPDATED)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }

        // Circular edit button in top right corner
        if (!isEditing) {
            FloatingActionButton(
                onClick = { onToggleEdit(true) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PreferencesCardContent(
    user: User,
    onUserChange: (User) -> Unit,
    isEditing: Boolean,
    onToggleEdit: (Boolean) -> Unit,
    viewModel: SettingsViewModel,
    onShowToast: (SessionToastType) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            UnitPreferenceSelector(
                currentUnit = user.unitPreference ?: "meters",
                onUnitChange = { onUserChange(user.copy(unitPreference = it)) },
                isEditing = isEditing
            )

            NotificationToggle(
                enabled = user.enabledNotifications ?: false,
                onToggle = { onUserChange(user.copy(enabledNotifications = it)) },
                isEditing = isEditing
            )

            // Save/Cancel buttons when editing
            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onToggleEdit(false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            viewModel.updateUser(user)
                            onToggleEdit(false)
                            onShowToast(SessionToastType.PREFERENCES_UPDATED)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }

        // Circular edit button in top right corner
        if (!isEditing) {
            FloatingActionButton(
                onClick = { onToggleEdit(true) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Preferences",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ThresholdsCardContent(
    user: User,
    onUserChange: (User) -> Unit,
    isEditing: Boolean,
    onToggleEdit: (Boolean) -> Unit,
    viewModel: SettingsViewModel,
    onShowToast: (SessionToastType) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Alert Thresholds",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            ThresholdField(
                label = "Relative Height Threshold",
                value = user.relativeHeightFromStartThr ?: 0.0,
                onValueChange = { onUserChange(user.copy(relativeHeightFromStartThr = it)) },
                isEditing = isEditing,
                unit = user.unitPreference ?: "meters"
            )

            ThresholdField(
                label = "Total Height Threshold",
                value = user.totalHeightFromStartThr ?: 0.0,
                onValueChange = { onUserChange(user.copy(totalHeightFromStartThr = it)) },
                isEditing = isEditing,
                unit = user.unitPreference ?: "meters"
            )

            ThresholdField(
                label = "Average Speed Threshold",
                value = user.currentAvgHeightSpeedThr ?: 0.0,
                onValueChange = { onUserChange(user.copy(currentAvgHeightSpeedThr = it)) },
                isEditing = isEditing,
                unit = "${user.unitPreference ?: "meters"}/min"
            )

            // Save/Cancel buttons when editing
            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onToggleEdit(false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            viewModel.updateUser(user)
                            onToggleEdit(false)
                            onShowToast(SessionToastType.THRESHOLDS_UPDATED) // Show success toast
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }

        // Circular edit button in top right corner
        if (!isEditing) {
            FloatingActionButton(
                onClick = { onToggleEdit(true) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Thresholds",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ThemeCardContent() {
    // Get theme state and updater from MainActivity
    val currentThemeState = LocalThemeState.current
    val updateTheme = LocalThemeUpdater.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
    ) {
        // Header with toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            LottieToggleButton(
                isToggled = currentThemeState.isDarkMode,
                onToggle = { isDark ->
                    updateTheme(currentThemeState.copy(isDarkMode = isDark))
                },
                height = 52.dp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theme selection
        Text(
            text = "Color Palette",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Theme options with color previews
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemePalette.entries.forEach { themePalette ->
                ThemeOptionItem(
                    themePalette = themePalette,
                    isSelected = currentThemeState.palette == themePalette,
                    isDarkMode = currentThemeState.isDarkMode,
                    onSelect = { selectedPalette ->
                        updateTheme(currentThemeState.copy(palette = selectedPalette))
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeOptionItem(
    themePalette: ThemePalette,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onSelect: (ThemePalette) -> Unit
) {
    val colorScheme = TopOutTheme.getColorScheme(themePalette, isDarkMode)
    val themeName = when (themePalette) {
        ThemePalette.CLASSIC_RED -> "Classic Red"
        ThemePalette.OCEAN_BLUE -> "Ocean Blue"
        ThemePalette.FOREST_GREEN -> "Forest Green"
        ThemePalette.STORM_GRAY -> "Storm Gray"
        ThemePalette.SUNSET_ORANGE -> "Sunset Orange"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(themePalette) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Color palette preview
            ColorPalettePreview(
                colorScheme = colorScheme,
                modifier = Modifier.size(60.dp)
            )

            // Theme info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = themeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Light & Dark compatible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ColorPalettePreview(
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    // Create a small color palette preview with key colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colorScheme.background)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Primary color
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(colorScheme.primary)
        )

        // Secondary color
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(colorScheme.secondary)
        )

        // Surface color
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(colorScheme.surfaceVariant)
        )

        // Tertiary color (if available) or primary container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(colorScheme.primaryContainer)
        )
    }
}

@Composable
fun SettingsLoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = rememberTopContentSpacingDp(),
                start = 24.dp,
                end = 24.dp,
                bottom = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading settings...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SettingsErrorContent(errorMessage: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = rememberTopContentSpacingDp(),
                start = 24.dp,
                end = 24.dp,
                bottom = 24.dp
            ),
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
            text = "Settings Error",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EditableTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    if (isEditing) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    } else {
        ReadOnlyField(label = label, value = value.ifEmpty { "Not set" }, icon = icon)
    }
}

@Composable
fun ReadOnlyField(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitPreferenceSelector(
    currentUnit: String,
    onUnitChange: (String) -> Unit,
    isEditing: Boolean
) {
    if (isEditing) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox (
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentUnit.capitalize(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Unit Preference") },
                leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("meters", "feet").forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.capitalize()) },
                        onClick = {
                            onUnitChange(unit)
                            expanded = false
                        }
                    )
                }
            }
        }
    } else {
        ReadOnlyField(
            label = "Unit Preference",
            value = currentUnit.capitalize(),
            icon = Icons.Default.Straighten
        )
    }
}

@Composable
fun NotificationToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    isEditing: Boolean
) {
    val notificationController = koinInject<NotificationController>()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Notifications",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = enabled,
            onCheckedChange = { newValue ->
                onToggle(newValue)
                // Send welcome notification when enabling notifications
                if (newValue && !enabled) {
                    notificationController.sendNotification(
                        title = "TopOut Notifications",
                        message = "You are now receiving push notifications from TopOut"
                    )
                }
            },
            enabled = isEditing
        )
    }
}

@Composable
fun ThresholdField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    isEditing: Boolean,
    unit: String
) {
    if (isEditing) {
        OutlinedTextField(
            value = if (value == 0.0) "" else value.toString(),
            onValueChange = {
                val doubleValue = it.toDoubleOrNull() ?: 0.0
                onValueChange(doubleValue)
            },
            label = { Text("$label ($unit)") },
            leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
    } else {
        ReadOnlyField(
            label = "$label ($unit)",
            value = if (value == 0.0) "Not set" else value.toString(),
            icon = Icons.Default.Speed
        )
    }
}

private fun formatDate(timestamp: Long): String {
    return if (timestamp == 0L) {
        "Not available"
    } else {
        SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
