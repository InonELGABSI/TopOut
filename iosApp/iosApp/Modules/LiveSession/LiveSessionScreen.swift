import SwiftUI
import Shared
import MapKit

struct LiveSessionView: View {
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
    @State private var animationTrigger = 0
    @State private var sessionToastVisible = false
    @State private var sessionToastMessage = ""
    @State private var sessionToastSuccess = true
    // NEW: observable MSL state for UI updates
    @State private var mslHeightState: MSLHeightState = MSLHeightState.Loading()

    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @State private var navigateToDetails = false
    @State private var stoppedSessionId: String? = nil
    
    // MARK: – Body
    
    var body: some View {
        ZStack {
            theme.background.ignoresSafeArea()
            sessionContent
            dangerToastView
            sessionFeedbackToastView
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
                        viewModel.viewModel.resetToInitialState()
                        stoppedSessionId = nil
                        hasNavigatedToDetails = false
                    }
            }
        }

    }
    

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
                    .id("mountain_animation_\(animationTrigger)")
                }
                .frame(maxWidth: .infinity)
                .padding(.top, 60)
                .padding(.bottom, 40)
                .onAppear {
                    animationTrigger += 1
                }

                StartSessionContent(
                    hasLocationPermission: hasLocationPermission,
                    onStartClick:          {
                        let ok = viewModel.viewModel.onStartClicked()
                        showSessionToast(message: ok ? "Session started" : "Failed to start session", success: ok)
                    },
                    onRequestLocationPermission: { requestLocationPermission() },
                    onRefreshMSL:          { viewModel.viewModel.refreshMSLHeight() },
                    mslHeightState:        mslHeightState,
                    theme:                 theme
                )

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
                onPauseClicked: {
                    let ok = viewModel.viewModel.onPauseClicked()
                    showSessionToast(message: ok ? "Session paused" : "Failed to pause", success: ok)
                },
                onResumeClicked: {
                    let ok = viewModel.viewModel.onResumeClicked()
                    showSessionToast(message: ok ? "Session resumed" : "Failed to resume", success: ok)
                },
                isPaused: false,
                theme:         theme
            )

        case .paused(let state):
            ActiveSessionContent(
                trackPoint:    state.trackPoint,
                trackPoints:   state.historyTrackPoints,
                mapRegion:     $mapRegion,
                onStopClicked: { showingStopConfirmation    = true },
                onCancelClicked: { showingDiscardConfirmation = true },
                onPauseClicked: {
                    let ok = viewModel.viewModel.onPauseClicked()
                    showSessionToast(message: ok ? "Session paused" : "Failed to pause", success: ok)
                },
                onResumeClicked: {
                    let ok = viewModel.viewModel.onResumeClicked()
                    showSessionToast(message: ok ? "Session resumed" : "Failed to resume", success: ok)
                },
                isPaused: true,
                theme:         theme
            )

        case .stopping:
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
                onRetryClick: {
                    let ok = viewModel.viewModel.onStartClicked()
                    showSessionToast(message: ok ? "Retrying…" : "Retry failed", success: ok)
                },
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
    
    @ViewBuilder private var sessionFeedbackToastView: some View {
        if sessionToastVisible {
            VStack {
                Spacer()
                FeedbackToast(message: sessionToastMessage, success: sessionToastSuccess) {
                    withAnimation { sessionToastVisible = false }
                }
                .padding(.horizontal)
                .padding(.bottom, showDangerToast ? 200 : 120)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .transition(.move(edge: .bottom).combined(with: .opacity))
            .animation(.spring(), value: sessionToastVisible)
        }
    }


    private func onAppearSetup() {
        viewModel.startObserving()
        checkLocationPermission()
        Task { @MainActor in
            for await state in viewModel.viewModel.uiState {
                if let loaded = state as? LiveSessionState.Loaded, loaded.trackPoint.danger {
                    let now = Int64(Date().timeIntervalSince1970 * 1000)
                    if !showDangerToast && (now - lastToastTimestamp) > 10_000 {
                        currentAlertType   = loaded.trackPoint.alertType
                        showDangerToast    = true
                        lastToastTimestamp = now
                    }
                }
            }
        }
        Task { @MainActor in
            for await state in viewModel.viewModel.mslHeightState {
                mslHeightState = state
            }
        }
    }
    

    private func stopAlert() -> Alert {
        Alert(
            title:   Text("Stop Session"),
            message: Text("Are you sure you want to stop this climbing session? Your progress will be saved."),
            primaryButton: .destructive(Text("Stop Session")) {
                if let loaded = viewModel.uiState as? LiveSessionState.Loaded {
                    let ok = viewModel.viewModel.onStopClicked(sessionId: loaded.trackPoint.sessionId)
                    showSessionToast(message: ok ? "Stopping session…" : "Failed to stop session", success: ok)
                }
            },
            secondaryButton: .cancel(Text("Continue"))
        )
    }
    
    @ViewBuilder
    private func discardActions() -> some View {
        Button("Cancel Session", role: .destructive) {
            if let loaded = viewModel.uiState as? LiveSessionState.Loaded {
                let ok = viewModel.viewModel.onCancelClicked(sessionId: loaded.trackPoint.sessionId)
                showSessionToast(message: ok ? "Session cancelled" : "Failed to cancel", success: ok)
            }
        }
        Button("Keep Session", role: .cancel) { }
    }
    

    private func checkLocationPermission() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { hasLocationPermission = true }
    }
    
    private func requestLocationPermission() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) { hasLocationPermission = true }
    }
    

    private func getAlertMessage(alertType: AlertType) -> String {
        switch alertType {
        case .rapidDescent:           return "Rapid altitude decrease detected!"
        case .rapidAscent:            return "Rapid altitude increase detected!"
        case .relativeHeightExceeded: return "Height threshold exceeded!"
        case .totalHeightExceeded:    return "Maximum height threshold exceeded!"
        case .none:                   return "Warning: Abnormal movement detected!"
        }
    }

    private func showSessionToast(message: String, success: Bool) {
        sessionToastMessage = message
        sessionToastSuccess = success
        withAnimation { sessionToastVisible = true }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            withAnimation { sessionToastVisible = false }
        }
    }
}
