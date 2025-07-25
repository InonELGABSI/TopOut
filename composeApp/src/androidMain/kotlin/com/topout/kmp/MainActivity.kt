package com.topout.kmp
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.topout.kmp.shared_components.TopFadeGradient
import org.koin.compose.KoinContext

import androidx.compose.ui.platform.LocalContext
import com.topout.kmp.utils.observeNetworkStatus
import com.topout.kmp.utils.NetworkStatus
import com.topout.kmp.domain.SyncOfflineChanges
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import kotlinx.coroutines.flow.distinctUntilChanged

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.topout.kmp.ui.theme.TopOutAppTheme
import com.topout.kmp.ui.theme.ThemePalette
import com.topout.kmp.utils.ThemePreferences

// Theme state management
data class ThemeState(
    val palette: ThemePalette = ThemePalette.CLASSIC_RED,
    val isDarkMode: Boolean = false
)

// CompositionLocal for theme management
val LocalThemeState = staticCompositionLocalOf { ThemeState() }
val LocalThemeUpdater = staticCompositionLocalOf<(ThemeState) -> Unit> { {} }

sealed class NavTab(val route: String) {
    data object History : NavTab("history")
    data object Settings : NavTab("settings")
    data object LiveSession : NavTab("live_session")
    data object SessionDetail : NavTab("session/{sessionId}")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            KoinContext {
                // Theme preferences
                val themePreferences = remember { ThemePreferences(this@MainActivity) }

                // Theme state management
                val systemDarkMode = isSystemInDarkTheme()
                var themeState by remember {
                    mutableStateOf(
                        ThemeState(
                            palette = themePreferences.getThemePalette(),
                            isDarkMode = if (themePreferences.hasDarkModePreference()) {
                                themePreferences.getDarkMode(systemDarkMode)
                            } else {
                                systemDarkMode
                            }
                        )
                    )
                }

                // Theme update function
                val updateTheme: (ThemeState) -> Unit = { newThemeState ->
                    themeState = newThemeState
                    // Save theme preferences
                    themePreferences.saveThemePalette(newThemeState.palette)
                    themePreferences.saveDarkMode(newThemeState.isDarkMode)
                }

                // Provide theme state to the entire app
                CompositionLocalProvider(
                    LocalThemeState provides themeState,
                    LocalThemeUpdater provides updateTheme
                ) {
                    TopOutAppTheme(
                        palette = themeState.palette,
                        darkTheme = themeState.isDarkMode
                    ) {
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
                                        onBackClick = { navController.popBackStack() },
                                        isTransparent = true
                                    )
                                },
                                // Remove contentWindowInsets to allow content to go under system bars
                                contentWindowInsets = WindowInsets(0),
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
                                Box(modifier = Modifier.fillMaxSize()) {
                                    NavHost(
                                        navController = navController,
                                        startDestination = NavTab.LiveSession.route,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(bottom = innerPadding.calculateBottomPadding()) // Only bottom padding for nav bar
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

                                    // Add the fade gradient overlay on top
                                    TopFadeGradient(
                                        modifier = Modifier.align(Alignment.TopCenter)
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
