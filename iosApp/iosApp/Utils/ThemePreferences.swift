import Foundation
import Shared
import UIKit

final class ThemePreferences {
    private static let userDefaults = UserDefaults.standard
    
    private enum Keys {
        static let themePalette = "theme_palette"
        static let isDarkMode = "is_dark_mode"
    }
    
    // MARK: - Theme Palette
    
    static func getThemePalette() -> ThemePalette {
        let paletteString = userDefaults.string(forKey: Keys.themePalette) ?? ThemePalette.classicRed.rawValue
        return ThemePalette(rawValue: paletteString) ?? .classicRed
    }
    
    static func saveThemePalette(_ palette: ThemePalette) {
        userDefaults.set(palette.rawValue, forKey: Keys.themePalette)
    }
    
    // MARK: - Dark Mode
    
    static func hasDarkModePreference() -> Bool {
        userDefaults.object(forKey: Keys.isDarkMode) != nil
    }
    
    /// Returns the user's dark mode preference, or system value if not set.
    static func getDarkMode(fallback: Bool? = nil) -> Bool {
        if hasDarkModePreference() {
            return userDefaults.bool(forKey: Keys.isDarkMode)
        } else if let fallback = fallback {
            return fallback
        } else {
            // Use the current trait collection as the system fallback
            return UITraitCollection.current.userInterfaceStyle == .dark
        }
    }
    
    static func saveDarkMode(_ isDark: Bool) {
        userDefaults.set(isDark, forKey: Keys.isDarkMode)
    }
}
