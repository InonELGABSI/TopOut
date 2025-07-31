import SwiftUI

struct ChipControlBar: View {
    let title: String
    var showBackButton: Bool = false
    var onBackClick: () -> Void = {}
    var isTransparent: Bool = false
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    var body: some View {
        VStack {
            // Add safe area top padding
            Spacer()
                .frame(height: 0)
                .background(Color.clear)
            
            ZStack {
                // Gradient background
                RoundedRectangle(cornerRadius: 24)
                    .fill(
                        isTransparent ?
                        backgroundGradient(colorBase: colors.primary.opacity(0.8)) :
                        backgroundGradient(colorBase: colors.primaryContainer)
                    )
                
                HStack {
                    // Back button
                    if showBackButton {
                        Button(action: onBackClick) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 20, weight: .semibold))
                                .foregroundColor(isTransparent ?
                                                colors.onSurface :
                                                colors.onPrimaryContainer)
                                .frame(width: 40, height: 40)
                        }
                        .offset(x: 4)
                    }
                    
                    // Title
                    Text(title)
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(titleColor())
                        .padding(.leading, showBackButton ? 32 : 0)
                        .frame(maxWidth: .infinity, alignment: .center)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .padding(.horizontal, 32)
            .padding(.vertical, 16)
        }
    }
    
    private func backgroundGradient(colorBase: Color) -> LinearGradient {
        let lighterColor = colorBase.adjust(brightness: 0.1)
        let darkerColor = colorBase.adjust(brightness: -0.1)
        
        return LinearGradient(
            colors: [lighterColor, colorBase, darkerColor],
            startPoint: .top,
            endPoint: .bottom
        )
    }
    
    private func titleColor() -> Color {
        if isTransparent {
            // Much darker version of primary color for transparent mode
            return colors.primary.adjust(brightness: -0.7)
        } else {
            // Much darker version of primary container color for normal mode
            return colors.primaryContainer.adjust(brightness: -0.7)
        }
    }
}

// Extension to adjust color brightness
extension Color {
    func adjust(brightness: CGFloat) -> Color {
        let uiColor = UIColor(self)
        var hue: CGFloat = 0
        var saturation: CGFloat = 0
        var brightness: CGFloat = 0
        var alpha: CGFloat = 0
        
        uiColor.getHue(&hue, saturation: &saturation, brightness: &brightness, alpha: &alpha)
        
        let newBrightness = max(0, min(1, brightness + brightness))
        
        return Color(UIColor(hue: hue, saturation: saturation, brightness: newBrightness, alpha: alpha))
    }
}
