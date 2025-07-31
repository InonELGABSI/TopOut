//
//  TopFadeGradient.swift
//  iosApp
//
//  Created by Inon Elgabsi on 30/07/2025.
//

import SwiftUI
import Shared

struct TopFadeGradient: View {
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
            LinearGradient(
                gradient: Gradient(colors: [
                    colors.background.opacity(0.95),
                    colors.background.opacity(0.8),
                    colors.background.opacity(0.4),
                    Color.clear
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(height: 100)
            
            Spacer()
        }
        .allowsHitTesting(false) // Allow touches to pass through
        .ignoresSafeArea(.all, edges: .top)
    }
}
