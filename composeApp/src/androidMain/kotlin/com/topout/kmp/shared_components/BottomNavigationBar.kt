package com.topout.kmp.shared_components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
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
            selected = selectedTab is NavTab.Home,
            onClick = { onTabSelected(NavTab.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedTab is NavTab.History,
            onClick = { onTabSelected(NavTab.History) },
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History") }
        )
        NavigationBarItem(
            selected = selectedTab is NavTab.Settings,
            onClick = { onTabSelected(NavTab.Settings) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
//        NavigationBarItem(
//            selected = selectedTab is NavTab.Livesession,
//            onClick = { onTabSelected(NavTab.Livesession) },
//            icon = { Icon(Icons.Default.Favorite, contentDescription = "Live Session") },
//            label = { Text("Live Session") }
//        )
    }
}