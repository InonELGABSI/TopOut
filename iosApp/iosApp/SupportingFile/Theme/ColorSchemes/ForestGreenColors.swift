import SwiftUI

struct ForestGreenLightColors: TopOutColorScheme {
    let primary = BrandColors.forestGreen
    let onPrimary = Color.white
    let primaryContainer = Color(red: 0.898, green: 0.957, blue: 0.941)      // #E5F4F0
    let onPrimaryContainer = BrandColors.forestGreen
    let secondary = BrandColors.earthBrown
    let onSecondary = Color.white
    let secondaryContainer = Color(red: 0.953, green: 0.925, blue: 0.886)    // #F3ECDC
    let onSecondaryContainer = BrandColors.earthBrown
    let background = BrandColors.snowWhite
    let onBackground = Color(red: 0.137, green: 0.137, blue: 0.137)
    let surface = Color(red: 0.992, green: 0.988, blue: 0.984)
    let onSurface = Color(red: 0.137, green: 0.137, blue: 0.137)
    let surfaceVariant = Color(red: 0.941, green: 0.957, blue: 0.949)        // #F0F4F2
    let onSurfaceVariant = Color(red: 0.239, green: 0.239, blue: 0.239)
    
    // --- ADD THESE ---
    let surfaceContainer = Color(red: 0.922, green: 0.973, blue: 0.961)      // #EBF8F5 (very light greenish)
    let tertiaryContainer = Color(red: 0.835, green: 0.925, blue: 0.800)     // #D5ECD0 (mint/greenish)
    // ------------------

    let error = Color(red: 0.827, green: 0.184, blue: 0.184)
    let onError = Color.white
    let errorContainer = Color(red: 1.0, green: 0.902, blue: 0.902)
    let onErrorContainer = Color(red: 0.827, green: 0.184, blue: 0.184)
    let outline = BrandColors.neutralOutline
    let shadow = Color.black.opacity(0.2)
}

struct ForestGreenDarkColors: TopOutColorScheme {
    let primary = Color(red: 0.533, green: 0.831, blue: 0.780)               // #88D4C7
    let onPrimary = Color(red: 0.0, green: 0.275, blue: 0.227)               // #00463A
    let primaryContainer = Color(red: 0.071, green: 0.392, blue: 0.325)      // #126454
    let onPrimaryContainer = Color(red: 0.898, green: 0.957, blue: 0.941)    // #E5F4F0
    let secondary = Color(red: 0.812, green: 0.725, blue: 0.624)             // #CFB99F
    let onSecondary = Color(red: 0.298, green: 0.196, blue: 0.098)           // #4C3219
    let secondaryContainer = Color(red: 0.384, green: 0.278, blue: 0.161)    // #62472A
    let onSecondaryContainer = Color(red: 0.953, green: 0.925, blue: 0.886)  // #F3ECDC
    let background = BrandColors.deepNight
    let onBackground = Color(red: 0.898, green: 0.898, blue: 0.898)
    let surface = Color(red: 0.067, green: 0.094, blue: 0.153)
    let onSurface = Color(red: 0.898, green: 0.898, blue: 0.898)
    let surfaceVariant = Color(red: 0.196, green: 0.333, blue: 0.275)        // #325546
    let onSurfaceVariant = Color(red: 0.706, green: 0.831, blue: 0.773)      // #B4D4C5
    
    // --- ADD THESE ---
    let surfaceContainer = Color(red: 0.129, green: 0.231, blue: 0.180)      // #214D2E (deep green)
    let tertiaryContainer = Color(red: 0.251, green: 0.388, blue: 0.286)     // #406348 (muted green)
    // ------------------

    let error = Color(red: 1.0, green: 0.733, blue: 0.733)
    let onError = Color(red: 0.408, green: 0.0, blue: 0.0)
    let errorContainer = Color(red: 0.627, green: 0.078, blue: 0.078)
    let onErrorContainer = Color(red: 1.0, green: 0.902, blue: 0.902)
    let outline = BrandColors.neutralOutline
    let shadow = Color.black.opacity(0.4)
}
