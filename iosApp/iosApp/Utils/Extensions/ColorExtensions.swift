import SwiftUI
import Shared

extension Color {
    // Hex color initializer
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }
        
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
    
    // Initialize from BrandColors Kotlin object
    init(_ kotlinColor: Any) {
        // This will be resolved by the BrandColorsKt integration
        // For now, provide a fallback
        self = Color.primary
    }
    
    // Convert to hex string
    func toHex() -> String {
        let components = UIColor(self).cgColor.components ?? [0, 0, 0, 1]
        let r = Int(components[0] * 255)
        let g = Int(components[1] * 255)
        let b = Int(components[2] * 255)
        return String(format: "#%02X%02X%02X", r, g, b)
    }
    
    // Lighten color
    func lighter(by percentage: CGFloat = 0.2) -> Color {
        let uiColor = UIColor(self)
        var hue: CGFloat = 0
        var saturation: CGFloat = 0
        var brightness: CGFloat = 0
        var alpha: CGFloat = 0
        
        uiColor.getHue(&hue, saturation: &saturation, brightness: &brightness, alpha: &alpha)
        
        return Color(UIColor(hue: hue,
                            saturation: saturation,
                            brightness: min(brightness + percentage, 1.0),
                            alpha: alpha))
    }
    
    // Darken color
    func darker(by percentage: CGFloat = 0.2) -> Color {
        let uiColor = UIColor(self)
        var hue: CGFloat = 0
        var saturation: CGFloat = 0
        var brightness: CGFloat = 0
        var alpha: CGFloat = 0
        
        uiColor.getHue(&hue, saturation: &saturation, brightness: &brightness, alpha: &alpha)
        
        return Color(UIColor(hue: hue,
                            saturation: saturation,
                            brightness: max(brightness - percentage, 0.0),
                            alpha: alpha))
    }
    
    // Blend colors for animation effects
    func blended(with color: Color, ratio: Double) -> Color {
        let ratio = max(0, min(1, ratio))
        return Color(
            red: (1 - ratio) * self.components.red + ratio * color.components.red,
            green: (1 - ratio) * self.components.green + ratio * color.components.green,
            blue: (1 - ratio) * self.components.blue + ratio * color.components.blue,
            opacity: (1 - ratio) * self.components.opacity + ratio * color.components.opacity
        )
    }
    
    var components: (red: Double, green: Double, blue: Double, opacity: Double) {
        let uiColor = UIColor(self)
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        
        uiColor.getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        
        return (Double(red), Double(green), Double(blue), Double(alpha))
    }
}
