import SwiftUI

struct SunsetOrangeLightColors: TopOutColorScheme {
    let primary = Color(red: 1.0, green: 0.596, blue: 0.0)                   // #FF9800
    let onPrimary = Color.white
    let primaryContainer = Color(red: 1.0, green: 0.953, blue: 0.898)        // #FFF3E5
    let onPrimaryContainer = Color(red: 1.0, green: 0.596, blue: 0.0)
    let secondary = Color(red: 0.612, green: 0.153, blue: 0.690)             // #9C27B0
    let onSecondary = Color.white
    let secondaryContainer = Color(red: 0.953, green: 0.898, blue: 0.965)    // #F3E5F6
    let onSecondaryContainer = Color(red: 0.612, green: 0.153, blue: 0.690)
    let background = BrandColors.snowWhite
    let onBackground = Color(red: 0.137, green: 0.137, blue: 0.137)
    let surface = Color(red: 0.992, green: 0.988, blue: 0.984)
    let onSurface = Color(red: 0.137, green: 0.137, blue: 0.137)
    let surfaceVariant = Color(red: 1.0, green: 0.973, blue: 0.941)          // #FFF8F0
    let onSurfaceVariant = Color(red: 0.239, green: 0.239, blue: 0.239)
    let error = Color(red: 0.827, green: 0.184, blue: 0.184)
    let onError = Color.white
    let errorContainer = Color(red: 1.0, green: 0.902, blue: 0.902)
    let onErrorContainer = Color(red: 0.827, green: 0.184, blue: 0.184)
    
    // Extras required by the protocol
    let outline = BrandColors.neutralOutline
    let shadow = Color.black.opacity(0.2)
}

struct SunsetOrangeDarkColors: TopOutColorScheme {
    let primary = Color(red: 1.0, green: 0.741, blue: 0.533)                 // #FFBD88
    let onPrimary = Color(red: 0.533, green: 0.216, blue: 0.0)               // #883700
    let primaryContainer = Color(red: 0.800, green: 0.400, blue: 0.0)        // #CC6600
    let onPrimaryContainer = Color(red: 1.0, green: 0.953, blue: 0.898)      // #FFF3E5
    let secondary = Color(red: 0.831, green: 0.533, blue: 0.890)             // #D488E3
    let onSecondary = Color(red: 0.373, green: 0.0, blue: 0.427)             // #5F006D
    let secondaryContainer = Color(red: 0.490, green: 0.075, blue: 0.557)    // #7D138E
    let onSecondaryContainer = Color(red: 0.953, green: 0.898, blue: 0.965)  // #F3E5F6
    let background = BrandColors.deepNight
    let onBackground = Color(red: 0.898, green: 0.898, blue: 0.898)
    let surface = Color(red: 0.067, green: 0.094, blue: 0.153)
    let onSurface = Color(red: 0.898, green: 0.898, blue: 0.898)
    let surfaceVariant = Color(red: 0.333, green: 0.255, blue: 0.196)        // #554132
    let onSurfaceVariant = Color(red: 0.831, green: 0.773, blue: 0.706)      // #D4C5B4
    let error = Color(red: 1.0, green: 0.733, blue: 0.733)
    let onError = Color(red: 0.408, green: 0.0, blue: 0.0)
    let errorContainer = Color(red: 0.627, green: 0.078, blue: 0.078)
    let onErrorContainer = Color(red: 1.0, green: 0.902, blue: 0.902)
    
    // Extras required by the protocol
    let outline = BrandColors.neutralOutline
    let shadow = Color.black.opacity(0.4)
}
