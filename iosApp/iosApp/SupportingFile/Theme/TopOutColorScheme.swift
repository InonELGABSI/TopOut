import SwiftUI

/// All semantic colour‑roles we expose to the UI layer.
/// Matches Material 3 naming so tokens stay familiar across platforms.
protocol TopOutColorScheme {
    // Core tones
    var primary:            Color { get }
    var onPrimary:          Color { get }
    var primaryContainer:   Color { get }
    var onPrimaryContainer: Color { get }

    var secondary:            Color { get }
    var onSecondary:          Color { get }
    var secondaryContainer:   Color { get }
    var onSecondaryContainer: Color { get }

    // Surfaces & backgrounds
    var background:       Color { get }
    var onBackground:     Color { get }
    var surface:          Color { get }
    var onSurface:        Color { get }
    var surfaceVariant:   Color { get }
    var onSurfaceVariant: Color { get }
    var surfaceContainer: Color { get }
    var tertiaryContainer: Color { get }

    // Error states
    var error:            Color { get }
    var onError:          Color { get }
    var errorContainer:   Color { get }
    var onErrorContainer: Color { get }

    // Extras recommended by Material/HIG
    var outline: Color { get }   // borders & dividers :contentReference[oaicite:3]{index=3}
    var shadow:  Color { get }   // elevation‑casting colour :contentReference[oaicite:4]{index=4}
}
