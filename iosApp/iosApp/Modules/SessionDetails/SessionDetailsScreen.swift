import SwiftUI
import Shared
import Charts

struct SessionDetailsView: View {
    @StateObject private var viewModel = ViewModelWrapper<SessionDetailsState, SessionDetailsViewModel>()
    
    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

    @Environment(\.presentationMode) var presentationMode
    @State private var showDeleteConfirmation = false
    @State private var showEditTitleDialog = false
    @State private var editTitleText = ""
    @State private var showShareSheet = false
    
    private let sessionId: String
    @State private var hasLoaded = false   // prevent multiple loads
    
    init(sessionId: String) {
        self.sessionId = sessionId
    }
    
    var body: some View {
        ZStack {
            theme.background.ignoresSafeArea()

            switch onEnum(of: viewModel.uiState) {
            case .loading:
                LoadingAnimation(
                    text: "Loading session details...",
                    animationAsset: "outdoor_boots_animation",
                    speed: 1.4,
                    animationSize: 220,
                    containerWidth: 240,
                    containerHeight: 170,
                    spacing: 24
                )
                
            case .loaded(let state):
                ScrollView {
                    VStack(spacing: 0) {
                        if !state.sessionDetails.points.isEmpty {
                            MapPreview(trackPoints: state.sessionDetails.points)
                                .frame(height: 500)
                                .clipShape(.rect(bottomLeadingRadius: 24, bottomTrailingRadius: 24))
                                .ignoresSafeArea(edges: .top)
                        }
                        
                        // Title Section with circular corners
                        VStack {
                            HStack {
                                Text(state.sessionDetails.session.title ?? "Climbing Session")
                                    .font(.title3)
                                    .fontWeight(.semibold)
                                    .foregroundColor(theme.onSurface)
                                    .lineLimit(2)
                                    .multilineTextAlignment(.leading)

                                Spacer()

                                Button(action: {
                                    editTitleText = state.sessionDetails.session.title ?? "Climbing Session"
                                    showEditTitleDialog = true
                                }) {
                                    Image(systemName: "pencil")
                                        .font(.system(size: 16, weight: .medium))
                                        .foregroundColor(theme.primary)
                                        .frame(width: 32, height: 32)
                                        .background(theme.primary.opacity(0.1))
                                        .clipShape(Circle())
                                }
                            }
                            .padding(.horizontal, 20)
                            .padding(.vertical, 16)
                            .background(theme.surfaceContainer)
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                        .padding(.horizontal, 16)
                        .padding(.top, 16)

                        // Info Section with no background
                        SessionInfoSection(
                            sessionDetails: state.sessionDetails,
                            onDeleteClick: { showDeleteConfirmation = true },
                            theme: theme
                        )
                        .background(Color.clear)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 16)

                        // Climbing Session Card with top rounded corners only and 3D shading
                        VStack(spacing: 0) {
                            Text("Climbing Session")
                                .font(.system(size: 28, weight: .bold))
                                .foregroundColor(theme.onSurface)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.horizontal, 24)
                                .padding(.top, 24)
                                .padding(.bottom, 16)

                            Divider()
                                .background(theme.outline.opacity(0.3))
                                .padding(.horizontal, 24)
                            
                            SessionStatisticsCard(sessionDetails: state.sessionDetails, theme: theme)
                                .padding(.top, 16)
                            
                            Divider()
                                .background(theme.outline.opacity(0.3))
                                .padding(.horizontal, 24)
                                .padding(.vertical, 16)
                            
                            if state.sessionDetails.points.count > 1 {
                                VStack(alignment: .leading, spacing: 12) {
                                    HStack {
                                        Image(systemName: "chart.xyaxis.line")
                                            .foregroundColor(theme.primary)
                                        Text("Altitude over Time")
                                            .font(.headline)
                                            .fontWeight(.bold)
                                    }
                                    .padding(.horizontal, 16)
                                    
                                    TimeHeightChartView(
                                        samples: prepareChartData(points: state.sessionDetails.points),
                                        theme: theme
                                    )
                                    .frame(height: 200)
                                    .padding(.horizontal, 16)
                                }
                                .padding(.vertical, 8)
                            }
                            
                            TrackPointsSection(trackPoints: state.sessionDetails.points, theme: theme)
                                .padding(16)
                        }
                        .background(theme.surface)
                        .clipShape(.rect(topLeadingRadius: 24, topTrailingRadius: 24))
                        .shadow(color: .black.opacity(0.15), radius: 8, x: 0, y: -4)
                        .padding(.top, 8)
                    }
                }
                
            case .error(let state):
                SessionErrorContent(
                    errorMessage: state.errorMessage,
                    onRetryClick: {
                        viewModel.viewModel.loadSession(sessionId: sessionId)
                    },
                    theme: theme
                )
                
            @unknown default:
                EmptyView()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { showShareSheet = true }) {
                    Image(systemName: "square.and.arrow.up")
                }
                .frame(width: 44, height: 44)
            }
        }
        .sheet(isPresented: $showShareSheet) {
            if let loadedState = viewModel.uiState as? SessionDetailsState.Loaded {
                ShareSessionSheet(sessionDetails: loadedState.sessionDetails)
            }
        }
        .alert(isPresented: $showDeleteConfirmation) {
            Alert(
                title: Text("Delete Session"),
                message: Text("This action cannot be undone. Are you sure you want to delete this session?"),
                primaryButton: .destructive(Text("Delete")) {
                    if case .loaded(let loadedState) = onEnum(of: viewModel.uiState) {
                        viewModel.viewModel.deleteSession(sessionId: loadedState.sessionDetails.session.id)
                        presentationMode.wrappedValue.dismiss()
                    }
                },
                secondaryButton: .cancel()
            )
        }
        .sheet(isPresented: $showEditTitleDialog) {
            EditTitleView(
                currentTitle: editTitleText,
                onSave: { newTitle in
                    if case .loaded(let state) = onEnum(of: viewModel.uiState) {
                        viewModel.viewModel.updateSessionTitle(sessionId: state.sessionDetails.session.id, newTitle: newTitle)
                    }
                },
                onCancel: { showEditTitleDialog = false },
                theme: theme
            )
        }
        .onAppear {
            viewModel.startObserving()
            if !hasLoaded {
                viewModel.viewModel.loadSession(sessionId: sessionId)
                hasLoaded = true
            }
        }
    }
}

