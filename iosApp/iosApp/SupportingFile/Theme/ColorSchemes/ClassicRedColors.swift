import SwiftUI

struct ClassicRedLightColors: TopOutColorScheme {
    let primary = BrandColors.climbingRed
    let onPrimary = Color.white
    let primaryContainer = Color(red: 1.0, green: 0.902, blue: 0.902)        // #FFE6E6
    let onPrimaryContainer = BrandColors.climbingRed
    let secondary = BrandColors.rockGray
    let onSecondary = Color.white
    let secondaryContainer = Color(red: 0.906, green: 0.914, blue: 0.929)    // #E7E9ED
    let onSecondaryContainer = BrandColors.rockGray
    let background = BrandColors.snowWhite
    let onBackground = Color(red: 0.137, green: 0.137, blue: 0.137)          // #232323
    let surface = Color(red: 0.992, green: 0.988, blue: 0.984)               // #FDFCFB
    let onSurface = Color(red: 0.137, green: 0.137, blue: 0.137)
    let surfaceVariant = Color(red: 0.953, green: 0.945, blue: 0.949)        // #F3F1F2
    let onSurfaceVariant = Color(red: 0.239, green: 0.239, blue: 0.239)      // #3D3D3D

    // --- ADD THESE ---
    let surfaceContainer = Color(red: 1.0, green: 0.941, blue: 0.941)        // #FFF0F0 (very light red tint)
    let tertiaryContainer = Color(red: 1.0, green: 0.871, blue: 0.847)       // #FFE0D8 (warm neutral for tertiary)
    // ------------------

    let error = Color(red: 0.827, green: 0.184, blue: 0.184)                 // #D32F2F
    let onError = Color.white
    let errorContainer = Color(red: 1.0, green: 0.902, blue: 0.902)          // #FFE6E6
    let onErrorContainer = Color(red: 0.827, green: 0.184, blue: 0.184)
    let outline = BrandColors.neutralOutline
    let shadow = Color.black.opacity(0.2)
}

struct ClassicRedDarkColors: TopOutColorScheme {
    let primary = Color(red: 1.0, green: 0.733, blue: 0.733)                 // #FFBBBB
    let onPrimary = Color(red: 0.408, green: 0.0, blue: 0.0)                 // #680000
    let primaryContainer = Color(red: 0.627, green: 0.078, blue: 0.078)      // #A01414
    let onPrimaryContainer = Color(red: 1.0, green: 0.902, blue: 0.902)      // #FFE6E6
    let secondary = Color(red: 0.729, green: 0.745, blue: 0.773)             // #BABEc5
    let onSecondary = Color(red: 0.196, green: 0.216, blue: 0.247)           // #323E3F
    let secondaryContainer = Color(red: 0.275, green: 0.298, blue: 0.341)    // #464C57
    let onSecondaryContainer = Color(red: 0.906, green: 0.914, blue: 0.929)  // #E7E9ED
    let background = BrandColors.deepNight
    let onBackground = Color(red: 0.898, green: 0.898, blue: 0.898)          // #E5E5E5
    let surface = Color(red: 0.067, green: 0.094, blue: 0.153)               // #111827
    let onSurface = Color(red: 0.898, green: 0.898, blue: 0.898)
    let surfaceVariant = Color(red: 0.275, green: 0.298, blue: 0.341)        // #464C57
    let onSurfaceVariant = Color(red: 0.729, green: 0.745, blue: 0.773)      // #BABEc5

    // --- ADD THESE ---
    let surfaceContainer = Color(red: 0.251, green: 0.129, blue: 0.129)      // #402121 (deep muted red)
    let tertiaryContainer = Color(red: 0.333, green: 0.212, blue: 0.192)     // #553631 (soft brownish, for tertiary)
    // ------------------

    let error = Color(red: 1.0, green: 0.733, blue: 0.733)                   // #FFBBBB
    let onError = Color(red: 0.408, green: 0.0, blue: 0.0)                   // #680000
    let errorContainer = Color(red: 0.627, green: 0.078, blue: 0.078)        // #A01414
    let onErrorContainer = Color(red: 1.0, green: 0.902, blue: 0.902)        // #FFE6E6
    let outline = BrandColors.neutralOutline
    let shadow = Color.black.opacity(0.4)
}
