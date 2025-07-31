import SwiftUI
import Shared
import Charts

struct SessionDetailsView: View {
    @ObservedObject private(set) var viewModel = ViewModelWrapper<SessionDetailsState, SessionDetailsViewModel>()
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue

    private var colors: TopOutColorScheme {
        (ThemePalette(rawValue: selectedTheme) ?? .classicRed).scheme(for: colorScheme)
    }
    
    @Environment(\.presentationMode) var presentationMode
    @State private var showDeleteConfirmation = false
    @State private var showEditTitleDialog = false
    @State private var editTitleText = ""
    
    init(sessionId: String) {
        self.viewModel.viewModel.loadSession(sessionId: sessionId)
    }
    
    var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()
            
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
                                .clipShape(RoundedRectangle(cornerRadius: 24))
                                .ignoresSafeArea(edges: .top)
                        }
                        
                        SessionTitleSection(
                            sessionDetails: state.sessionDetails,
                            onEditClick: {
                                editTitleText = state.sessionDetails.session.title ?? "Climbing Session"
                                showEditTitleDialog = true
                            },
                            colors: colors
                        )
                        
                        SessionInfoSection(
                            sessionDetails: state.sessionDetails,
                            onDeleteClick: { showDeleteConfirmation = true },
                            colors: colors
                        )
                        
                        VStack(spacing: 0) {
                            Text("Climbing Session")
                                .font(.system(size: 28, weight: .bold))
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 24)
                            
                            Divider()
                                .background(colors.outline.opacity(0.3))
                                .padding(.horizontal, 24)
                            
                            SessionStatisticsCard(sessionDetails: state.sessionDetails, colors: colors)
                                .padding(.top, 16)
                            
                            Divider()
                                .background(colors.outline.opacity(0.3))
                                .padding(.horizontal, 24)
                                .padding(.vertical, 16)
                            
                            if state.sessionDetails.points.count > 1 {
                                VStack(alignment: .leading, spacing: 12) {
                                    HStack {
                                        Image(systemName: "chart.xyaxis.line")
                                            .foregroundColor(colors.primary)
                                        Text("Altitude over Time")
                                            .font(.headline)
                                            .fontWeight(.bold)
                                    }
                                    .padding(.horizontal, 16)
                                    
                                    TimeHeightChartView(
                                        samples: prepareChartData(points: state.sessionDetails.points),
                                        colors: colors
                                    )
                                    .frame(height: 200)
                                    .padding(.horizontal, 16)
                                }
                                .padding(.vertical, 8)
                            }
                            
                            TrackPointsSection(trackPoints: state.sessionDetails.points, colors: colors)
                                .padding(16)
                            Spacer(minLength: 80)
                        }
                        .background(colors.surfaceContainer)
                        .cornerRadius(24, corners: [.bottomLeft, .bottomRight])
                    }
                }
                
            case .error(let state):
                SessionErrorContent(
                    errorMessage: state.errorMessage,
                    onRetryClick: {
                        if let session = viewModel.viewModel.uiState.value as? SessionDetailsState.Loaded {
                            viewModel.viewModel.loadSession(sessionId: session.sessionDetails.session.id)
                        }
                    },
                    colors: colors
                )
            }
        }
        .navigationTitle("Session Details")
        .alert(isPresented: $showDeleteConfirmation) {
            Alert(
                title: Text("Delete Session"),
                message: Text("This action cannot be undone. Are you sure you want to delete this session?"),
                primaryButton: .destructive(Text("Delete")) {
                    // Only run delete on loaded state
                    switch onEnum(of: viewModel.uiState) {
                    case .loaded(let loadedState):
                        viewModel.viewModel.deleteSession(sessionId: loadedState.sessionDetails.session.id)
                        presentationMode.wrappedValue.dismiss()
                    default:
                        break
                    }
                },
                secondaryButton: .cancel()
            )
        }
        .sheet(isPresented: $showEditTitleDialog) {
            EditTitleView(
                currentTitle: editTitleText,
                onSave: { newTitle in
                    switch onEnum(of: viewModel.uiState) {
                    case .loaded(let state):
                        viewModel.viewModel.updateSessionTitle(sessionId: state.sessionDetails.session.id, newTitle: newTitle)
                    default:
                        break
                    }
                },
                onCancel: { showEditTitleDialog = false },
                colors: colors
            )
        }
        .onAppear {
            viewModel.startObserving()
        }
    }
}

