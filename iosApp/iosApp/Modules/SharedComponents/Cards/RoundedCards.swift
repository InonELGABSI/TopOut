import SwiftUI

struct TopRoundedCard<Content: View>: View {
    let content: Content
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
            .padding()
            .background(colors.surface)
            .clipShape(
                UnevenRoundedRectangle(
                    topLeadingRadius: 16,
                    bottomLeadingRadius: 0,
                    bottomTrailingRadius: 0,
                    topTrailingRadius: 16
                )
            )
            .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

struct BottomRoundedCard<Content: View>: View {
    let content: Content
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
            .padding()
            .background(colors.surface)
            .clipShape(
                UnevenRoundedRectangle(
                    topLeadingRadius: 0,
                    bottomLeadingRadius: 16,
                    bottomTrailingRadius: 16,
                    topTrailingRadius: 0
                )
            )
            .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

struct MiddleRoundedCard<Content: View>: View {
    let content: Content
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
            .padding()
            .background(colors.surface)
            .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

struct FullRoundedCard<Content: View>: View {
    let content: Content
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
            .padding()
            .background(colors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}
