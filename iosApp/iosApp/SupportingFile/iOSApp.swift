import SwiftUI
import UIKit
import Shared
import Firebase
import UserNotifications

// MARK: - Notification Best Practices Manager
enum NotificationManager {
    static func registerCategories() {
        let generalCategory = UNNotificationCategory(
            identifier: "topout_general",
            actions: [],
            intentIdentifiers: [],
            options: .customDismissAction
        )

        let alertHeightCategory = UNNotificationCategory(
            identifier: "alert_height_warning",
            actions: [
                .init(identifier: "action_ok", title: "OK", options: .foreground)
            ],
            intentIdentifiers: [],
            options: .customDismissAction
        )

        // Add categories for different alert types
        let alertSpeedCategory = UNNotificationCategory(
            identifier: "alert_speed_warning",
            actions: [
                .init(identifier: "action_ok", title: "OK", options: .foreground)
            ],
            intentIdentifiers: [],
            options: .customDismissAction
        )

        UNUserNotificationCenter.current().setNotificationCategories([
            generalCategory,
            alertHeightCategory,
            alertSpeedCategory
        ])
        NSLog("[Notif] Registered categories")
    }
}

// MARK: - Gradient Text Helper
extension UIColor {
    static func gradientColor(from startColor: UIColor, to endColor: UIColor, size: CGSize) -> UIColor {
        let renderer = UIGraphicsImageRenderer(size: size)
        let gradientImage = renderer.image { context in
            let gradient = CGGradient(
                colorsSpace: CGColorSpaceCreateDeviceRGB(),
                colors: [startColor.cgColor, endColor.cgColor] as CFArray,
                locations: [0.0, 1.0]
            )!
            context.cgContext.drawLinearGradient(
                gradient,
                start: CGPoint(x: 0, y: 0),
                end: CGPoint(x: size.width, y: size.height),
                options: []
            )
        }
        return UIColor(patternImage: gradientImage)
    }
}

// MARK: - Appearance builder (blur + translucent + gradient titles)
extension UINavigationBarAppearance {
    static func translucentThemed(primary: UIColor) -> UINavigationBarAppearance {
        let a = UINavigationBarAppearance()
        a.configureWithTransparentBackground()
        a.backgroundEffect = UIBlurEffect(style: .systemUltraThinMaterial)
        a.backgroundColor  = primary.withAlphaComponent(0.10)

        let grad = UIColor.gradientColor(
            from: primary.withAlphaComponent(1.0),
            to:   primary.withAlphaComponent(0.4),
            size: .init(width: 300, height: 52)
        )
        a.largeTitleTextAttributes = [.foregroundColor: grad]
        a.titleTextAttributes      = [.foregroundColor: grad]
        a.shadowColor = nil
        return a
    }
}

// MARK: - Centralized theming helper
enum NavBarThemer {
    static func apply(primary: UIColor) {
        let themed = UINavigationBarAppearance.translucentThemed(primary: primary)
        
        // Apply to global appearance (for new navigation controllers)
        let nav = UINavigationBar.appearance()
        nav.standardAppearance           = themed
        nav.compactAppearance            = themed
        nav.scrollEdgeAppearance         = themed
        nav.compactScrollEdgeAppearance  = themed
        nav.tintColor = .label
        
        // Apply to all existing navigation controllers immediately
        applyToExistingNavigationControllers(appearance: themed)
    }
    
    /// Apple's best practice: Update all active navigation controllers immediately
    private static func applyToExistingNavigationControllers(appearance: UINavigationBarAppearance) {
        DispatchQueue.main.async {
            guard let windowScene = UIApplication.shared.connectedScenes
                .compactMap({ $0 as? UIWindowScene })
                .first else { return }
            
            for window in windowScene.windows {
                updateNavigationControllers(in: window.rootViewController, with: appearance)
            }
        }
    }
    
