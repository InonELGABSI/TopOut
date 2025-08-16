import SwiftUI
import MapKit
import Shared

struct ActiveSessionContent: View {
    let trackPoint:        TrackPoint
    let trackPoints:       [TrackPoint]
    @Binding var mapRegion: MKCoordinateRegion
    let onStopClicked:     () -> Void
    let onCancelClicked:   () -> Void
    let onPauseClicked:    () -> Void
    let onResumeClicked:   () -> Void
    let isPaused:          Bool
    let theme:            AppTheme
    @State private var isFollowingLast = true

    private let overlap: CGFloat = 18
    
    var body: some View {
        ZStack(alignment: .top) {
            // Background
            theme.background
                .ignoresSafeArea()

            VStack {
                Spacer()
                    .frame(height: UIScreen.main.bounds.height * 0.45)

                LiveDataCard(trackPoint: trackPoint, theme: theme, isPaused: isPaused)
                    .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: -4)

                Spacer()
            }

            VStack {
                MapView(trackPoints: trackPoints, region: $mapRegion)
                    .frame(height: UIScreen.main.bounds.height * 0.50)
                    .clipShape(.rect(bottomLeadingRadius: 24, bottomTrailingRadius: 24))
                    .shadow(color: .black.opacity(0.15), radius: 12, x: 0, y: 8)
                    .ignoresSafeArea(edges: .top)

                Spacer()
            }

            VStack {
                Spacer()
                BottomControls(
                    onStopClicked: onStopClicked,
                    onCancelClicked: onCancelClicked,
                    onPauseClicked: onPauseClicked,
                    onResumeClicked: onResumeClicked,
                    isPaused: isPaused
                )
                .padding(.horizontal, 16)
                .padding(.bottom, 40)
            }
        }
    }
}






private struct RoundedCornerBackground: View {
    let color: Color
    let topLeft: CGFloat
    let topRight: CGFloat
    let bottomLeft: CGFloat
    let bottomRight: CGFloat
    
    var body: some View {
        GeometryReader { geo in
            RoundedRectangle(cornerRadius: 0)
                .fill(color)
                .overlay(
                    Path { path in
                        let w = geo.size.width, h = geo.size.height
                        path.move(to: CGPoint(x: 0, y: 0))
                        path.addLine(to: CGPoint(x: w, y: 0))
                        path.addLine(to: CGPoint(x: w, y: h - bottomRight))
                        path.addArc(
                            center: CGPoint(x: w - bottomRight, y: h - bottomRight),
                            radius: bottomRight,
                            startAngle: .degrees(0),
                            endAngle: .degrees(90),
                            clockwise: false
                        )
                        path.addLine(to: CGPoint(x: bottomLeft, y: h))
                        path.addArc(
                            center: CGPoint(x: bottomLeft, y: h - bottomLeft),
                            radius: bottomLeft,
                            startAngle: .degrees(90),
                            endAngle: .degrees(180),
                            clockwise: false
                        )
                        path.addLine(to: CGPoint(x: 0, y: 0))
                    }
                    .fill(color)
                )
        }
    }
}


private struct LiveDataCard: View {
    let trackPoint: TrackPoint
    let theme: AppTheme
    let isPaused: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            HeaderRow(timestamp: trackPoint.timestamp, isPaused: isPaused, theme: theme)

            VStack(spacing: 16) {
                LocationRow(trackPoint: trackPoint, theme: theme)
                SpeedAltitudeRow(trackPoint: trackPoint, theme: theme)
            }
        }
        .padding(.top, 60)
        .padding(.horizontal, 24)
        .padding(.bottom, 28)
        .background(
            RoundedRectangle(cornerRadius: 0)
                .fill(theme.primary)
                .clipShape(.rect(bottomLeadingRadius: 32, bottomTrailingRadius: 32))
        )
        .overlay(
            Rectangle()
                .fill(theme.onPrimary.opacity(0.1))
                .frame(height: 1)
                .frame(maxWidth: .infinity)
                .position(x: UIScreen.main.bounds.width / 2, y: 0)
        )
        .opacity(isPaused ? 0.6 : 1.0)
    }
}

private struct HeaderRow: View {
    let timestamp: Int64
    let isPaused: Bool
    let theme: AppTheme

    var body: some View {
        HStack {
            HStack(spacing: 8) {
                Circle()
                    .fill(isPaused ? Color.orange : Color.red)
                    .frame(width: 8, height: 8)
                Text(isPaused ? "Session Paused" : "Live Data")
                    .font(.title2).bold()
                    .foregroundColor(theme.onPrimary)
            }
            Spacer()
            Text(formatTime(timestamp))
                .font(.subheadline).fontWeight(.medium)
                .foregroundColor(theme.onPrimary.opacity(0.8))
        }
    }
    
    private func formatTime(_ ts: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(ts) / 1000)
        let f = DateFormatter(); f.dateFormat = "HH:mm:ss"
        return f.string(from: date)
    }
}

