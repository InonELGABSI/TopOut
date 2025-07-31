import SwiftUI
import Shared
import Firebase

// MARK: - Bottom‑bar tabs (mirrors Android)
enum NavTab: String, CaseIterable, Identifiable {
    case history = "history"
    case liveSession = "live_session"
    case settings = "settings"
    
    var id: String { rawValue }
    
    var title: String {
        switch self {
        case .history:     return "Sessions History"
        case .liveSession: return "Live Session"
        case .settings:    return "Settings"
        }
    }
    
    var icon: String {
        switch self {
        case .history:     return "clock.fill"
        case .liveSession: return "play.fill"
        case .settings:    return "gearshape.fill"
        }
    }
}

// MARK: - UIApplicationDelegate
final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        return true
    }
}

// MARK: - Root SwiftUI entry point
@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var delegate
    
    // Shared singletons
    @StateObject private var themeManager   = ThemeManager.shared
    @StateObject private var networkMonitor = NetworkMonitor()
    
    // UI state
    @State private var selectedTab: NavTab = .liveSession
    @State private var isAppLoading        = true
    
    // DI / Koin bootstrap
    init() {
        KoinKt.doInitKoin()

        Task {
            let ensureAnon: EnsureAnonymousUser = get()
            _ = try? await ensureAnon.invoke()
        }
    }

    
    // MARK: Scene
    var body: some Scene {
        WindowGroup {
            Group {
                if isAppLoading {
                    AnyView(
                        LoadingAnimation(text: "Welcome to TopOut")
                            .onAppear {
                                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                    isAppLoading = false
                                }
                            }
                    )
                } else {
                    AnyView(
                        NavigationView {
                            ZStack {
                                TabView(selection: $selectedTab) {
                                    HistoryView()
                                        .tabItem { Label("History", systemImage: NavTab.history.icon) }
                                        .tag(NavTab.history)

                                    LiveSessionView()
                                        .tabItem { Label("Live", systemImage: NavTab.liveSession.icon) }
                                        .tag(NavTab.liveSession)

                                    SettingsView()
                                        .tabItem { Label("Settings", systemImage: NavTab.settings.icon) }
                                        .tag(NavTab.settings)
                                }
                                TopFadeGradient()
                            }
                            .onAppear {
                                if networkMonitor.status == .available {
                                    Task {
                                        let sync: SyncOfflineChanges = get()
                                        _ = try? await sync.invoke()
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Global environment injections
            .environmentObject(networkMonitor)
            .environmentObject(themeManager)          // still useful for Settings screen
            .topOutTheme(themeManager.current)        // 🔑 new theme Environment value
            .preferredColorScheme(themeManager.current.colorScheme)
        }
    }
}
