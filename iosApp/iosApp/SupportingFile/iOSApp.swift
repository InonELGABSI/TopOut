import SwiftUI
import Shared
import Firebase

// MARK: - Transparent Navigation Bar Appearance
extension UINavigationBarAppearance {
    static func primaryBar(primaryColor: UIColor) -> UINavigationBarAppearance {
        let appearance = UINavigationBarAppearance()
        appearance.configureWithOpaqueBackground()

        // Set transparent primary color background
        appearance.backgroundColor = primaryColor.withAlphaComponent(0.85)
        appearance.backgroundEffect = UIBlurEffect(style: .systemUltraThinMaterial)

        // Set text colors to contrast with primary color
        appearance.titleTextAttributes = [.foregroundColor: UIColor.white]
        appearance.largeTitleTextAttributes = [.foregroundColor: UIColor.white]

        return appearance
    }
}

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
    
    // Apple's best practice: Simple theme management with @AppStorage
    @StateObject private var themeManager = AppThemeManager.shared
    @StateObject private var networkMonitor = NetworkMonitor()

    // UI state
    @State private var selectedTab: NavTab = .liveSession
    @State private var isAppLoading = true

    // DI / Koin bootstrap
    init() {
        KoinKt.doInitKoin()

        Task {
            let ensureAnon: EnsureAnonymousUser = get()
            _ = try? await ensureAnon.invoke()
        }
    }

    private func updateNavigationBarAppearance() {
        let primaryColor = UIColor(themeManager.currentTheme.primary)
        let primaryBar = UINavigationBarAppearance.primaryBar(primaryColor: primaryColor)
        UINavigationBar.appearance().standardAppearance = primaryBar
        UINavigationBar.appearance().scrollEdgeAppearance = primaryBar
        UINavigationBar.appearance().compactAppearance = primaryBar
        UINavigationBar.appearance().tintColor = .white
    }

    var body: some Scene {
        WindowGroup {
            Group {
                if isAppLoading {
                    LoadingAnimation(text: "Welcome to TopOut")
                        .onAppear {
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                isAppLoading = false
                            }
                        }
                } else {
                    TabView(selection: $selectedTab) {
                        HistoryNavStack()
                            .tabItem { Label("History", systemImage: NavTab.history.icon) }
                            .tag(NavTab.history)

                        LiveNavStack()
                            .tabItem { Label("Live", systemImage: NavTab.liveSession.icon) }
                            .tag(NavTab.liveSession)

                        SettingsNavStack()
                            .tabItem { Label("Settings", systemImage: NavTab.settings.icon) }
                            .tag(NavTab.settings)
                    }
                    .accentColor(themeManager.currentTheme.primary)
                    .onAppear {
                        if networkMonitor.status == .available {
                            Task {
                                let sync: SyncOfflineChanges = get()
                                _ = try? await sync.invoke()
                            }
                        }
                    }
                }
            }
            .environmentObject(networkMonitor)
            .environmentObject(themeManager)
            .withAppTheme()
            .preferredColorScheme(.none) // Let system handle dark/light mode automatically
            .onAppear {
                updateNavigationBarAppearance()
            }
            .onReceive(themeManager.objectWillChange) { _ in
                updateNavigationBarAppearance()
            }
        }
    }
}

// MARK: - Navigation Stacks

struct HistoryNavStack: View {
    @EnvironmentObject private var themeManager: AppThemeManager

    var body: some View {
        NavigationStack {
            HistoryView()
                .navigationTitle("Sessions History")
                .navigationBarTitleDisplayMode(.inline)
                .toolbarBackground(themeManager.currentTheme.primary.opacity(0.85), for: .navigationBar)
                .toolbarBackground(.visible, for: .navigationBar)
                .toolbarColorScheme(.dark, for: .navigationBar)
        }
    }
}

struct LiveNavStack: View {
    var body: some View {
        NavigationStack {
            LiveSessionView()
                .navigationBarHidden(true)  // Full-bleed for map view
        }
    }
}

struct SettingsNavStack: View {
    @EnvironmentObject private var themeManager: AppThemeManager

    var body: some View {
        NavigationStack {
            SettingsView()
                .navigationTitle("Settings")
                .navigationBarTitleDisplayMode(.inline)
                .toolbarBackground(themeManager.currentTheme.primary.opacity(0.85), for: .navigationBar)
                .toolbarBackground(.visible, for: .navigationBar)
                .toolbarColorScheme(.dark, for: .navigationBar)
        }
    }
}