// MARK: - Share Session Sheet
struct ShareSessionSheet: View {
    let sessionDetails: SessionDetails
    @Environment(\.dismiss) private var dismiss

    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                VStack(alignment: .leading, spacing: 16) {
                    Text("Share this climbing session")
                        .font(.headline)
                        .foregroundColor(theme.onSurface)

                    Text("Session: \(sessionDetails.session.title ?? "Climbing Session")")
                        .font(.subheadline)
                        .foregroundColor(theme.onSurfaceVariant)

                    Text("Duration: \(calculateSessionDuration(sessionDetails))")
                        .font(.subheadline)
                        .foregroundColor(theme.onSurfaceVariant)
                }

                VStack(spacing: 12) {
                    ShareLink(
                        item: generateShareText(),
                        subject: Text("Climbing Session - \(sessionDetails.session.title ?? "Session")")
                    ) {
                        HStack {
                            Image(systemName: "square.and.arrow.up")
                            Text("Share Session Summary")
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(theme.primary)
                        .foregroundColor(theme.onPrimary)
                        .cornerRadius(8)
                    }
                }

                Spacer()
            }
            .padding()
            .navigationTitle("Share Session")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }

    private func generateShareText() -> String {
        let title = sessionDetails.session.title ?? "Climbing Session"
        let duration = calculateSessionDuration(sessionDetails)
        let maxAlt = sessionDetails.points.map { $0.altitude?.double ?? 0.0 }.max() ?? 0.0
        let totalGain = sessionDetails.points.last?.gain ?? 0.0

        return """
        🧗‍♂️ \(title)

        📊 Session Stats:
        ⏱️ Duration: \(duration)
        📈 Max Altitude: \(String(format: "%.1f m", maxAlt))
        ⬆️ Total Gain: \(String(format: "%.1f m", totalGain))

        Tracked with TopOut 🏔️
        """
    }
}

// All color usage in these components should use the passed `theme` parameter:

struct SessionTitleSection: View {
    let sessionDetails: SessionDetails
    let onEditClick: () -> Void
    let theme: AppTheme

