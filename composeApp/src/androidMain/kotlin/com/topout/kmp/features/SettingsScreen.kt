package com.topout.kmp.features

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
import com.topout.kmp.features.settings.SettingsState
import com.topout.kmp.features.settings.SettingsViewModel
import com.topout.kmp.models.User
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (uiState) {
            is SettingsState.Loading -> LoadingContent()
            is SettingsState.Error -> ErrorContent(uiState.errorMessage)
            is SettingsState.Loaded -> UserSettingsContent(uiState.user)
        }
    }
}

@Composable
fun UserSettingsContent(user: User) {
    var editableUser by remember { mutableStateOf(user) }
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Section
        SettingsSection(
            title = "Profile",
            icon = Icons.Default.Person
        ) {
            EditableTextField(
                label = "Name",
                value = editableUser.name ?: "",
                onValueChange = { editableUser = editableUser.copy(name = it.ifEmpty { null }) },
                isEditing = isEditing,
                icon = Icons.Default.Person
            )

            EditableTextField(
                label = "Email",
                value = editableUser.email ?: "",
                onValueChange = { editableUser = editableUser.copy(email = it.ifEmpty { null }) },
                isEditing = isEditing,
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            EditableTextField(
                label = "Image URL",
                value = editableUser.imgUrl ?: "",
                onValueChange = { editableUser = editableUser.copy(imgUrl = it.ifEmpty { null }) },
                isEditing = isEditing,
                icon = Icons.Default.Image
            )
        }

        // Preferences Section
        SettingsSection(
            title = "Preferences",
            icon = Icons.Default.Settings
        ) {
            UnitPreferenceSelector(
                currentUnit = editableUser.unitPreference ?: "meters",
                onUnitChange = { editableUser = editableUser.copy(unitPreference = it) },
                isEditing = isEditing
            )

            NotificationToggle(
                enabled = editableUser.enabledNotifications ?: false,
                onToggle = { editableUser = editableUser.copy(enabledNotifications = it) },
                isEditing = isEditing
            )
        }

        // Thresholds Section
        SettingsSection(
            title = "Alert Thresholds",
            icon = Icons.Default.Warning
        ) {
            ThresholdField(
                label = "Relative Height Threshold",
                value = editableUser.relativeHeightFromStartThr ?: 0.0,
                onValueChange = { editableUser = editableUser.copy(relativeHeightFromStartThr = it) },
                isEditing = isEditing,
                unit = editableUser.unitPreference ?: "meters"
            )

            ThresholdField(
                label = "Total Height Threshold",
                value = editableUser.totalHeightFromStartThr ?: 0.0,
                onValueChange = { editableUser = editableUser.copy(totalHeightFromStartThr = it) },
                isEditing = isEditing,
                unit = editableUser.unitPreference ?: "meters"
            )

            ThresholdField(
                label = "Average Speed Threshold",
                value = editableUser.currentAvgHeightSpeedThr ?: 0.0,
                onValueChange = { editableUser = editableUser.copy(currentAvgHeightSpeedThr = it) },
                isEditing = isEditing,
                unit = "${editableUser.unitPreference ?: "meters"}/min"
            )
        }

        // Account Info Section
        SettingsSection(
            title = "Account Information",
            icon = Icons.Default.Info
        ) {
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
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isEditing) {
                OutlinedButton(
                    onClick = {
                        editableUser = user
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        // TODO: Call viewModel.updateUser(editableUser)
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            } else {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            content()
        }
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
            onCheckedChange = onToggle,
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

