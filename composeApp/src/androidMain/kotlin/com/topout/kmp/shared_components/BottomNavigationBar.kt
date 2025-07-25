package com.topout.kmp.shared_components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.topout.kmp.NavTab

@Composable
fun BottomNavigationBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit
) {
    NavigationBar {

        NavigationBarItem(
            selected = selectedTab is NavTab.History,
            onClick = { onTabSelected(NavTab.History) },
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History") }
        )
        NavigationBarItem(
            selected = selectedTab is NavTab.LiveSession,
            onClick = { onTabSelected(NavTab.LiveSession) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Live Session") },
            label = { Text("Live") }
        )
        NavigationBarItem(
            selected = selectedTab is NavTab.Settings,
            onClick = { onTabSelected(NavTab.Settings) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )

    }
}
