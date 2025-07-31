import SwiftUI

struct ConfirmationDialog: View {
    let title: String
    let message: String
    let confirmText: String
    let cancelText: String
    let onConfirm: () -> Void
    let onCancel: () -> Void
    @Binding var isPresented: Bool
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    var body: some View {
        VStack(spacing: 20) {
            VStack(spacing: 8) {
                Text(title)
                    .font(.headline)
                    .foregroundColor(colors.onSurface)
                    .multilineTextAlignment(.center)
                
                Text(message)
                    .font(.body)
                    .foregroundColor(colors.onSurfaceVariant)
                    .multilineTextAlignment(.center)
            }
            
            HStack(spacing: 12) {
                Button(cancelText) {
                    onCancel()
                    isPresented = false
                }
                .buttonStyle(SecondaryButtonStyle())
                
                Button(confirmText) {
                    onConfirm()
                    isPresented = false
                }
                .buttonStyle(PrimaryButtonStyle())
            }
        }
        .padding(24)
        .background(colors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.2), radius: 20)
    }
}

struct PrimaryButtonStyle: ButtonStyle {
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding(.horizontal, 24)
            .padding(.vertical, 12)
            .background(colors.primary)
            .foregroundColor(colors.onPrimary)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .opacity(configuration.isPressed ? 0.8 : 1.0)
    }
}

struct SecondaryButtonStyle: ButtonStyle {
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding(.horizontal, 24)
            .padding(.vertical, 12)
            .background(colors.surfaceVariant)
            .foregroundColor(colors.onSurfaceVariant)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .opacity(configuration.isPressed ? 0.8 : 1.0)
    }
}
