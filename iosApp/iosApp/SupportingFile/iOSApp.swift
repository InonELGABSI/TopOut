import SwiftUI
import UIKit
import Shared
import Firebase

// MARK: - Gradient Text Helper
extension UIColor {
    static func gradientColor(from startColor: UIColor, to endColor: UIColor, size: CGSize) -> UIColor {
        let renderer = UIGraphicsImageRenderer(size: size)
        let gradientImage = renderer.image { context in
            let gradient = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(),
                                    colors: [startColor.cgColor, endColor.cgColor] as CFArray,
                                    locations: [0.0, 1.0])!
            // Make gradient more visible with diagonal direction
            context.cgContext.drawLinearGradient(gradient,
                                               start: CGPoint(x: 0, y: 0),
                                               end: CGPoint(x: size.width, y: size.height),
                                               options: [])
        }
        return UIColor(patternImage: gradientImage)
    }
}

// MARK: - Appearance builder (Apple-native fade, consistent blur)
extension UINavigationBarAppearance {
    static func translucentThemed(primary: UIColor) -> (standard: UINavigationBarAppearance, scroll: UINavigationBarAppearance) {
        func make() -> UINavigationBarAppearance {
            let a = UINavigationBarAppearance()
            a.configureWithTransparentBackground()
            a.backgroundEffect = UIBlurEffect(style: .systemUltraThinMaterial)
            // Increase alpha for more vivid color while keeping translucency
            a.backgroundColor = primary.withAlphaComponent(0.1)

            // Create more dramatic gradient colors for better visibility
            let lightPrimary = primary.withAlphaComponent(0.4)  // Much lighter
            let darkPrimary = primary.withAlphaComponent(1.0)   // Full opacity
            let gradientColor = UIColor.gradientColor(from: darkPrimary, to: lightPrimary, size: CGSize(width: 300, height: 50))

            // Use gradient color for title text
            a.titleTextAttributes      = [.foregroundColor: gradientColor]
            a.largeTitleTextAttributes = [.foregroundColor: gradientColor]

            let buttons = UIBarButtonItemAppearance(style: .plain)
            buttons.normal.titleTextAttributes      = [.foregroundColor: primary]
            buttons.highlighted.titleTextAttributes = [.foregroundColor: primary]
            a.buttonAppearance     = buttons
            a.backButtonAppearance = buttons
            a.shadowColor = nil
            return a
        }

        let appearance = make()
        return (standard: appearance, scroll: appearance)
    }
}


// MARK: - Bottom-bar tabs
enum NavTab: String, CaseIterable, Identifiable {
    case history, liveSession, settings
    var id: String { rawValue }
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
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        FirebaseApp.configure()

        let (std, edge) = UINavigationBarAppearance.translucentThemed(primary: .systemBlue)
        let nav = UINavigationBar.appearance()
        nav.standardAppearance   = std
        nav.compactAppearance    = std
        nav.scrollEdgeAppearance = edge
        nav.tintColor            = .label   // bar button icons/tint
        return true
    }
}

// MARK: - App
@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var delegate
    @StateObject private var themeManager = AppThemeManager.shared
    @StateObject private var networkMonitor = NetworkMonitor()

    @State private var selectedTab: NavTab = .liveSession
    @State private var isAppLoading = true

    init() {
        KoinKt.doInitKoin()
        Task { let ensureAnon: EnsureAnonymousUser = get(); _ = try? await ensureAnon.invoke() }
    }

    private func applyThemedNavBar() {
        let primary = UIColor(themeManager.currentTheme.primary)
        let (std, edge) = UINavigationBarAppearance.translucentThemed(primary: primary)
        let nav = UINavigationBar.appearance()
        nav.standardAppearance   = std
        nav.compactAppearance    = std
        nav.scrollEdgeAppearance = edge
        nav.tintColor            = .label

        // Force immediate update of all existing navigation bars
        DispatchQueue.main.async {
            UIApplication.shared.connectedScenes
                .compactMap { $0 as? UIWindowScene }
                .flatMap { $0.windows }
                .forEach { window in
                    self.updateNavigationBarsInView(window.rootViewController)
                }
        }
    }

    private func updateNavigationBarsInView(_ viewController: UIViewController?) {
        guard let viewController = viewController else { return }

        if let navController = viewController as? UINavigationController {
            // Get the new themed appearance
            let primary = UIColor(themeManager.currentTheme.primary)
            let (std, edge) = UINavigationBarAppearance.translucentThemed(primary: primary)

            // Apply the new appearance immediately
            navController.navigationBar.standardAppearance = std
            navController.navigationBar.scrollEdgeAppearance = edge
            navController.navigationBar.compactAppearance = std

            // Force the navigation bar to refresh
            navController.navigationBar.setNeedsLayout()
            navController.navigationBar.layoutIfNeeded()

        } else if let tabController = viewController as? UITabBarController {
            tabController.viewControllers?.forEach { updateNavigationBarsInView($0) }
        }

        // Also check child view controllers
        viewController.children.forEach { updateNavigationBarsInView($0) }
    }

    var body: some Scene {
        WindowGroup {
            Group {
                if isAppLoading {
                    LoadingAnimation(text: "Welcome to TopOut")
                        .onAppear { DispatchQueue.main.asyncAfter(deadline: .now() + 2) { isAppLoading = false } }
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
                    .tint(themeManager.currentTheme.primary)
                    .onAppear {
                        if networkMonitor.status == .available {
                            Task { let sync: SyncOfflineChanges = get(); _ = try? await sync.invoke() }
                        }
                    }
                }
            }
            .environmentObject(themeManager)
            .environmentObject(networkMonitor)
            .withAppTheme()
            .onAppear { applyThemedNavBar() }
            .onReceive(themeManager.objectWillChange) { _ in applyThemedNavBar() }
        }
    }
}

// MARK: - Navigation Stacks (Apple's modern approach)
struct HistoryNavStack: View {
    @EnvironmentObject private var themeManager: AppThemeManager

    var body: some View {
        NavigationStack {
            HistoryView()
                .navigationTitle("Sessions History")
                .navigationBarTitleDisplayMode(.large)
        }
        .background(themeManager.currentTheme.primary.opacity(0.05))
    }
}

struct LiveNavStack: View {
    var body: some View {
        NavigationStack {
            LiveSessionView()
                .toolbar(.hidden, for: .navigationBar)
        }
    }
}

struct SettingsNavStack: View {
    @EnvironmentObject private var themeManager: AppThemeManager

    var body: some View {
        NavigationStack {
            SettingsView()
                .navigationTitle("Settings")
                .navigationBarTitleDisplayMode(.large)
        }
        .background(themeManager.currentTheme.primary.opacity(0.05))
    }
}
