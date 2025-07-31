import SwiftUI

struct BrandColors {
    static let climbingRed = Color(red: 0.871, green: 0.169, blue: 0.169)    // #DE2B2B
    static let mountainBlue = Color(red: 0.084, green: 0.396, blue: 0.753)   // #1565C0
    static let forestGreen = Color(red: 0.149, green: 0.514, blue: 0.431)    // #26836E
    static let rockGray = Color(red: 0.361, green: 0.388, blue: 0.439)       // #5C6370
    static let skyBlue = Color(red: 0.267, green: 0.647, blue: 0.878)        // #44A5E0
    static let earthBrown = Color(red: 0.486, green: 0.369, blue: 0.235)     // #7C5E3C
    static let snowWhite = Color(red: 0.992, green: 0.988, blue: 0.984)      // #FDFCFB
    static let deepNight = Color(red: 0.067, green: 0.094, blue: 0.153)      // #111827
    static let neutralOutline = Color(red: 0.55, green: 0.55, blue: 0.55)  // 45 % Grey

}

enum ThemePalette: String, CaseIterable {
    case classicRed = "CLASSIC_RED"
    case oceanBlue = "OCEAN_BLUE"
    case forestGreen = "FOREST_GREEN"
    case stormGray = "STORM_GRAY"
    case sunsetOrange = "SUNSET_ORANGE"
    
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

// MARK: - Palette → Scheme resolver
extension ThemePalette {
    /// Maps a palette + colourScheme to its concrete struct.
    func scheme(for colorScheme: ColorScheme) -> TopOutColorScheme {
        switch (self, colorScheme) {
        case (.classicRed, .light):   return ClassicRedLightColors()
        case (.classicRed, .dark):    return ClassicRedDarkColors()
        case (.oceanBlue, .light):    return OceanBlueLightColors()
        case (.oceanBlue, .dark):     return OceanBlueDarkColors()
        case (.forestGreen, .light):  return ForestGreenLightColors()
        case (.forestGreen, .dark):   return ForestGreenDarkColors()
        case (.stormGray, .light):    return StormGrayLightColors()
        case (.stormGray, .dark):     return StormGrayDarkColors()
        case (.sunsetOrange, .light): return SunsetOrangeLightColors()
        case (.sunsetOrange, .dark):  return SunsetOrangeDarkColors()
        @unknown default:
            // Fallback to classic red as default
            return colorScheme == .light ? ClassicRedLightColors() : ClassicRedDarkColors()
        }
    }
}
