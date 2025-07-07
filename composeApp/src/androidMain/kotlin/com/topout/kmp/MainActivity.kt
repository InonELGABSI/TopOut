package com.topout.kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.topout.kmp.features.HistoryScreen
import com.topout.kmp.models.Session
import com.topout.kmp.shared_components.BottomNavigationBar


sealed class NavTab(val route: String, val title: String) {
    data object Home : NavTab("home", "Home")
    data object History : NavTab("history", "History")
    data object Settings : NavTab("settings", "Settings")
    data object Livesession : NavTab("live_session", "Live Session")
    data object SessionDetail : NavTab("session/{sessionId}", "Session") {
        fun createRoute(sessionId: String) = "session/$sessionId"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                var selectedTab by remember { mutableStateOf<NavTab>(NavTab.History) }

                Scaffold (
                    topBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val sessionId = navBackStackEntry?.arguments?.getString("sessionId")
                        val currentRoute = navBackStackEntry?.destination?.route
                        val isSessionScreen = currentRoute == "session"
                        val appBarTitle: String = when {
                            currentRoute?.startsWith("session/") == true -> sessionId ?: "Session"
                            else -> "Sessions"
                        }
                        AppBar(
                            title = appBarTitle,
                            showBackButton = isSessionScreen,
                            onBackClick = { navController.popBackStack() }
                        )

                    },
                    bottomBar = {
                        BottomNavigationBar(
                            selectedTab = selectedTab,
                            onTabSelected = {
                                selectedTab = it
                                navController.navigate(it.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = NavTab.History.route,
                        modifier = Modifier.padding(innerPadding)
                    ){
//                        composable(NavTab.Home.route) {
//                            HomeScreen(
//                            )
//                        }

//                        composable(NavTab.Settings.route) {
//                            SettingsScreen(
//                            )
//                        }

                        composable(NavTab.History.route) {
                            HistoryScreen(
                                onSessionClick = { session ->
                                    navController.navigateToSession(session)
                                }
                            )
                        }

//                        composable(NavTab.Livesession.route) {
//                            LiveSessionScreen(
//                            )
//                        }
                    }
                    // Main content of the app
                }
            }
        }
    }
}
fun NavController.navigateToSession(session: Session) {
    this.navigate("session/${session.id}")
}

// Fix scrollable top app bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        windowInsets = TopAppBarDefaults.windowInsets,
        title = { Text(text = title) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    )
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}