    /// Recursively find and update all navigation controllers
    private static func updateNavigationControllers(in viewController: UIViewController?, with appearance: UINavigationBarAppearance) {
        guard let viewController = viewController else { return }
        
        if let navigationController = viewController as? UINavigationController {
            // Apply the new appearance immediately
            navigationController.navigationBar.standardAppearance = appearance
            navigationController.navigationBar.compactAppearance = appearance
            navigationController.navigationBar.scrollEdgeAppearance = appearance
            if #available(iOS 15.0, *) {
                navigationController.navigationBar.compactScrollEdgeAppearance = appearance
            }
            
            // Force the navigation bar to update its appearance
            navigationController.navigationBar.setNeedsLayout()
        }
        
        // Check child view controllers
        for child in viewController.children {
            updateNavigationControllers(in: child, with: appearance)
        }
        
        // Check presented view controllers
        if let presented = viewController.presentedViewController {
            updateNavigationControllers(in: presented, with: appearance)
        }
        
        // For tab bar controllers, check all tabs
        if let tabBarController = viewController as? UITabBarController {
            for tabViewController in tabBarController.viewControllers ?? [] {
                updateNavigationControllers(in: tabViewController, with: appearance)
            }
        }
    }
}

// MARK: - UIApplicationDelegate
final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        FirebaseApp.configure()

        let center = UNUserNotificationCenter.current()
        center.delegate = self

        NavBarThemer.apply(primary: .systemBlue)

        // Best Practice: Register categories and request permission
        NotificationManager.registerCategories()
        requestNotificationPermissionIfNeeded()

        return true
    }

    private func requestNotificationPermissionIfNeeded() {
        let center = UNUserNotificationCenter.current()
        center.getNotificationSettings { [weak self] settings in
            guard let self = self else { return }

            switch settings.authorizationStatus {
            case .notDetermined:
                center.requestAuthorization(options: [.alert, .sound, .badge]) { [weak self] granted, error in
                    guard let self = self else { return }
                    NSLog("[Notif] Permission request result: granted=\(granted), error=\(error?.localizedDescription ?? "none")")
                    if granted {
                        #if DEBUG
                        DispatchQueue.main.async {
                            self.scheduleDebugTestNotification(reason: "Permission just granted")
                        }
                        #endif
                    }
                }
            case .denied:
                NSLog("[Notif] Permission was denied. User must enable in Settings.")
            case .authorized, .provisional, .ephemeral:
                NSLog("[Notif] Permission already authorized.")
                #if DEBUG
                DispatchQueue.main.async {
                    self.scheduleDebugTestNotification(reason: "Already authorized on launch")
                }
                #endif
            @unknown default:
                break
            }
        }
    }

    #if DEBUG
    private func scheduleDebugTestNotification(reason: String) {
        let content = UNMutableNotificationContent()
        content.title = "TopOut Ready"
        content.body = "Local notifications are working. (\(reason))"
        content.sound = .default
        content.categoryIdentifier = "topout_general"
        let request = UNNotificationRequest(identifier: "debug_init_\(UUID())", content: content, trigger: nil)
        UNUserNotificationCenter.current().add(request) { err in
            if let err = err { NSLog("[Notif] Failed debug schedule: %@", err.localizedDescription) }
            else { NSLog("[Notif] Debug notification scheduled") }
        }
    }
    #endif

    // Show banners & sounds while app in foreground
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .sound, .list])
    }

    // Handle notification tap/action
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let identifier = response.actionIdentifier
        let notificationId = response.notification.request.identifier

        NSLog("[Notif] User interaction: action=\(identifier) notification=\(notificationId)")

        // Handle specific actions if needed
        switch identifier {
        case "action_ok":
            // User acknowledged the alert
            break
        case UNNotificationDefaultActionIdentifier:
            // User tapped the notification (not an action button)
            break
        default:
            break
        }

        completionHandler()
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

    var body: some Scene {
        WindowGroup {
            Group {
                if isAppLoading {
                    LoadingAnimation(text: "Welcome to TopOut")
                        .onAppear {
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2) { isAppLoading = false }
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
            // Apply themed nav bar once the theme is known, and whenever it changes.
            .onAppear {
                NavBarThemer.apply(primary: UIColor(themeManager.currentTheme.primary))
            }
            .onChange(of: themeManager.currentTheme.primary) { _, newPrimary in
                NavBarThemer.apply(primary: UIColor(newPrimary))
            }
        }
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

// MARK: - Navigation Stacks
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
