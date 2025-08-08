import SwiftUI

/// Apple's best practice: Use named colors from Asset Catalog
/// Each theme has Light/Dark variants automatically handled by the system
enum AppTheme: String, CaseIterable {
    case classicRed = "ClassicRed"
    case oceanBlue = "OceanBlue"
    case forestGreen = "ForestGreen"
    case stormGray = "StormGray"
    case sunsetOrange = "SunsetOrange"

    var displayName: String {
        switch self {
        case .classicRed: return "Classic Red"
        case .oceanBlue: return "Ocean Blue"
        case .forestGreen: return "Forest Green"
        case .stormGray: return "Storm Gray"
        case .sunsetOrange: return "Sunset Orange"
        }
    }
}

/// Reactive theme manager using Apple's best practices
class AppThemeManager: ObservableObject {
    static let shared = AppThemeManager()

    @Published var selectedTheme: String = AppTheme.classicRed.rawValue {
        didSet {
            // Store the selected theme persistently
            UserDefaults.standard.set(selectedTheme, forKey: "selectedTheme")
        }
    }

    private init() {
        // Load the saved theme on startup
        selectedTheme = UserDefaults.standard.string(forKey: "selectedTheme") ?? AppTheme.classicRed.rawValue
    }

    var currentTheme: AppTheme {
        AppTheme(rawValue: selectedTheme) ?? .classicRed
    }

    func setTheme(_ theme: AppTheme) {
        selectedTheme = theme.rawValue
    }
}

/// Semantic color accessors using Asset Catalog naming with theme support
extension AppTheme {
    // Core colors - automatically respect Dark Mode and theme selection
    var primary: Color { Color("\(rawValue)/Primary", bundle: .main) }
    var onPrimary: Color { Color("\(rawValue)/OnPrimary", bundle: .main) }
    var primaryContainer: Color { Color("\(rawValue)/PrimaryContainer", bundle: .main) }
    var onPrimaryContainer: Color { Color("\(rawValue)/OnPrimaryContainer", bundle: .main) }

    var secondary: Color { Color("\(rawValue)/Secondary", bundle: .main) }
    var onSecondary: Color { Color("\(rawValue)/OnSecondary", bundle: .main) }
    var secondaryContainer: Color { Color("\(rawValue)/SecondaryContainer", bundle: .main) }
    var onSecondaryContainer: Color { Color("\(rawValue)/OnSecondaryContainer", bundle: .main) }

    // Surfaces & backgrounds
    var background: Color { Color("\(rawValue)/Background", bundle: .main) }
    var onBackground: Color { Color("\(rawValue)/OnBackground", bundle: .main) }
    var surface: Color { Color("\(rawValue)/Surface", bundle: .main) }
    var onSurface: Color { Color("\(rawValue)/OnSurface", bundle: .main) }
    var surfaceVariant: Color { Color("\(rawValue)/SurfaceVariant", bundle: .main) }
    var onSurfaceVariant: Color { Color("\(rawValue)/OnSurfaceVariant", bundle: .main) }
    var surfaceContainer: Color { Color("\(rawValue)/SurfaceContainer", bundle: .main) }
    var tertiaryContainer: Color { Color("\(rawValue)/TertiaryContainer", bundle: .main) }

    // Error states
    var error: Color { Color("\(rawValue)/Error", bundle: .main) }
    var onError: Color { Color("\(rawValue)/OnError", bundle: .main) }
    var errorContainer: Color { Color("\(rawValue)/ErrorContainer", bundle: .main) }
    var onErrorContainer: Color { Color("\(rawValue)/OnErrorContainer", bundle: .main) }

    // Utility colors
    var outline: Color { Color("\(rawValue)/Outline", bundle: .main) }
    var shadow: Color { Color("\(rawValue)/Shadow", bundle: .main) }
}

/// Environment key for reactive theme access
private struct AppThemeKey: EnvironmentKey {
    static let defaultValue = AppTheme.classicRed
}

extension EnvironmentValues {
    var appTheme: AppTheme {
        get { self[AppThemeKey.self] }
        set { self[AppThemeKey.self] = newValue }
    }
}

/// View modifier for easy theme injection that reacts to theme changes
extension View {
    func withAppTheme() -> some View {
        modifier(AppThemeModifier())
    }
}

/// Reactive theme modifier that updates when theme changes
struct AppThemeModifier: ViewModifier {
    @StateObject private var themeManager = AppThemeManager.shared

    func body(content: Content) -> some View {
        content
            .environment(\.appTheme, themeManager.currentTheme)
            .environmentObject(themeManager)
    }
}
