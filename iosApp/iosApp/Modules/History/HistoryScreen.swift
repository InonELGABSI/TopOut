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
                        },
                        onSearchTextChanged: { searchText in
                            viewModel.viewModel.searchSessions(searchText: searchText)
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
    let onSearchTextChanged: (String) -> Void

    @State private var sessionIdToDelete: String? = nil
    @State private var currentSortOrder: SortOption = .dateNewest
    @State private var searchText: String = ""
    @FocusState private var isSearchFieldFocused: Bool

    // Color system
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue

    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }

    // Computed property for sort display text
    private var sortDisplayText: String {
        switch currentSortOrder {
        case .dateNewest: return "Date (Newest)"
        case .dateOldest: return "Date (Oldest)"
        case .durationLongest: return "Duration (Longest)"
        case .durationShortest: return "Duration (Shortest)"
        case .ascentHighest: return "Ascent (Highest)"
        case .ascentLowest: return "Ascent (Lowest)"
        }
    }

    var body: some View {
        GeometryReader { geometry in
            VStack(spacing: 0) {
                // --- Top Card: Sessions History & Sort ---
                TopRoundedCard(backgroundColor: colors.primary) {
                    VStack(spacing: 16) {
                        // First row: Title
                        HStack {
                            Text("Sessions History")
                                .font(.title2.bold())
                                .foregroundColor(colors.onPrimary)
                            Spacer()
                        }

                        // Second row: Sort button and search field
                        HStack(spacing: 12) {
                            // Sort picker on the left - styled as chip with Menu
                            Menu {
                                Button("Date (Newest)") {
                                    currentSortOrder = .dateNewest
                                    onSortOrderSelected(.dateNewest)
                                }
                                Button("Date (Oldest)") {
                                    currentSortOrder = .dateOldest
                                    onSortOrderSelected(.dateOldest)
                                }
                                Button("Duration (Longest)") {
                                    currentSortOrder = .durationLongest
                                    onSortOrderSelected(.durationLongest)
                                }
                                Button("Duration (Shortest)") {
                                    currentSortOrder = .durationShortest
                                    onSortOrderSelected(.durationShortest)
                                }
                                Button("Ascent (Highest)") {
                                    currentSortOrder = .ascentHighest
                                    onSortOrderSelected(.ascentHighest)
                                }
                                Button("Ascent (Lowest)") {
                                    currentSortOrder = .ascentLowest
                                    onSortOrderSelected(.ascentLowest)
                                }
                            } label: {
                                HStack(spacing: 8) {
                                    Text(sortDisplayText)
                                        .foregroundColor(colors.onPrimary)
                                        .lineLimit(1)
                                    Image(systemName: "chevron.down")
                                        .foregroundColor(colors.onPrimary)
                                        .font(.caption)
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(
                                    RoundedRectangle(cornerRadius: 20)
                                        .fill(colors.onPrimary.opacity(0.2))
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 20)
                                        .stroke(colors.onPrimary.opacity(0.3), lineWidth: 1)
                                )
                            }

                            // Search field on the right - styled as chip
                            TextField("Session name", text: $searchText)
                                .focused($isSearchFieldFocused)
                                .foregroundColor(colors.onPrimary)
                                .textFieldStyle(PlainTextFieldStyle())
                                .onChange(of: searchText) { _, newValue in
                                    onSearchTextChanged(newValue)
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(
                                    RoundedRectangle(cornerRadius: 20)
                                        .fill(colors.onPrimary.opacity(0.2))
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 20)
                                        .stroke(colors.onPrimary.opacity(0.3), lineWidth: 1)
                                )
                        }
                    }
                    .padding(.top, geometry.safeAreaInsets.top)
                }

                // --- Content ---
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
            .background(colors.background.ignoresSafeArea())
            .edgesIgnoringSafeArea(.top)
            .onTapGesture {
                // Dismiss keyboard when tapping outside
                isSearchFieldFocused = false
            }
        }
    }

    // MARK: - Card list with vertical overlap
    private func sessionsGrid(sessions: [Session]) -> some View {
        let overlap: CGFloat = 32 // how far each card should overlap the previous one

        return ScrollView {
            LazyVStack(spacing: -overlap, pinnedViews: []) {
                ForEach(Array(sessions.enumerated()), id: \.element.id) { index, session in
                    Button {
                        onSessionSelected(session)
                    } label: {
                        SessionCard(
                            session: session,
                            onSessionClick: { _ in onSessionSelected(session) }
                        )
                        .padding(.top, index == 0 ? 0 : overlap)
                        .zIndex(Double(index))
                    }
                    .buttonStyle(.plain)
                }
                // extra scroll-room below the final card
                Color.clear.frame(height: 80)
            }
            .padding(.top, 8) // initial breathing-space
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
