//
//  BottomNavigationBar.swift
//  iosApp
//
//  Created by Inon Elgabsi on 30/07/2025.
//

import SwiftUI

struct BottomNavigationBar: View {
    @Binding var selectedTab: Int
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    var body: some View {
        HStack(spacing: 0) {
            // Home Tab
            bottomNavItem(
                icon: "house.fill",
                label: "Home",
                isSelected: selectedTab == 0
            )
            .onTapGesture { selectedTab = 0 }
            
            // Routes Tab
            bottomNavItem(
                icon: "map.fill",
                label: "Routes",
                isSelected: selectedTab == 1
            )
            .onTapGesture { selectedTab = 1 }
            
            // Explore Tab
            bottomNavItem(
                icon: "binoculars.fill",
                label: "Explore",
                isSelected: selectedTab == 2
            )
            .onTapGesture { selectedTab = 2 }
            
            // Profile Tab
            bottomNavItem(
                icon: "person.fill",
                label: "Profile",
                isSelected: selectedTab == 3
            )
            .onTapGesture { selectedTab = 3 }
        }
        .padding(.vertical, 8)
        .background(colors.surface)
        .shadow(color: .black.opacity(0.1), radius: 6, x: 0, y: -2)
    }
    
    private func bottomNavItem(icon: String, label: String, isSelected: Bool) -> some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .font(.system(size: 22))
                .foregroundColor(isSelected ? colors.primary : colors.onSurfaceVariant)
                .frame(height: 24)
            
            Text(label)
                .font(.caption)
                .foregroundColor(isSelected ? colors.primary : colors.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity)
        .contentShape(Rectangle())
    }
}

struct BottomNavigationBar_Previews: PreviewProvider {
    static var previews: some View {
        VStack {
            Spacer()
            BottomNavigationBar(selectedTab: .constant(0))
        }
    }
}
