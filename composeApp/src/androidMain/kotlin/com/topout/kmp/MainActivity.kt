package com.topout.kmp
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.topout.kmp.features.HistoryScreen
import com.topout.kmp.features.LiveSessionScreen
import com.topout.kmp.features.SettingsScreen
import com.topout.kmp.features.SessionDetailsScreen
import com.topout.kmp.models.Session
import com.topout.kmp.shared_components.BottomNavigationBar
import com.topout.kmp.shared_components.ChipControlBar
import com.topout.kmp.shared_components.LoadingAnimation
import org.koin.compose.KoinContext

import androidx.compose.ui.platform.LocalContext
import com.topout.kmp.utils.observeNetworkStatus
import com.topout.kmp.utils.NetworkStatus
import com.topout.kmp.domain.SyncOfflineChanges
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import kotlinx.coroutines.flow.distinctUntilChanged

import android.widget.Toast


sealed class NavTab(val route: String, val title: String) {
    data object History : NavTab("history", "History")
    data object Settings : NavTab("settings", "Settings")
    data object LiveSession : NavTab("live_session", "Live Session")
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
            KoinContext {
                MaterialTheme {
                    var isAppLoading by remember { mutableStateOf(true) }
                    val navController = rememberNavController()
                    var selectedTab by remember { mutableStateOf<NavTab>(NavTab.LiveSession) }


                    // Get the DI use case
                    val syncOfflineChanges = koinInject<SyncOfflineChanges>()
                    val context = LocalContext.current

                    // Launch a coroutine to observe network and trigger sync
                    LaunchedEffect(Unit) {
                        observeNetworkStatus(context)
                            .distinctUntilChanged()
                            .collectLatest { status ->
                            if (status == NetworkStatus.Available) {
                                Toast.makeText(context, "Network reconnected, syncing!", Toast.LENGTH_SHORT).show()
                                syncOfflineChanges.invoke()
                            } else {
                                Toast.makeText(context, "Network disconnected!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }


                    // --- Permission State ---
                    var hasLocationPermission by remember { mutableStateOf(false) }
                    // Compose launcher for permission request
                    val launcher = rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        hasLocationPermission = isGranted
                    }

                    // Check permission initially
                    LaunchedEffect(Unit) {
                        val granted = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        hasLocationPermission = granted

                        // Simulate app loading time (you can adjust this or add real initialization)
                        kotlinx.coroutines.delay(2000) // 2 seconds loading
                        isAppLoading = false
                    }

                    if (isAppLoading) {
                        LoadingAnimation(
                            text = "Welcome to TopOut"
                        )
                    } else {
                        Scaffold(
                            topBar = {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentRoute = navBackStackEntry?.destination?.route
                                val isSessionScreen = currentRoute == "session/{sessionId}"
                                val appBarTitle: String = when (currentRoute) {
                                    "session/{sessionId}" -> "Session Details"
                                    NavTab.History.route -> "Sessions History"
                                    NavTab.LiveSession.route -> "Live Session"
                                    NavTab.Settings.route -> "Settings"
                                    else -> "TopOut"
                                }

                                ChipControlBar(
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
                                startDestination = NavTab.LiveSession.route,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(NavTab.Settings.route) {
                                    SettingsScreen()
                                }
                                composable(NavTab.History.route) {
                                    HistoryScreen(
                                        onSessionClick = { session ->
                                            navController.navigateToSession(session)
                                        }
                                    )
                                }
                                composable(NavTab.LiveSession.route) {
                                    LiveSessionScreen(
                                        hasLocationPermission = hasLocationPermission,
                                        onRequestLocationPermission = {
                                            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        },
                                        onNavigateToSessionDetails = { sessionId ->
                                            // Navigate to session details and clear live session from back stack
                                            navController.navigate("session/$sessionId") {
                                                // Remove live session from back stack
                                                popUpTo(NavTab.LiveSession.route) {
                                                    inclusive = true
                                                }
                                                // Set history as the parent destination
                                                popUpTo(NavTab.History.route) {
                                                    saveState = true
                                                }
                                            }
                                            // Update selected tab to history so back navigation is correct
                                            selectedTab = NavTab.History
                                        }
                                    )
                                }
                                composable(NavTab.SessionDetail.route) { backStackEntry ->
                                    val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                                    SessionDetailsScreen(
                                        sessionId = sessionId,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
fun NavController.navigateToSession(session: Session) {
    this.navigate("session/${session.id}")
}
