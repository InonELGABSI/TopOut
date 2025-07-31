import SwiftUI

struct OceanBlueLightColors: TopOutColorScheme {
    let primary = BrandColors.mountainBlue
    let onPrimary = Color.white
    let primaryContainer = Color(red: 0.898, green: 0.933, blue: 0.980)      // #E5EEFA
    let onPrimaryContainer = BrandColors.mountainBlue
    let secondary = BrandColors.skyBlue
    let onSecondary = Color.white
    let secondaryContainer = Color(red: 0.898, green: 0.949, blue: 0.988)    // #E5F2FC
    let onSecondaryContainer = BrandColors.skyBlue
    let background = BrandColors.snowWhite
    let onBackground = Color(red: 0.137, green: 0.137, blue: 0.137)
    let surface = Color(red: 0.992, green: 0.988, blue: 0.984)
    let onSurface = Color(red: 0.137, green: 0.137, blue: 0.137)
    let surfaceVariant = Color(red: 0.941, green: 0.953, blue: 0.973)        // #F0F3F8
    let onSurfaceVariant = Color(red: 0.239, green: 0.239, blue: 0.239)
    let error = Color(red: 0.827, green: 0.184, blue: 0.184)
    let onError = Color.white
    let errorContainer = Color(red: 1.0, green: 0.902, blue: 0.902)
    let onErrorContainer = Color(red: 0.827, green: 0.184, blue: 0.184)
    
    // Using consistent outline value with other color schemes
    let outline = BrandColors.neutralOutline
    let shadow = Color.black.opacity(0.2)
}

struct OceanBlueDarkColors: TopOutColorScheme {
    let primary = Color(red: 0.333, green: 0.600, blue: 0.933)
    let onPrimary = Color(red: 0.067, green: 0.133, blue: 0.200)
    let primaryContainer = Color(red: 0.200, green: 0.400, blue: 0.667)
    let onPrimaryContainer = Color(red: 0.733, green: 0.867, blue: 0.967)
    let secondary = Color(red: 0.467, green: 0.733, blue: 0.933)
    let onSecondary = Color(red: 0.033, green: 0.133, blue: 0.200)
    let secondaryContainer = Color(red: 0.133, green: 0.400, blue: 0.667)
    let onSecondaryContainer = Color(red: 0.733, green: 0.867, blue: 0.967)
    let background = Color(red: 0.067, green: 0.067, blue: 0.067)
    let onBackground = Color(red: 0.867, green: 0.867, blue: 0.867)
    let surface = Color(red: 0.133, green: 0.133, blue: 0.133)
    let onSurface = Color(red: 0.867, green: 0.867, blue: 0.867)
    let surfaceVariant = Color(red: 0.133, green: 0.200, blue: 0.267)
    let onSurfaceVariant = Color(red: 0.667, green: 0.733, blue: 0.800)
    let error = Color(red: 0.933, green: 0.467, blue: 0.467)
    let onError = Color(red: 0.133, green: 0.067, blue: 0.067)
    let errorContainer = Color(red: 0.667, green: 0.200, blue: 0.200)
    let onErrorContainer = Color(red: 1.0, green: 0.800, blue: 0.800)
    
    // Using consistent outline value with other color schemes
    let outline = BrandColors.neutralOutline
    let shadow = Color.black.opacity(0.4)
}