    var body: some View {
        ZStack {
            VStack {
                Rectangle()
                    .fill(Color.clear)
                    .frame(height: 15)
                RoundedRectangle(cornerRadius: 24)
                    .fill(theme.surfaceContainer)
                    .cornerRadius(0, corners: [.topLeft, .topRight])
                    .cornerRadius(24, corners: [.bottomLeft, .bottomRight])
            }
            HStack {
                Text("Title: ")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(theme.onSurfaceVariant)
                Text(sessionDetails.session.title ?? "Climbing Session")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(theme.onSurface)
                Spacer()
                Button(action: onEditClick) {
                    Image(systemName: "pencil")
                        .foregroundColor(theme.primary)
                        .font(.system(size: 16))
                }
                .frame(width: 40, height: 40)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
        }
        .frame(height: 60)
        .padding(.horizontal)
        .zIndex(-1)
    }
}

struct SessionInfoSection: View {
    let sessionDetails: SessionDetails
    let onDeleteClick: () -> Void
    let theme: AppTheme

    var body: some View {
        HStack(spacing: 0) {
            VStack(spacing: 4) {
                Text(
                    sessionDetails.session.startTime.map { formatDate(Int64(truncating: $0)) } ?? "-"
                )
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(theme.onSurface)
                Text(
                    sessionDetails.session.startTime.map { formatDate(Int64(truncating: $0)) } ?? "-"
                )
                .font(.subheadline)
                    .foregroundColor(theme.onSurfaceVariant)
            }
            .frame(maxWidth: .infinity)
            VStack(spacing: 4) {
                Text(calculateSessionDuration(sessionDetails))
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(theme.onSurface)
                Text("Duration")
                    .font(.subheadline)
                    .foregroundColor(theme.onSurfaceVariant)
            }
            .frame(maxWidth: .infinity)
            VStack(spacing: 4) {
                Button(action: onDeleteClick) {
                    Circle()
                        .fill(theme.errorContainer)
                        .frame(width: 40, height: 40)
                        .overlay(
                            Image(systemName: "trash")
                                .foregroundColor(theme.onErrorContainer)
                        )
                }
                Text("Delete")
                    .font(.caption)
                    .foregroundColor(theme.error)
            }
            .frame(maxWidth: .infinity)
        }
        .padding(.vertical, 10)
    }
    
    private func formatDate(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp) / 1000)
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM dd"
        return formatter.string(from: date)
    }
    
    private func formatTime(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp) / 1000)
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }
}

struct SessionStatisticsCard: View {
    let sessionDetails: SessionDetails
    let theme: AppTheme

    var body: some View {
        VStack(spacing: 16) {
            HStack(spacing: 0) {
                StatisticItemWithIcon(
                    icon: "arrow.up",
                    label: "Max Altitude",
                    value: String(format: "%.1f m", sessionDetails.points.map { $0.altitude?.double ?? 0.0 }.max() ?? 0.0),
                    theme: theme
                )
                Spacer()
                StatisticItemWithIcon(
                    icon: "arrow.down",
                    label: "Min Altitude",
                    value: String(format: "%.1f m", sessionDetails.points.map {$0.altitude?.double ?? 0.0}.min() ?? 0.0),
                    theme: theme
                )
                Spacer()
                StatisticItemWithIcon(
                    icon: "chart.line.uptrend.xyaxis",
                    label: "Total Gain",
                    value: String(format: "%.1f m", sessionDetails.points.last?.gain ?? 0.0),
                    textColor: .green,
                    theme: theme
                )
            }
            .padding(.horizontal, 16)
            HStack(spacing: 0) {
                Spacer()
                StatisticItemWithIcon(
                    icon: "chart.line.downtrend.xyaxis",
                    label: "Total Loss",
                    value: String(format: "%.1f m", sessionDetails.points.last?.loss ?? 0.0),
                    textColor: .orange,
                    theme: theme
                )
                Spacer()
                Spacer()
                StatisticItemWithIcon(
                    icon: "speedometer",
                    label: "Max Speed",
                    value: String(format: "%.1f m/s", sessionDetails.points.map { $0.vTotal }.max() ?? 0.0),
                    theme: theme
                )
                Spacer()
            }
            .padding(.horizontal, 16)
        }
        .padding(.vertical, 8)
    }
}

struct StatisticItemWithIcon: View {
    let icon: String
    let label: String
    let value: String
    var textColor: Color = Color.primary
    let theme: AppTheme

    var body: some View {
        HStack(alignment: .center, spacing: 8) {
            Image(systemName: icon)
                .foregroundColor(theme.primary)
                .font(.system(size: 18))
            VStack(alignment: .leading, spacing: 2) {
                Text(value)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(textColor)
                Text(label)
                    .font(.caption)
                    .foregroundColor(theme.onSurfaceVariant)
            }
        }
    }
}

