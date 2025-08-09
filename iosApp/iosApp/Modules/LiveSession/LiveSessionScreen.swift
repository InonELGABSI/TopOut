import SwiftUI
import Shared
import MapKit

struct LiveSessionView: View {
    // MARK: – State / DI
    @ObservedObject private(set) var viewModel = ViewModelWrapper<LiveSessionState, LiveSessionViewModel>()
    @State private var showingStopConfirmation    = false
    @State private var showingDiscardConfirmation = false
    @State private var mapRegion = MKCoordinateRegion(
        center: .init(latitude: 37.7749, longitude: -122.4194),
        span:  .init(latitudeDelta: 0.01, longitudeDelta: 0.01)
    )
    @State private var showDangerToast  = false
    @State private var currentAlertType = AlertType.none
    @State private var lastToastTimestamp: Int64 = 0
    @State private var hasLocationPermission   = false
    @State private var hasNavigatedToDetails = false

    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    // ⬅️ ADDED for navigation to SessionDetails
    @State private var navigateToDetails = false
    @State private var stoppedSessionId: String? = nil
    
    // MARK: – Body
    
    var body: some View {
        ZStack {
            theme.background.ignoresSafeArea()
            sessionContent          // big switch broken out
            dangerToastView         // toast broken out
        }
        .onAppear(perform: onAppearSetup)
        .alert(isPresented: $showingStopConfirmation, content: stopAlert)
        .confirmationDialog(
            "Cancel Session",
            isPresented: $showingDiscardConfirmation,
            titleVisibility: .visible,
            actions: discardActions,
            message: {
                Text("Are you sure you want to cancel this session? All tracking data will be permanently deleted and cannot be recovered.")
            }
        )
        .ignoresSafeArea(edges: .top)
        .navigationBarHidden(true)
        
        .navigationDestination(isPresented: $navigateToDetails) {
            if let sessionId = stoppedSessionId {
                SessionDetailsView(sessionId: sessionId)
                    .navigationTitle("Session Details")
                    .navigationBarTitleDisplayMode(.inline)
                    .onDisappear {
                        viewModel.viewModel.resetToInitialState() // clear ViewModel state
                        stoppedSessionId = nil
                        hasNavigatedToDetails = false              // allow navigation next time
                    }
            }
        }

    }
    
    // MARK: – Computed Views
    
    @ViewBuilder
    private var sessionContent: some View {
        switch onEnum(of: viewModel.uiState) {
        case .loading:
            VStack(spacing: 0) {
                // Mountain animation section
                VStack {
                    MountainAnimationView(
                        animationAsset: "Travel_Mountain",
                        speed: 1.2,
                        animationSize: 220,
                        iterations: 1
                    )
                    .id(UUID()) // Force recreation on each navigation
                }
                .frame(maxWidth: .infinity)
                .padding(.top, 60) // Generous top spacing after safe area
                .padding(.bottom, 40) // Space before content

                // Content section
                StartSessionContent(
                    hasLocationPermission: hasLocationPermission,
                    onStartClick:          { viewModel.viewModel.onStartClicked() },
                    onRequestLocationPermission: { requestLocationPermission() },
                    mslHeightState:        viewModel.viewModel.mslHeightState.value,
                    theme:                 theme
                )

                // Push content up, button stays at natural position
                Spacer(minLength: 0)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .safeAreaInset(edge: .top) { Color.clear.frame(height: 0) }

        case .loaded(let state):
            ActiveSessionContent(
                trackPoint:    state.trackPoint,
                trackPoints:   state.historyTrackPoints,
                mapRegion:     $mapRegion,
                onStopClicked: { showingStopConfirmation    = true },
                onCancelClicked: { showingDiscardConfirmation = true },
                theme:         theme
            )

        case .stopping:
            // ⬅️ CHANGED: Show loading spinner while stopping
            VStack {
                ProgressView("Stopping session…")
                    .progressViewStyle(CircularProgressViewStyle())
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

        case .sessionStopped(let state):
            Color.clear
                .onAppear {
                    if !hasNavigatedToDetails {
                        stoppedSessionId = state.sessionId
                        navigateToDetails = true
                        hasNavigatedToDetails = true
                    }
                }

        case .error(let state):
            ErrorContent(
                errorMessage: state.errorMessage,
                onRetryClick: { viewModel.viewModel.onStartClicked() },
                theme:        theme
            )
        }
    }
    
    @ViewBuilder private var dangerToastView: some View {
        if showDangerToast {
            VStack {
                Spacer()
                DangerToast(
                    message:  getAlertMessage(alertType: currentAlertType),
                    isVisible: showDangerToast,
                    color:    theme.error,
                    onDismiss:{ showDangerToast = false }
                )
                .padding(.horizontal)
                .padding(.bottom, 120)
            }
            .transition(.move(edge: .bottom).combined(with: .opacity))
            .animation(.spring(), value: showDangerToast)
        }
    }
    
    // MARK: – Lifecycle
    
    private func onAppearSetup() {
        viewModel.startObserving()
        checkLocationPermission()
        
        Task {
            for await state in viewModel.viewModel.uiState {
                if let loaded = state as? LiveSessionState.Loaded,
                   loaded.trackPoint.danger {
                    let now = Int64(Date().timeIntervalSince1970 * 1000)
                    if !showDangerToast && (now - lastToastTimestamp) > 10_000 {
                        currentAlertType   = loaded.trackPoint.alertType
                        showDangerToast    = true
                        lastToastTimestamp = now
                    }
                }
            }
        }
    }
    
    // MARK: – Alerts / dialogs
    
    private func stopAlert() -> Alert {
        Alert(
            title:   Text("Stop Session"),
            message: Text("Are you sure you want to stop this climbing session? Your progress will be saved."),
            primaryButton: .destructive(Text("Stop Session")) {
                if let loaded = viewModel.uiState as? LiveSessionState.Loaded {
                    viewModel.viewModel.onStopClicked(sessionId: loaded.trackPoint.sessionId)
                }
            },
            secondaryButton: .cancel(Text("Continue"))
        )
    }
    
    @ViewBuilder
    private func discardActions() -> some View {
        Button("Cancel Session", role: .destructive) {
            if let loaded = viewModel.uiState as? LiveSessionState.Loaded {
                viewModel.viewModel.onCancelClicked(sessionId: loaded.trackPoint.sessionId)
            }
        }
        Button("Keep Session", role: .cancel) { }
    }
    
    // MARK: – Permission helpers
    
    private func checkLocationPermission() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { hasLocationPermission = true }
    }
    
    private func requestLocationPermission() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) { hasLocationPermission = true }
    }
    
    // MARK: – Toast helpers
    
    private func getAlertMessage(alertType: AlertType) -> String {
        switch alertType {
        case .rapidDescent:           return "Rapid altitude decrease detected!"
        case .rapidAscent:            return "Rapid altitude increase detected!"
        case .relativeHeightExceeded: return "Height threshold exceeded!"
        case .totalHeightExceeded:    return "Maximum height threshold exceeded!"
        case .none:                   return "Warning: Abnormal movement detected!"
        }
    }
}
