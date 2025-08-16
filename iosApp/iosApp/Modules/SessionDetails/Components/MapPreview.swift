import SwiftUI
import MapKit
import Shared


struct MapPreview: View {
    let trackPoints: [TrackPoint]
    @State private var cameraPosition: MapCameraPosition

    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

    private static let overlayLineWidth: CGFloat      = 3
    private static let segmentThresholdMeters: Double = 500

    init(trackPoints: [TrackPoint]) {
        self.trackPoints = trackPoints

        let coords = trackPoints.compactMap { point -> CLLocationCoordinate2D? in
            guard
                let lat = point.latitude?.double,
                let lon = point.longitude?.double
            else { return nil }
            return CLLocationCoordinate2D(latitude: lat, longitude: lon)
        }

        let region: MKCoordinateRegion = {
            if coords.count > 1 { return .fitting(coords) }
            return MKCoordinateRegion(
                center: coords.first ?? .init(latitude: 0, longitude: 0),
                span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
            )
        }()

        _cameraPosition = State(initialValue: .region(region))
    }

    var body: some View {
        ZStack {
            Map(position: $cameraPosition) {
                ForEach(routeOverlays.indices, id: \.self) { index in
                    MapPolyline(coordinates: routeOverlays[index])
                        .stroke(theme.primary, lineWidth: Self.overlayLineWidth)
                }

                if let start = routeOverlays.first?.first {
                    Marker("Start", coordinate: start)
                        .tint(.green)
                }
                if let start = routeOverlays.first?.first,
                   let end = routeOverlays.last?.last,
                   !coordinatesEqual(start, end) {
                    Marker("End", coordinate: end)
                        .tint(.red)
                }
            }
            .mapStyle(.standard(elevation: .flat))
            .mapControls {
                MapCompass()
                MapUserLocationButton()
            }
            .onAppear {
                print("Map has appeared.")
            }
            .onChange(of: cameraPosition) { _,newPosition in
                print("Camera position updated: \(newPosition)")
            }
        }
    }

    private var routeOverlays: [[CLLocationCoordinate2D]] {
        let coords = trackPoints.compactMap { point -> CLLocationCoordinate2D? in
            guard
                let lat = point.latitude?.double,
                let lon = point.longitude?.double
            else { return nil }
            return CLLocationCoordinate2D(latitude: lat, longitude: lon)
        }
        guard !coords.isEmpty else { return [] }

        var segments: [[CLLocationCoordinate2D]] = []
        var current: [CLLocationCoordinate2D] = []

        for (idx, coord) in coords.enumerated() {
            if idx > 0 {
                let prev = coords[idx - 1]
                if prev.distance(to: coord) > Self.segmentThresholdMeters {
                    if !current.isEmpty { segments.append(current); current.removeAll(keepingCapacity: true) }
                }
            }
            current.append(coord)
        }
        if !current.isEmpty { segments.append(current) }
        return segments
    }
}

private extension MKCoordinateRegion {
    static func fitting(_ coords: [CLLocationCoordinate2D]) -> MKCoordinateRegion {
        guard !coords.isEmpty else {
            return MKCoordinateRegion(
                center: .init(latitude: 0, longitude: 0),
                span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
            )
        }
        let lats  = coords.map(\.latitude)
        let lons  = coords.map(\.longitude)
        guard let minLat = lats.min(), let maxLat = lats.max(),
              let minLon = lons.min(), let maxLon = lons.max() else {
            return MKCoordinateRegion(
                center: coords[0],
                span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
            )
        }
        let center = CLLocationCoordinate2D(
            latitude: (minLat + maxLat) / 2,
            longitude: (minLon + maxLon) / 2
        )
        let span = MKCoordinateSpan(
            latitudeDelta: max((maxLat - minLat) * 1.2, 0.01),
            longitudeDelta: max((maxLon - minLon) * 1.2, 0.01)
        )
        return MKCoordinateRegion(center: center, span: span)
    }
}

private extension CLLocationCoordinate2D {
    func distance(to other: CLLocationCoordinate2D) -> Double {
        CLLocation(latitude: latitude, longitude: longitude)
            .distance(from: CLLocation(latitude: other.latitude, longitude: other.longitude))
    }
}

private func coordinatesEqual(_ lhs: CLLocationCoordinate2D?, _ rhs: CLLocationCoordinate2D?) -> Bool {
    guard let a = lhs, let b = rhs else { return false }
    return a.latitude == b.latitude && a.longitude == b.longitude
}
