import SwiftUI
import Shared
import Charts

struct ElevationChartView: View {
    let trackPoints: [TrackPoint]
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    var body: some View {
        FullRoundedCard {
            VStack(alignment: .leading, spacing: 16) {
                Text("Elevation Profile")
                    .font(.headline)
                    .foregroundColor(colors.onSurface)
                
                Divider()
                    .background(colors.surfaceVariant)
                
                if #available(iOS 16.0, *) {
                    Chart {
                        ForEach(elevationData, id: \.distance) { point in
                            LineMark(
                                x: .value("Distance", point.distance),
                                y: .value("Elevation", point.elevation)
                            )
                            .foregroundStyle(colors.primary)
                            .interpolationMethod(.catmullRom)
                            
                            AreaMark(
                                x: .value("Distance", point.distance),
                                y: .value("Elevation", point.elevation)
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
                    .chartYAxis {
                        AxisMarks { _ in
                            AxisGridLine()
                                .foregroundStyle(colors.surfaceVariant)
                            AxisTick()
                                .foregroundStyle(colors.onSurfaceVariant)
                            AxisValueLabel()
                                .foregroundStyle(colors.onSurfaceVariant)
                        }
                    }
                    .chartXAxis {
                        AxisMarks { _ in
                            AxisGridLine()
                                .foregroundStyle(colors.surfaceVariant)
                            AxisTick()
                                .foregroundStyle(colors.onSurfaceVariant)
                            AxisValueLabel()
                                .foregroundStyle(colors.onSurfaceVariant)
                        }
                    }
                    .frame(height: 200)
                } else {
                    // Fallback for iOS 15
                    LegacyElevationChart(elevationData: elevationData)
                }
                
                HStack {
                    Text("Min: \(String(format: "%.1f m", minElevation))")
                        .font(.caption)
                        .foregroundColor(colors.onSurfaceVariant)
                    
                    Spacer()
                    
                    Text("Max: \(String(format: "%.1f m", maxElevation))")
                        .font(.caption)
                        .foregroundColor(colors.onSurfaceVariant)
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
                distance: cumulativeDistance / 1000, // Convert to km
                elevation: point.altitude?.doubleValue ?? 0.0
            )
        }
    }
    
    private var minElevation: Double {
        trackPoints.compactMap { $0.altitude?.doubleValue }.min() ?? 0
    }
    
    private var maxElevation: Double {
        trackPoints.compactMap { $0.altitude?.doubleValue }.max() ?? 0
    }
    
    private func calculateDistance(from point1: TrackPoint, to point2: TrackPoint) -> Double {
        // Simple distance calculation between two GPS coordinates
        // Using Haversine formula
        let lat1 = (point1.latitude?.doubleValue ?? 0.0) * Double.pi / 180
        let lon1 = (point1.longitude?.doubleValue ?? 0.0) * Double.pi / 180
        let lat2 = (point2.latitude?.doubleValue ?? 0.0) * Double.pi / 180
        let lon2 = (point2.longitude?.doubleValue ?? 0.0) * Double.pi / 180
        
        let dlon = lon2 - lon1
        let dlat = lat2 - lat1
        
        let a = pow(sin(dlat/2), 2) + cos(lat1) * cos(lat2) * pow(sin(dlon/2), 2)
        let c = 2 * asin(sqrt(a))
        let r = 6371000.0 // Earth radius in meters
        
        return c * r
    }
}

struct ElevationPoint {
    let distance: Double  // in km
    let elevation: Double // in meters
}

// Fallback chart for iOS 15
struct LegacyElevationChart: View {
    let elevationData: [ElevationPoint]
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    var body: some View {
        GeometryReader { geometry in
            Path { path in
                let width = geometry.size.width
                let height = geometry.size.height
                let maxElevation = elevationData.map { $0.elevation }.max() ?? 0
                let minElevation = elevationData.map { $0.elevation }.min() ?? 0
                let elevationRange = max(maxElevation - minElevation, 1)
                let maxDistance = elevationData.map { $0.distance }.max() ?? 1
                
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
            .stroke(colors.primary, lineWidth: 2)
            
            // Add a gradient area below the line
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
                        colors.primary.opacity(0.5),
                        colors.primary.opacity(0.1)
                    ],
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
        }
    }
}