// All color usage in these components should use the passed `colors` parameter:

struct SessionTitleSection: View {
    let sessionDetails: SessionDetails
    let onEditClick: () -> Void
    let colors: TopOutColorScheme
    
    var body: some View {
        ZStack {
            VStack {
                Rectangle()
                    .fill(Color.clear)
                    .frame(height: 15)
                RoundedRectangle(cornerRadius: 24)
                    .fill(colors.surfaceContainer)
                    .cornerRadius(0, corners: [.topLeft, .topRight])
                    .cornerRadius(24, corners: [.bottomLeft, .bottomRight])
            }
            HStack {
                Text("Title: ")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(colors.onSurfaceVariant)
                Text(sessionDetails.session.title ?? "Climbing Session")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(colors.onSurface)
                Spacer()
                Button(action: onEditClick) {
                    Image(systemName: "pencil")
                        .foregroundColor(colors.primary)
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
    let colors: TopOutColorScheme
    
    var body: some View {
        HStack(spacing: 0) {
            VStack(spacing: 4) {
                Text(
                    sessionDetails.session.startTime.map { formatDate(Int64(truncating: $0)) } ?? "-"
                )
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(colors.onSurface)
                Text(
                    sessionDetails.session.startTime.map { formatDate(Int64(truncating: $0)) } ?? "-"
                )
                .font(.subheadline)
                    .foregroundColor(colors.onSurfaceVariant)
            }
            .frame(maxWidth: .infinity)
            VStack(spacing: 4) {
                Text(calculateSessionDuration(sessionDetails))
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(colors.onSurface)
                Text("Duration")
                    .font(.subheadline)
                    .foregroundColor(colors.onSurfaceVariant)
            }
            .frame(maxWidth: .infinity)
            VStack(spacing: 4) {
                Button(action: onDeleteClick) {
                    Circle()
                        .fill(colors.errorContainer)
                        .frame(width: 40, height: 40)
                        .overlay(
                            Image(systemName: "trash")
                                .foregroundColor(colors.onErrorContainer)
                        )
                }
                Text("Delete")
                    .font(.caption)
                    .foregroundColor(colors.error)
            }
            .frame(maxWidth: .infinity)
        }
        .padding(.vertical, 10)
        .background(colors.surfaceContainer)
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
    let colors: TopOutColorScheme
    
    var body: some View {
        VStack(spacing: 16) {
            HStack(spacing: 0) {
                StatisticItemWithIcon(
                    icon: "arrow.up",
                    label: "Max Altitude",
                    value: String(format: "%.1f m", sessionDetails.points.map { $0.altitude?.doubleValueOrZero ?? 0.0 }.max() ?? 0.0),
                    colors: colors
                )
                Spacer()
                StatisticItemWithIcon(
                    icon: "arrow.down",
                    label: "Min Altitude",
                    value: String(format: "%.1f m", sessionDetails.points.map { $0.altitude?.doubleValueOrZero ?? 0.0 }.min() ?? 0.0),
                    colors: colors
                )
                Spacer()
                StatisticItemWithIcon(
                    icon: "chart.line.uptrend.xyaxis",
                    label: "Total Gain",
                    value: String(format: "%.1f m", sessionDetails.points.last?.gain ?? 0.0),
                    textColor: .green,
                    colors: colors
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
                    colors: colors
                )
                Spacer()
                Spacer()
                StatisticItemWithIcon(
                    icon: "speedometer",
                    label: "Max Speed",
                    value: String(format: "%.1f m/s", sessionDetails.points.map { $0.vTotal }.max() ?? 0.0),
                    colors: colors
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
    let colors: TopOutColorScheme
    
    var body: some View {
        HStack(alignment: .center, spacing: 8) {
            Image(systemName: icon)
                .foregroundColor(colors.primary)
                .font(.system(size: 18))
            VStack(alignment: .leading, spacing: 2) {
                Text(value)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(textColor)
                Text(label)
                    .font(.caption)
                    .foregroundColor(colors.onSurfaceVariant)
            }
        }
    }
}

struct TrackPointsSection: View {
    let trackPoints: [TrackPoint]
    let colors: TopOutColorScheme
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 8) {
                Image(systemName: "list.bullet")
                    .foregroundColor(colors.primary)
                Text("Track Points (\(trackPoints.count))")
                    .font(.headline)
                    .fontWeight(.bold)
            }
        }
    }
}

struct TimeHeightChartView: View {
    let samples: [(Float, Float)]
    let colors: TopOutColorScheme
    
    var body: some View {
        if #available(iOS 16.0, *) {
            Chart {
                ForEach(samples.indices, id: \.self) { index in
                    LineMark(
                        x: .value("Time", samples[index].0),
                        y: .value("Altitude", samples[index].1)
                    )
                    .foregroundStyle(colors.primary)
                    .interpolationMethod(.catmullRom)
                    AreaMark(
                        x: .value("Time", samples[index].0),
                        y: .value("Altitude", samples[index].1)
                    )
                    .foregroundStyle(
                        LinearGradient(
                            colors: [
                                colors.primary.opacity(0.5),
                                colors.primary.opacity(0.1)
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
            LegacyChartView(samples: samples, colors: colors)
        }
    }
}

struct LegacyChartView: View {
    let samples: [(Float, Float)]
    let colors: TopOutColorScheme
    
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
                        with: .color(colors.primary),
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
                        colors.primary.opacity(0.5),
                        colors.primary.opacity(0.1)
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
                        .foregroundColor(colors.onSurfaceVariant)
                }
                HStack {
                    Text("Altitude (meters)")
                        .font(.caption)
                        .foregroundColor(colors.onSurfaceVariant)
                        .rotationEffect(.degrees(-90))
                    Spacer()
                }
            } else {
                Text("No data available for chart")
                    .foregroundColor(colors.onSurfaceVariant)
            }
        }
    }
}


struct SessionErrorContent: View {
    let errorMessage: String
    let onRetryClick: () -> Void
    let colors: TopOutColorScheme
    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 64))
                .foregroundColor(colors.error)
            Text("Failed to Load Session")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(colors.error)
                .multilineTextAlignment(.center)
            Text(errorMessage)
                .font(.body)
                .foregroundColor(colors.onSurfaceVariant)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Button(action: onRetryClick) {
                HStack {
                    Image(systemName: "arrow.clockwise")
                    Text("Try Again")
                }
                .font(.headline)
                .foregroundColor(colors.onPrimary)
                .padding(.horizontal, 64)
                .padding(.vertical, 16)
                .background(colors.primary)
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
    let colors: TopOutColorScheme
    
    @State private var titleText: String
    @Environment(\.presentationMode) var presentationMode
    
    init(currentTitle: String, onSave: @escaping (String) -> Void, onCancel: @escaping () -> Void, colors: TopOutColorScheme) {
        self.currentTitle = currentTitle
        self.onSave = onSave
        self.onCancel = onCancel
        self.colors = colors
        _titleText = State(initialValue: currentTitle)
    }
    
    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Enter a new title for this climbing session:")
                        .font(.body)
                        .foregroundColor(colors.onSurfaceVariant)
                    TextField("Session Title", text: $titleText)
                        .padding()
                        .background(colors.surfaceVariant.opacity(0.3))
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
    
    // Filter points that have altitude data
    let pointsWithAltitude = points.filter { $0.altitude != 0.0 || $0.relAltitude != 0.0 }
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

            // ---- FIXED MAP BELOW ----
            let altitudes: [Double] = pointsInRange.map { point in
                let alt = Double(truncating: point.altitude ?? 0)
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
