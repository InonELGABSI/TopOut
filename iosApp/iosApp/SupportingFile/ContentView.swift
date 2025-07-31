import SwiftUI
import Shared

struct ContentView: View {
    // Remove ThemeManager dependency
    @StateObject private var networkMonitor = NetworkMonitor()
    @State private var selectedTab = 0
    
    // Add our consistent color system pattern
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    var body: some View {
        TabView(selection: $selectedTab) {
            LiveSessionView()
                .tabItem { Label("Live", systemImage: "waveform.path.ecg") }
                .tag(0)
            
            HistoryView()
                .tabItem { Label("History", systemImage: "clock") }
                .tag(1)
            
            SettingsView()
                .tabItem { Label("Settings", systemImage: "gear") }
                .tag(2)
        }
        .environmentObject(networkMonitor)
        .accentColor(colors.primary)
        .background(colors.background)
        .preferredColorScheme(colorScheme)
    }
}
