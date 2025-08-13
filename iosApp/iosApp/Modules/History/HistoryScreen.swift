import SwiftUI
import Shared

struct HistoryView: View {
    @ObservedObject private(set) var viewModel = ViewModelWrapper<SessionsState, SessionsViewModel>()
    @State private var selectedSession: Session?
    @State private var searchText = ""
    @State private var showingSortSheet = false
    @State private var currentSortOption: SortOption = .dateNewest

    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    var body: some View {
        ZStack {
            theme.background.ignoresSafeArea()

            // Use type checking instead of pattern matching for KMP enums
            if viewModel.uiState is SessionsState.Loading {
                LoadingView(theme: theme)
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
                        currentSortOption = sortOrder
                    },
                    onDeleteSession: { sessionId in
                        // TODO: Add delete functionality to SessionsViewModel
                    },
                    onSearchTextChanged: { searchText in
                        viewModel.viewModel.searchSessions(searchText: searchText)
                    },
                    theme: theme
                )
            } else if let errorState = viewModel.uiState as? SessionsState.Error {
                ErrorView(
                    message: errorState.errorMessage,
                    onRetry: {
                        viewModel.viewModel.fetchSessions()
                    },
                    theme: theme
                )
            }
        }
        .searchable(text: $searchText, placement: .navigationBarDrawer)
        .onChange(of: searchText) { _, newValue in
            viewModel.viewModel.searchSessions(searchText: newValue)
        }
        .toolbar {
            ToolbarItemGroup(placement: .navigationBarTrailing) {
                Button(action: { showingSortSheet = true }) {
                    Image(systemName: "arrow.up.arrow.down")
                }
                .frame(width: 44, height: 44)
            }
        }
        .sheet(isPresented: $showingSortSheet) {
            SortOptionsSheet(
                currentSortOption: currentSortOption,
                onSortSelected: { sortOption in
                    viewModel.viewModel.sortSessions(sortOption: sortOption)
                    currentSortOption = sortOption
                    showingSortSheet = false
                },
                theme: theme
            )
        }
        .onAppear {
            viewModel.startObserving()
            viewModel.viewModel.fetchSessions()
        }
        .navigationDestination(item: $selectedSession) { session in
            SessionDetailsView(sessionId: session.id)
        }
    }
}

// MARK: - Sort Options Sheet
struct SortOptionsSheet: View {
    let currentSortOption: SortOption
    let onSortSelected: (SortOption) -> Void
    let theme: AppTheme
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section {
                    SortOptionRow(
                        title: "Newest First",
                        sortOption: .dateNewest,
                        isSelected: currentSortOption == .dateNewest,
                        onTap: { onSortSelected(.dateNewest) },
                        theme: theme
                    )

                    SortOptionRow(
                        title: "Oldest First",
                        sortOption: .dateOldest,
                        isSelected: currentSortOption == .dateOldest,
                        onTap: { onSortSelected(.dateOldest) },
                        theme: theme
                    )
                } header: {
                    Text("Date")
                        .font(.footnote)
                        .foregroundColor(theme.onSurfaceVariant)
                }

                Section {
                    SortOptionRow(
                        title: "Longest First",
                        sortOption: .durationLongest,
                        isSelected: currentSortOption == .durationLongest,
                        onTap: { onSortSelected(.durationLongest) },
                        theme: theme
                    )

                    SortOptionRow(
                        title: "Shortest First",
                        sortOption: .durationShortest,
                        isSelected: currentSortOption == .durationShortest,
                        onTap: { onSortSelected(.durationShortest) },
                        theme: theme
                    )
                } header: {
                    Text("Duration")
                        .font(.footnote)
                        .foregroundColor(theme.onSurfaceVariant)
                }

                Section {
                    SortOptionRow(
                        title: "Highest First",
                        sortOption: .ascentHighest,
                        isSelected: currentSortOption == .ascentHighest,
                        onTap: { onSortSelected(.ascentHighest) },
                        theme: theme
                    )

                    SortOptionRow(
                        title: "Lowest First",
                        sortOption: .ascentLowest,
                        isSelected: currentSortOption == .ascentLowest,
                        onTap: { onSortSelected(.ascentLowest) },
                        theme: theme
                    )
                } header: {
                    Text("Elevation")
                        .font(.footnote)
                        .foregroundColor(theme.onSurfaceVariant)
                }
            }
            .listStyle(.insetGrouped)
            .scrollContentBackground(.hidden)
            .background(theme.background)
            .navigationTitle("Sort Sessions")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                    .font(.body.weight(.medium))
                    .foregroundColor(theme.primary)
                }
            }
        }
        .presentationDetents([.height(400)])
        .presentationDragIndicator(.visible)
        .presentationCornerRadius(20)
    }
}

// MARK: - Sort Option Row
struct SortOptionRow: View {
    let title: String
    let sortOption: SortOption
    let isSelected: Bool
    let onTap: () -> Void
    let theme: AppTheme

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                Text(title)
                    .foregroundColor(theme.onSurface)
                    .font(.body)
                    .multilineTextAlignment(.leading)

                Spacer()

                if isSelected {
                    Image(systemName: "checkmark")
                        .foregroundColor(theme.primary)
                        .font(.body.weight(.semibold))
                        .imageScale(.medium)
                }
            }
            .frame(minHeight: 40) // Slightly reduced from 44
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .listRowBackground(theme.surface)
        .listRowSeparatorTint(theme.outline.opacity(0.2))
        .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
    }
}

// Loading View
struct LoadingView: View {
    let theme: AppTheme

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
    let theme: AppTheme

    var body: some View {
        VStack {
            Spacer()
            Text(message)
                .font(.headline)
                .foregroundColor(theme.error)
                .multilineTextAlignment(.center)
                .padding()
            
            Button("Retry") {
                onRetry()
            }
            .padding()
            .background(theme.primary)
            .foregroundColor(theme.onPrimary)
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
    let theme: AppTheme

    @State private var sessionIdToDelete: String? = nil
    @State private var currentSortOrder: SortOption = .dateNewest
    @State private var searchText: String = ""
    @FocusState private var isSearchFieldFocused: Bool

    var body: some View {
        // --- Content ---
        if let sessions = state.sessions, !sessions.isEmpty {
            sessionsGrid(sessions: sessions)
        } else {
            EmptyStateView(
                title: "No Sessions Found",
                message: "Start recording your first climbing session!",
                actionText: "Refresh",
                systemImage: "mountain.2",
                onActionTapped: onRefresh,
                theme: theme
            )
        }
    }

    // MARK: - Card list without spacing
    private func sessionsGrid(sessions: [Session]) -> some View {
        return ScrollView {
            LazyVStack(spacing: 0, pinnedViews: []) {
                ForEach(Array(sessions.enumerated()), id: \.element.id) { index, session in
                    Button {
                        onSessionSelected(session)
                    } label: {
                        SessionCard(
                            session: session,
                            onSessionClick: { _ in onSessionSelected(session) },
                            theme: theme
                        )
                        .zIndex(Double(sessions.count - index))
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
