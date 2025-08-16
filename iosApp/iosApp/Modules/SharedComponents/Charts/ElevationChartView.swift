import SwiftUI
import Shared
import Charts

struct ElevationChartView: View {
    let trackPoints: [TrackPoint]
    
    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

    var body: some View {
        FullRoundedCard {
            VStack(alignment: .leading, spacing: 16) {
                Text("Elevation Profile")
                    .font(.headline)
                    .foregroundColor(theme.onSurface)

                Divider()
                    .background(theme.surfaceVariant)

                if #available(iOS 16.0, *) {
                    Chart {
                        ForEach(elevationData, id: \.distance) { point in
                            LineMark(
                                x: .value("Distance", point.distance),
                                y: .value("Elevation", point.elevation)
                            )
                            .foregroundStyle(theme.primary)
                            .interpolationMethod(.catmullRom)
                            
                            AreaMark(
                                x: .value("Distance", point.distance),
                                y: .value("Elevation", point.elevation)
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
                    .chartYAxis {
                        AxisMarks { _ in
                            AxisGridLine()
                                .foregroundStyle(theme.surfaceVariant)
                            AxisTick()
                                .foregroundStyle(theme.onSurfaceVariant)
                            AxisValueLabel()
                                .foregroundStyle(theme.onSurfaceVariant)
                        }
                    }
                    .chartXAxis {
                        AxisMarks { _ in
                            AxisGridLine()
                                .foregroundStyle(theme.surfaceVariant)
                            AxisTick()
                                .foregroundStyle(theme.onSurfaceVariant)
                            AxisValueLabel()
                                .foregroundStyle(theme.onSurfaceVariant)
                        }
                    }
                    .frame(height: 200)
                } else {
                    LegacyElevationChart(elevationData: elevationData, theme: theme)
                }
                
                HStack {
                    Text("Min: \(String(format: "%.1f m", minElevation))")
                        .font(.caption)
                        .foregroundColor(theme.onSurfaceVariant)

                    Spacer()
                    
                    Text("Max: \(String(format: "%.1f m", maxElevation))")
                        .font(.caption)
                        .foregroundColor(theme.onSurfaceVariant)
                }
            }
        }
    }
    
    private var elevationData: [ElevationPoint] {
        var cumulativeDistance: Double = 0
        var previousPoint: TrackPoint? = nil
        
        return trackPoints.compactMap { point in
            if let prev = previousPoint {
                let distance = calculateDistance(from: prev, to: point)
                cumulativeDistance += distance
            }
            
            previousPoint = point
            
            return ElevationPoint(
                distance: cumulativeDistance / 1000,
                elevation: point.altitude?.double ?? 0.0
            )
        }
    }
    
    private var minElevation: Double {
        trackPoints.compactMap { $0.altitude?.double }.min() ?? 0
    }
    
    private var maxElevation: Double {
        trackPoints.compactMap { $0.altitude?.double }.max() ?? 0
    }
    
    private func calculateDistance(from point1: TrackPoint, to point2: TrackPoint) -> Double {

        let lat1 = (point1.latitude?.double ?? 0.0) * Double.pi / 180
        let lon1 = (point1.longitude?.double ?? 0.0) * Double.pi / 180
        let lat2 = (point2.latitude?.double ?? 0.0) * Double.pi / 180
        let lon2 = (point2.longitude?.double ?? 0.0) * Double.pi / 180
        
        let dlon = lon2 - lon1
        let dlat = lat2 - lat1
        
        let a = pow(sin(dlat/2), 2) + cos(lat1) * cos(lat2) * pow(sin(dlon/2), 2)
        let c = 2 * asin(sqrt(a))
        let r = 6371000.0 // Earth radius in meters
        
        return c * r
    }
}

struct ElevationPoint {
    let distance: Double
    let elevation: Double
}

struct LegacyElevationChart: View {
    let elevationData: [ElevationPoint]
    let theme: AppTheme

    var body: some View {
        GeometryReader { geometry in
            Path { path in
                let width = geometry.size.width
                let height = geometry.size.height
                let maxElevation = elevationData.map { $0.elevation }.max() ?? 0
                let minElevation = elevationData.map { $0.elevation }.min() ?? 0
                let elevationRange = max(maxElevation - minElevation, 1)
                let _ = elevationData.map { $0.distance }.max() ?? 1
                
                let pointSpacing = width / CGFloat(elevationData.count - 1)
                
                if !elevationData.isEmpty {
                    let firstPoint = elevationData[0]
                    let yPosition = height - CGFloat((firstPoint.elevation - minElevation) / elevationRange) * height
                    path.move(to: CGPoint(x: 0, y: yPosition))
                    
                    for (index, point) in elevationData.enumerated().dropFirst() {
                        let xPosition = CGFloat(index) * pointSpacing
                        let yPosition = height - CGFloat((point.elevation - minElevation) / elevationRange) * height
                        path.addLine(to: CGPoint(x: xPosition, y: yPosition))
                    }
                }
            }
            .stroke(theme.primary, lineWidth: 2)

            Path { path in
                let width = geometry.size.width
                let height = geometry.size.height
                let maxElevation = elevationData.map { $0.elevation }.max() ?? 0
                let minElevation = elevationData.map { $0.elevation }.min() ?? 0
                let elevationRange = max(maxElevation - minElevation, 1)
                
                let pointSpacing = width / CGFloat(elevationData.count - 1)
                
                if !elevationData.isEmpty {
                    let firstPoint = elevationData[0]
                    let yPosition = height - CGFloat((firstPoint.elevation - minElevation) / elevationRange) * height
                    path.move(to: CGPoint(x: 0, y: yPosition))
                    
                    for (index, point) in elevationData.enumerated().dropFirst() {
                        let xPosition = CGFloat(index) * pointSpacing
                        let yPosition = height - CGFloat((point.elevation - minElevation) / elevationRange) * height
                        path.addLine(to: CGPoint(x: xPosition, y: yPosition))
                    }
                    
                    path.addLine(to: CGPoint(x: width, y: height))
                    path.addLine(to: CGPoint(x: 0, y: height))
                    path.closeSubpath()
                }
            }
            .fill(
                LinearGradient(
                    colors: [
                        theme.primary.opacity(0.5),
                        theme.primary.opacity(0.1)
                    ],
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
        }
    }
}
