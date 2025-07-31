import SwiftUI

/// Run–time theme “snapshot” injected through the SwiftUI Environment.
struct TopOutTheme {
    let palette: ThemePalette
    let colorScheme: ColorScheme             // .light / .dark
    
    /// Resolved Material-like colour set for the current palette + mode.
    var colors: TopOutColorScheme {
        palette.scheme(for: colorScheme)
    }
}

// MARK: - Environment plumbing
private struct TopOutThemeKey: EnvironmentKey {
    static let defaultValue = TopOutTheme(
        palette: .classicRed,
        colorScheme: .light
    )
}

extension EnvironmentValues {
    var topOutTheme: TopOutTheme {
        get { self[TopOutThemeKey.self] }
        set { self[TopOutThemeKey.self] = newValue }
    }
}

/// Convenience for injecting a theme into any subtree.
extension View {
    func topOutTheme(_ theme: TopOutTheme) -> some View {
        environment(\.topOutTheme, theme)
    }
}
