import SwiftUI

struct TopRoundedCard<Content: View>: View {
    let content: Content
    let backgroundColor: Color
    
    init(
        backgroundColor: Color = Color.white, // default for backwards compatibility
        @ViewBuilder content: () -> Content
    ) {
        self.backgroundColor = backgroundColor
        self.content = content()
    }
    
    var body: some View {
        content
            .padding()
            .background(backgroundColor)
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

struct BottomRoundedCard<Content: View>: View {
    let content: Content
    
    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
            .padding()
            .background(theme.surface)
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
    
    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
            .padding()
            .background(theme.surface)
            .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

struct FullRoundedCard<Content: View>: View {
    let content: Content
    
    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
            .padding()
            .background(theme.surface)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}
