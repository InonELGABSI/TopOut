import SwiftUI

struct StormGrayLightColors: TopOutColorScheme {
    let primary = BrandColors.rockGray
    let onPrimary = Color.white
    let primaryContainer = Color(red: 0.918, green: 0.925, blue: 0.937)      // #EAECEF
    let onPrimaryContainer = BrandColors.rockGray
    let secondary = Color(red: 0.525, green: 0.565, blue: 0.631)             // #869099
    let onSecondary = Color.white
    let secondaryContainer = Color(red: 0.906, green: 0.918, blue: 0.933)    // #E7EAEE
    let onSecondaryContainer = Color(red: 0.525, green: 0.565, blue: 0.631)
    let background = BrandColors.snowWhite
    let onBackground = Color(red: 0.137, green: 0.137, blue: 0.137)
    let surface = Color(red: 0.992, green: 0.988, blue: 0.984)
    let onSurface = Color(red: 0.137, green: 0.137, blue: 0.137)
    let surfaceVariant = Color(red: 0.945, green: 0.949, blue: 0.957)        // #F1F2F4
    let onSurfaceVariant = Color(red: 0.239, green: 0.239, blue: 0.239)
    let error = Color(red: 0.827, green: 0.184, blue: 0.184)
    let onError = Color.white
    let errorContainer = Color(red: 1.0, green: 0.902, blue: 0.902)
    let onErrorContainer = Color(red: 0.827, green: 0.184, blue: 0.184)
    
    // Extras required by the protocol
    let outline = BrandColors.neutralOutline
    let shadow = Color.black.opacity(0.2)
}

struct StormGrayDarkColors: TopOutColorScheme {
    let primary = Color(red: 0.761, green: 0.784, blue: 0.816)               // #C2C8D0
    let onPrimary = Color(red: 0.165, green: 0.192, blue: 0.235)             // #2A313C
    let primaryContainer = Color(red: 0.259, green: 0.290, blue: 0.337)      // #424A56
    let onPrimaryContainer = Color(red: 0.918, green: 0.925, blue: 0.937)    // #EAECEF
    let secondary = Color(red: 0.729, green: 0.757, blue: 0.796)             // #BAC1CB
    let onSecondary = Color(red: 0.196, green: 0.235, blue: 0.282)           // #323C48
    let secondaryContainer = Color(red: 0.357, green: 0.396, blue: 0.451)    // #5B6573
    let onSecondaryContainer = Color(red: 0.906, green: 0.918, blue: 0.933)  // #E7EAEE
    let background = BrandColors.deepNight
    let onBackground = Color(red: 0.898, green: 0.898, blue: 0.898)
    let surface = Color(red: 0.067, green: 0.094, blue: 0.153)
    let onSurface = Color(red: 0.898, green: 0.898, blue: 0.898)
    let surfaceVariant = Color(red: 0.259, green: 0.290, blue: 0.337)        // #424A56
    let onSurfaceVariant = Color(red: 0.729, green: 0.757, blue: 0.796)      // #BAC1CB
    let error = Color(red: 1.0, green: 0.733, blue: 0.733)
    let onError = Color(red: 0.408, green: 0.0, blue: 0.0)
    let errorContainer = Color(red: 0.627, green: 0.078, blue: 0.078)
    let onErrorContainer = Color(red: 1.0, green: 0.902, blue: 0.902)
    
    // Extras required by the protocol
    let outline = BrandColors.neutralOutline
    let shadow = Color.black.opacity(0.4)
}
