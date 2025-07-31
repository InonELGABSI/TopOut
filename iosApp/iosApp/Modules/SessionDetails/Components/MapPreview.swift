import SwiftUI
import MapKit
import Shared

struct MapPreview: View {
    let trackPoints: [TrackPoint]
    @State private var region: MKCoordinateRegion
    @EnvironmentObject var themeManager: ThemeManager
    
    init(trackPoints: [TrackPoint]) {
        let initialCoordinate = trackPoints.first.map { 
            CLLocationCoordinate2D(latitude: $0.latitude, longitude: $0.longitude) 
        } ?? CLLocationCoordinate2D(latitude: 0, longitude: 0)
        
        // Calculate the region that encompasses all track points
        var minLat = Double.greatestFiniteMagnitude
        var maxLat = -Double.greatestFiniteMagnitude
        var minLon = Double.greatestFiniteMagnitude
        var maxLon = -Double.greatestFiniteMagnitude
        
        for point in trackPoints {
            minLat = min(minLat, point.latitude)
            maxLat = max(maxLat, point.latitude)
            minLon = min(minLon, point.longitude)
            maxLon = max(maxLon, point.longitude)
        }
        
        // Add some padding to the region
        let latDelta = (maxLat - minLat) * 1.2
        let lonDelta = (maxLon - minLon) * 1.2
        
        let centerLat = (maxLat + minLat) / 2
        let centerLon = (maxLon + minLon) / 2
        
        self._region = State(initialValue: MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: centerLat, longitude: centerLon),
            span: MKCoordinateSpan(latitudeDelta: max(latDelta, 0.01), longitudeDelta: max(lonDelta, 0.01))
        ))
    }
    
    var body: some View {
        Map(coordinateRegion: $region, showsUserLocation: false, annotationItems: mapAnnotations) { item in
            MapAnnotation(coordinate: item.coordinate) {
                if item.isStart {
                    Circle()
                        .fill(Color.green)
                        .frame(width: 12, height: 12)
                        .overlay(
                            Circle()
                                .stroke(Color.white, lineWidth: 2)
                        )
                } else if item.isEnd {
                    Circle()
                        .fill(Color.red)
                        .frame(width: 12, height: 12)
                        .overlay(
                            Circle()
                                .stroke(Color.white, lineWidth: 2)
                        )
                }
            }
        }
        .overlay(
            ZStack {
                ForEach(routeOverlays.indices, id: \.self) { index in
                    RouteOverlay(coordinates: routeOverlays[index])
                        .stroke(themeManager.colorScheme.primary, lineWidth: 3)
                }
            }
        )
    }
    
    // Create start and end annotations
    private var mapAnnotations: [MapAnnotationItem] {
        var items: [MapAnnotationItem] = []
        
        if let first = trackPoints.first {
            items.append(MapAnnotationItem(
                coordinate: CLLocationCoordinate2D(latitude: first.latitude, longitude: first.longitude),
                isStart: true,
                isEnd: false
            ))
        }
        
        if let last = trackPoints.last, trackPoints.count > 1 {
            items.append(MapAnnotationItem(
                coordinate: CLLocationCoordinate2D(latitude: last.latitude, longitude: last.longitude),
                isStart: false,
                isEnd: true
            ))
        }
        
        return items
    }
    
    // Create a route overlay
    private var routeOverlays: [[CLLocationCoordinate2D]] {
        let coordinates = trackPoints.map { point in
            CLLocationCoordinate2D(latitude: point.latitude, longitude: point.longitude)
        }
        
        // Split into multiple segments if we have gaps in tracking
        var segments: [[CLLocationCoordinate2D]] = []
        var currentSegment: [CLLocationCoordinate2D] = []
        
        for (index, coordinate) in coordinates.enumerated() {
            if index > 0 {
                let previousCoordinate = coordinates[index - 1]
                let distance = calculateDistance(from: previousCoordinate, to: coordinate)
                
                // If distance between consecutive points is too large, start a new segment
                if distance > 500 { // 500 meters threshold
                    if !currentSegment.isEmpty {
                        segments.append(currentSegment)
                        currentSegment = []
                    }
                }
            }
            
            currentSegment.append(coordinate)
        }
        
        if !currentSegment.isEmpty {
            segments.append(currentSegment)
        }
        
        return segments
    }
    
    private func calculateDistance(from coord1: CLLocationCoordinate2D, to coord2: CLLocationCoordinate2D) -> Double {
        let location1 = CLLocation(latitude: coord1.latitude, longitude: coord1.longitude)
        let location2 = CLLocation(latitude: coord2.latitude, longitude: coord2.longitude)
        return location1.distance(from: location2)
    }
}

struct MapAnnotationItem: Identifiable {
    let id = UUID()
    let coordinate: CLLocationCoordinate2D
    let isStart: Bool
    let isEnd: Bool
}

struct RouteOverlay: Shape {
    let coordinates: [CLLocationCoordinate2D]
    
    func path(in rect: CGRect) -> Path {
        var path = Path()
        
        guard coordinates.count > 1 else { return path }
        
        let maxLat = coordinates.map { $0.latitude }.max() ?? 0
        let minLat = coordinates.map { $0.latitude }.min() ?? 0
        let maxLon = coordinates.map { $0.longitude }.max() ?? 0
        let minLon = coordinates.map { $0.longitude }.min() ?? 0
        
        let latRange = maxLat - minLat
        let lonRange = maxLon - minLon
        
        // Initial point
        let firstCoord = coordinates[0]
        let firstX = (firstCoord.longitude - minLon) / lonRange * rect.width
        let firstY = (1 - (firstCoord.latitude - minLat) / latRange) * rect.height
        path.move(to: CGPoint(x: firstX, y: firstY))
        
        // Subsequent points
        for i in 1..<coordinates.count {
            let coord = coordinates[i]
            let x = (coord.longitude - minLon) / lonRange * rect.width
            let y = (1 - (coord.latitude - minLat) / latRange) * rect.height
            path.addLine(to: CGPoint(x: x, y: y))
        }
        
        return path
    }
}