private struct LocationRow: View {
    let trackPoint: TrackPoint
    let theme: AppTheme

    var body: some View {
        HStack {
            Label("Location", systemImage: "location.fill")
                .font(.headline)
                .foregroundColor(theme.onPrimary)
            Spacer()
            DataTriplet(
                first:   formatted(trackPoint.latitude?.double, suffix: "°"),
                second:  formatted(trackPoint.longitude?.double, suffix: "°"),
                third:   formatted(trackPoint.altitude?.double, suffix: " m"),
                firstLab: "Lat",
                secondLab:"Lon",
                thirdLab: "MSE",
                theme: theme
            )
        }
    }
    
    private func formatted(_ v: Double?, suffix: String) -> String { v == nil ? "-" : String(format: "%.4f%@", v!, suffix) }
}

private struct SpeedAltitudeRow: View {
    let trackPoint: TrackPoint
    let theme: AppTheme

    var body: some View {
        HStack(spacing: 16) {
            StatCard(
                title: "Speed",
                icon: "speedometer",
                triplet: (
                    String(format: "%.1f", trackPoint.avgHorizontal), "Avg-H",
                    String(format: "%.1f", trackPoint.avgVertical),   "Avg-V",
                    "", ""  // Empty third column
                ),
                background: theme.secondaryContainer.opacity(0.3),
                theme: theme
            )
            StatCard(
                title: "Altitude",
                icon: "mountain.2",
                triplet: (
                    "\(Int(trackPoint.gain))", "Gain",
                    "\(Int(trackPoint.loss))", "Loss",
                    "\(Int(trackPoint.relAltitude))", "Rel"
                ),
                background: theme.tertiaryContainer.opacity(0.3),
                theme: theme
            )
        }
    }
}

private struct StatCard: View {
    let title: String
    let icon: String
    let triplet: (String,String,String,String,String,String)
    let background: Color
    let theme: AppTheme

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 14)).foregroundColor(theme.onPrimary)
                Text(title)
                    .font(.subheadline).bold()
                    .foregroundColor(theme.onPrimary)
            }
            DataTriplet(
                first:  triplet.0, second: triplet.2, third: triplet.4,
                firstLab: triplet.1, secondLab: triplet.3, thirdLab: triplet.5,
                theme: theme
            )
        }
        .padding(12)
        .background(theme.onPrimary.opacity(0.15))
        .cornerRadius(12)
        .frame(maxWidth: .infinity)
    }
}

private struct DataTriplet: View {
    let first: String; let second: String; let third: String
    let firstLab: String; let secondLab: String; let thirdLab: String
    let theme: AppTheme

    var body: some View {
        HStack {
            ValueLabel(value: first, label: firstLab, theme: theme)
            ValueLabel(value: second, label: secondLab, theme: theme)
            ValueLabel(value: third, label: thirdLab, theme: theme)
        }
    }
}

private struct ValueLabel: View {
    let value: String; let label: String; let theme: AppTheme

    var body: some View {
        VStack {
            Text(value).font(.caption).bold()
                .foregroundColor(theme.onPrimary)
            Text(label).font(.caption2)
                .foregroundColor(theme.onPrimary.opacity(0.7))
        }.frame(maxWidth: .infinity)
    }
}


private struct BottomControls: View {
    let onStopClicked:   () -> Void
    let onCancelClicked: () -> Void
    let onPauseClicked:  () -> Void
    let onResumeClicked: () -> Void
    let isPaused:        Bool

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onCancelClicked) {
                HStack(spacing: 12) {
                    Image(systemName: "xmark")
                    Text("Cancel")
                }
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(
                    LinearGradient(
                        colors: [
                            Color(red: 0.95, green: 0.35, blue: 0.35),
                            Color(red: 0.80, green: 0.18, blue: 0.18)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .clipShape(RoundedRectangle(cornerRadius: 28))
            }
            .disabled(isPaused)
            .opacity(isPaused ? 0.45 : 1.0)

            Button(action: isPaused ? onResumeClicked : onPauseClicked) {
                HStack(spacing: 12) {
                    Image(systemName: isPaused ? "play.fill" : "pause.fill")
                    Text(isPaused ? "Resume" : "Pause")
                }
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(
                    LinearGradient(
                        colors: [
                            Color(red: 0.30, green: 0.70, blue: 0.35),
                            Color(red: 0.22, green: 0.55, blue: 0.24)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .clipShape(RoundedRectangle(cornerRadius: 28))
            }

            Button(action: onStopClicked) {
                HStack(spacing: 12) {
                    Image(systemName: "stop.fill")
                    Text("Stop & Save")
                }
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(
                    LinearGradient(
                        colors: [
                            Color(red: 0.30, green: 0.70, blue: 0.35),
                            Color(red: 0.22, green: 0.55, blue: 0.24)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .clipShape(RoundedRectangle(cornerRadius: 28))
            }
            .disabled(isPaused)
            .opacity(isPaused ? 0.45 : 1.0)
        }
    }
}
