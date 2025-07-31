import SwiftUI
import Shared

struct HistoryView: View {
    @ObservedObject private(set) var viewModel = ViewModelWrapper<SessionsState, SessionsViewModel>()
    @State private var selectedSession: Session?

    // Our consistent color system pattern
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    var body: some View {
        NavigationStack {
            ZStack {
                colors.background.ignoresSafeArea()
                
                // Use type checking instead of pattern matching for KMP enums
                if viewModel.uiState is SessionsState.Loading {
                    LoadingView()
                } else if let loadedState = viewModel.uiState as? SessionsState.Loaded {
                    SessionsContentView(
                        state: loadedState,
                        onSessionSelected: { session in
                            selectedSession = session
                        },
                        onRefresh: {
                            viewModel.viewModel.fetchSessions()
                        },
                        onSortOrderSelected: { sortOrder in
                            viewModel.viewModel.sortSessions(sortOption: sortOrder)
                        },
                        onDeleteSession: { sessionId in
                            // TODO: Add delete functionality to SessionsViewModel
                        }
                    )
                } else if let errorState = viewModel.uiState as? SessionsState.Error {
                    ErrorView(
                        message: errorState.errorMessage,
                        onRetry: {
                            viewModel.viewModel.fetchSessions()
                        }
                    )
                }
            }
            .navigationTitle("Sessions History")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        viewModel.viewModel.fetchSessions()
                    }) {
                        Image(systemName: "arrow.clockwise")
                            .foregroundColor(colors.primary)
                    }
                }
            }
            .onAppear {
                viewModel.startObserving()
                viewModel.viewModel.fetchSessions()
            }
            .navigationDestination(item: $selectedSession) { session in
                SessionDetailsView(sessionId: session.id)
                    .navigationTitle(session.title ?? "Session Details")
            }
        }
    }
}

// Loading View
struct LoadingView: View {
    var body: some View {
        VStack {
            Spacer()
            LoadingAnimation(text: "Loading sessions...")
            Spacer()
        }
    }
}

// Error View
struct ErrorView: View {
    let message: String
    let onRetry: () -> Void
    
    // Our consistent color system pattern
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
            Spacer()
            Text(message)
                .font(.headline)
                .foregroundColor(colors.error)
                .multilineTextAlignment(.center)
                .padding()
            
            Button("Retry") {
                onRetry()
            }
            .padding()
            .background(colors.primary)
            .foregroundColor(colors.onPrimary)
            .cornerRadius(8)
            
            Spacer()
        }
    }
}

// Main content view for the loaded state
struct SessionsContentView: View {
    let state: SessionsState.Loaded
    let onSessionSelected: (Session) -> Void
    let onRefresh: () -> Void
    let onSortOrderSelected: (SortOption) -> Void
    let onDeleteSession: (String) -> Void
    
    @State private var sessionIdToDelete: String? = nil
    @State private var currentSortOrder: SortOption = .dateNewest

    // Our consistent color system pattern
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    var body: some View {
        VStack(spacing: 0) {

            // Simple sort picker for now
            Picker("Sort", selection: $currentSortOrder) {
                Text("Date (Newest)").tag(SortOption.dateNewest)
                Text("Date (Oldest)").tag(SortOption.dateOldest)
                Text("Duration (Longest)").tag(SortOption.durationLongest)
                Text("Duration (Shortest)").tag(SortOption.durationShortest)
                Text("Ascent (Highest)").tag(SortOption.ascentHighest)
                Text("Ascent (Lowest)").tag(SortOption.ascentLowest)
            }
            .pickerStyle(MenuPickerStyle())
            .onChange(of: currentSortOrder) { _, newValue in
                onSortOrderSelected(newValue)
            }
            .padding(.horizontal)
            .padding(.top, 8)
            
            if let sessions = state.sessions, !sessions.isEmpty {
                sessionsGrid(sessions: sessions)
            } else {
                EmptyStateView(
                    title: "No Sessions Found",
                    message: "Start recording your first climbing session!",
                    actionText: "Refresh",
                    systemImage: "mountain.2",
                    onActionTapped: onRefresh
                )
            }
        }
        .confirmationDialog(
            "Delete Session",
            isPresented: Binding(
                get: { sessionIdToDelete != nil },
                set: { if !$0 { sessionIdToDelete = nil } }
            ),
            titleVisibility: .visible
        ) {
            Button("Delete", role: .destructive) {
                if let id = sessionIdToDelete {
                    onDeleteSession(id)
                }
                sessionIdToDelete = nil
            }
            Button("Cancel", role: .cancel) {
                sessionIdToDelete = nil
            }
        } message: {
            Text("This action cannot be undone.")
        }
    }

    private func sessionsGrid(sessions: [Session]) -> some View {
        ScrollView {
            LazyVStack(spacing: 16) {
                ForEach(sessions, id: \.id) { session in
                    Button {
                        onSessionSelected(session)
                    } label: {
                        SessionCard(
                            session: session,
                            onSessionClick: { _ in
                                onSessionSelected(session)
                            }
                        )
                        .padding(.horizontal)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
                
                // Extra padding at the bottom
                Color.clear.frame(height: 80)
            }
            .padding(.top, 8)
        }
    }
}


// Extension to help with state pattern matching
extension SessionsState {
    static func ~= (pattern: (SessionsState.Loaded) -> Bool, value: SessionsState) -> Bool {
        guard let loaded = value as? SessionsState.Loaded else { return false }
        return pattern(loaded)
    }
    
    static func ~= (pattern: (SessionsState.Error) -> Bool, value: SessionsState) -> Bool {
        guard let error = value as? SessionsState.Error else { return false }
        return pattern(error)
    }
}