struct TrackPointsSection: View {
    let trackPoints: [TrackPoint]
    let theme: AppTheme

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 8) {
                Image(systemName: "list.bullet")
                    .foregroundColor(theme.primary)
                Text("Track Points (\(trackPoints.count))")
                    .font(.headline)
                    .fontWeight(.bold)
            }
        }
    }
}

struct TimeHeightChartView: View {
    let samples: [(Float, Float)]
    let theme: AppTheme

    var body: some View {
        if #available(iOS 16.0, *) {
            Chart {
                ForEach(samples.indices, id: \.self) { index in
                    LineMark(
                        x: .value("Time", samples[index].0),
                        y: .value("Altitude", samples[index].1)
                    )
                    .foregroundStyle(theme.primary)
                    .interpolationMethod(.catmullRom)
                    AreaMark(
                        x: .value("Time", samples[index].0),
                        y: .value("Altitude", samples[index].1)
                    )
                    .foregroundStyle(
                        LinearGradient(
                            colors: [
                                theme.primary.opacity(0.5),
                                theme.primary.opacity(0.1)
                            ],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    )
                    .interpolationMethod(.catmullRom)
                }
            }
            .chartXAxisLabel("Time (seconds)")
            .chartYAxisLabel("Altitude (meters)")
        } else {
            LegacyChartView(samples: samples, theme: theme)
        }
    }
}

struct LegacyChartView: View {
    let samples: [(Float, Float)]
    let theme: AppTheme

    var body: some View {
        GeometryReader { geometry in
            if !samples.isEmpty {
                Canvas { context, size in
                    let minX = samples.map { $0.0 }.min() ?? 0
                    let maxX = samples.map { $0.0 }.max() ?? 1
                    let minY = samples.map { $0.1 }.min() ?? 0
                    let maxY = samples.map { $0.1 }.max() ?? 1
                    let xRange = max(1, maxX - minX)
                    let yRange = max(1, maxY - minY)
                    let xScale = size.width / CGFloat(xRange)
                    let yScale = size.height / CGFloat(yRange)
                    var path = Path()
                    var firstPoint = true
                    for sample in samples {
                        let x = CGFloat(sample.0 - minX) * xScale
                        let y = size.height - CGFloat(sample.1 - minY) * yScale
                        if firstPoint {
                            path.move(to: CGPoint(x: x, y: y))
                            firstPoint = false
                        } else {
                            path.addLine(to: CGPoint(x: x, y: y))
                        }
                    }
                    context.stroke(
                        path,
                        with: .color(theme.primary),
                        lineWidth: 2
                    )
                    var areaPath = Path()
                    areaPath.addPath(path)
                    if let last = samples.last {
                        let lastX = CGFloat(last.0 - minX) * xScale
                        areaPath.addLine(to: CGPoint(x: lastX, y: size.height))
                    }
                    if let first = samples.first {
                        let firstX = CGFloat(first.0 - minX) * xScale
                        areaPath.addLine(to: CGPoint(x: firstX, y: size.height))
                    }
                    areaPath.closeSubpath()
                    let gradient = Gradient(colors: [
                        theme.primary.opacity(0.5),
                        theme.primary.opacity(0.1)
                    ])
                    context.fill(
                        areaPath,
                        with: .linearGradient(
                            gradient,
                            startPoint: CGPoint(x: 0, y: 0),
                            endPoint: CGPoint(x: 0, y: size.height)
                        )
                    )
                }
                VStack {
                    Spacer()
                    Text("Time (seconds)")
                        .font(.caption)
                        .foregroundColor(theme.onSurfaceVariant)
                }
                HStack {
                    Text("Altitude (meters)")
                        .font(.caption)
                        .foregroundColor(theme.onSurfaceVariant)
                        .rotationEffect(.degrees(-90))
                    Spacer()
                }
            } else {
                Text("No data available for chart")
                    .foregroundColor(theme.onSurfaceVariant)
            }
        }
    }
}


struct SessionErrorContent: View {
    let errorMessage: String
    let onRetryClick: () -> Void
    let theme: AppTheme
    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 64))
                .foregroundColor(theme.error)
            Text("Failed to Load Session")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(theme.error)
                .multilineTextAlignment(.center)
            Text(errorMessage)
                .font(.body)
                .foregroundColor(theme.onSurfaceVariant)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Button(action: onRetryClick) {
                HStack {
                    Image(systemName: "arrow.clockwise")
                    Text("Try Again")
                }
                .font(.headline)
                .foregroundColor(theme.onPrimary)
                .padding(.horizontal, 64)
                .padding(.vertical, 16)
                .background(theme.primary)
                .cornerRadius(8)
            }
            .padding(.top, 16)
            Spacer()
        }
        .padding()
    }
}

