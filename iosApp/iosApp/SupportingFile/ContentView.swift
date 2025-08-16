import SwiftUI
import Shared

struct ContentView: View {
    @StateObject private var networkMonitor = NetworkMonitor()
    @EnvironmentObject var themeManager: AppThemeManager
    @State private var selectedTab = 0

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
        .accentColor(themeManager.currentTheme.primary)
        .background(themeManager.currentTheme.background)
        .preferredColorScheme(nil)
    }
}
