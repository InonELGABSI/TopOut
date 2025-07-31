import SwiftUI
import Combine   // (only if you use Combine utilities elsewhere)

/**
 A single source of truth for the theme.
 Values are persisted with `ThemePreferences`, exposed
 to SwiftUI via an `EnvironmentKey`, and mutated only through
 `update(...)`.
 */
final class ThemeManager: ObservableObject {
    
    // MARK: Singleton
    static let shared = ThemeManager()
    
    // MARK: Published state
    @Published private(set) var palette: ThemePalette
    @Published private(set) var isDarkMode: Bool
    
    // MARK: Storage
    private let prefs = ThemePreferences()
    private init() {
        palette    = prefs.getThemePalette()
        isDarkMode = prefs.getDarkMode(fallback: false)
    }
    
    // MARK: Value injected into the view hierarchy
    var current: TopOutTheme {
        TopOutTheme(
            palette: palette,
            colorScheme: isDarkMode ? .dark : .light
        )
    }
    
    // MARK: Mutation (e.g. from Settings screen)
    func update(palette newPalette: ThemePalette, darkMode: Bool) {
        self.palette    = newPalette          // ✅ property assignment
        self.isDarkMode = darkMode            // ✅ property assignment
        
        prefs.saveThemePalette(newPalette)
        prefs.saveDarkMode(darkMode)
    }
}