struct EditTitleView: View {
    let currentTitle: String
    let onSave: (String) -> Void
    let onCancel: () -> Void
    let theme: AppTheme

    @State private var titleText: String
    @Environment(\.presentationMode) var presentationMode
    
    init(currentTitle: String, onSave: @escaping (String) -> Void, onCancel: @escaping () -> Void, theme: AppTheme) {
        self.currentTitle = currentTitle
        self.onSave = onSave
        self.onCancel = onCancel
        self.theme = theme
        _titleText = State(initialValue: currentTitle)
    }
    
    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Enter a new title for this climbing session:")
                        .font(.body)
                        .foregroundColor(theme.onSurfaceVariant)
                    TextField("Session Title", text: $titleText)
                        .padding()
                        .background(theme.surfaceVariant.opacity(0.3))
                        .cornerRadius(8)
                }
                .padding(.horizontal)
                Spacer()
            }
            .padding(.top, 20)
            .navigationTitle("Edit Session Title")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        onCancel()
                        presentationMode.wrappedValue.dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        if !titleText.isEmpty {
                            onSave(titleText)
                        }
                        presentationMode.wrappedValue.dismiss()
                    }
                    .disabled(titleText.isEmpty)
                }
            }
        }
    }
}


// MARK: - Helper Functions

func calculateSessionDuration(_ sessionDetails: SessionDetails) -> String {
    let points = sessionDetails.points
    if points.isEmpty { return "N/A" }
    
    let startTime = points.first?.timestamp ?? 0
    let endTime = points.last?.timestamp ?? 0
    let durationMs = endTime - startTime
    
    return formatDuration(durationMs)
}

func formatDuration(_ durationMs: Int64) -> String {
    let seconds = (durationMs / 1000) % 60
    let minutes = (durationMs / (1000 * 60)) % 60
    let hours = (durationMs / (1000 * 60 * 60))
    
    if hours > 0 {
        return String(format: "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        return String(format: "%02d:%02d", minutes, seconds)
    }
}

func prepareChartData(points: [TrackPoint]) -> [(Float, Float)] {
    if points.isEmpty { return [] }
    
    // Filter points that have altitude data (including 0 values)
    let pointsWithAltitude = points.filter {
        $0.altitude != nil || $0.relAltitude != 0.0
    }
    if pointsWithAltitude.isEmpty { return [] }
    
    let sessionStartTime = pointsWithAltitude.first?.timestamp ?? 0
    
    // If we have 50 or fewer points, use all of them
    if pointsWithAltitude.count <= 50 {
        return pointsWithAltitude.map { point in
            let timeFromStart = Float((point.timestamp - sessionStartTime) / 1000)
            let alt = Double(truncating: point.altitude ?? KotlinDouble(value: 0.0))
            let altitudeValue: Double = (alt != 0.0) ? alt : point.relAltitude
            let altitude = Float(altitudeValue)
            return (timeFromStart, altitude)
        }
    }

    // If we have more than 50 points, aggregate them
    let step = pointsWithAltitude.count / 50
    var aggregatedPoints: [(Float, Float)] = []

    for i in 0..<50 {
        let startIndex = i * step
        let endIndex = min((i + 1) * step, pointsWithAltitude.count)
        let range = startIndex..<endIndex
        let pointsInRange = Array(pointsWithAltitude[range])

        if !pointsInRange.isEmpty {
            // Use the middle point's timestamp for time calculation
            let middlePoint = pointsInRange[pointsInRange.count / 2]
            let timeFromStart = Float((middlePoint.timestamp - sessionStartTime) / 1000)

            let altitudes: [Double] = pointsInRange.map { point in
                let alt = Double(truncating: point.altitude ?? KotlinDouble(value: 0.0))
                let relAlt = point.relAltitude
                return (alt != 0.0) ? alt : relAlt
            }

            let sumAltitudes: Double = altitudes.reduce(0.0, +)
            let averageAltitude: Float = altitudes.isEmpty ? 0.0 : Float(sumAltitudes / Double(altitudes.count))

            aggregatedPoints.append((timeFromStart, averageAltitude))
        }
    }

    return aggregatedPoints
}
