package com.topout.kmp

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.google.firebase.FirebaseApp
import com.topout.kmp.domain.SyncOfflineChanges
import com.topout.kmp.features.*
import com.topout.kmp.models.Session
import com.topout.kmp.shared_components.*
import com.topout.kmp.ui.theme.ThemePalette
import com.topout.kmp.ui.theme.TopOutAppTheme
import com.topout.kmp.utils.*
import com.topout.kmp.notifications.NotificationHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts

data class ThemeState(
    val palette: ThemePalette = ThemePalette.CLASSIC_RED,
    val isDarkMode: Boolean = false
)

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
        NotificationHelper.createGeneralChannel(this)

        setContent {
            KoinContext {
                val themePreferences = remember { ThemePreferences(this@MainActivity) }
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

                val updateTheme: (ThemeState) -> Unit = { newThemeState ->
                    themeState = newThemeState
                    themePreferences.saveThemePalette(newThemeState.palette)
                    themePreferences.saveDarkMode(newThemeState.isDarkMode)
                }

                CompositionLocalProvider(
                    LocalThemeState provides themeState,
                    LocalThemeUpdater provides updateTheme
                ) {
                    TopOutAppTheme(
                        palette = themeState.palette,
                        darkTheme = themeState.isDarkMode
                    ) {
                        val navController = rememberNavController()

                        var selectedTab by remember {
                            mutableStateOf<NavTab>(
                                if (intent.getBooleanExtra("NAVIGATE_TO_LIVE_SESSION", false)) {
                                    NavTab.LiveSession
                                } else {
                                    NavTab.LiveSession
                                }
                            )
                        }

                        var isAppLoading by remember { mutableStateOf(true) }

                        val syncOfflineChanges = koinInject<SyncOfflineChanges>()
                        val context = LocalContext.current

                        val locationPermissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission()
                        ) { /* handled later */ }

                        val notificationPermissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            if (!isGranted) {
                                Toast.makeText(context, "Notifications permission denied", Toast.LENGTH_SHORT).show()
                            }
                        }

                        var hasLocationPermission by remember { mutableStateOf(false) }

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

                        LaunchedEffect(Unit) {
                            val locationGranted = ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            hasLocationPermission = locationGranted

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val notificationGranted = ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED

                                if (!notificationGranted) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }

                            kotlinx.coroutines.delay(2000)
                            isAppLoading = false
                        }

                        if (isAppLoading) {
                            LoadingAnimation(text = "Welcome to TopOut")
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
                                            .padding(bottom = innerPadding.calculateBottomPadding())
                                    ) {
                                        composable(NavTab.Settings.route) { SettingsScreen() }
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
                                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                                },
                                                onNavigateToSessionDetails = { sessionId ->
                                                    navController.navigate("session/$sessionId") {
                                                        popUpTo(NavTab.LiveSession.route) { inclusive = true }
                                                        popUpTo(NavTab.History.route) { saveState = true }
                                                    }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

fun NavController.navigateToSession(session: Session) {
    this.navigate("session/${session.id}")
}
