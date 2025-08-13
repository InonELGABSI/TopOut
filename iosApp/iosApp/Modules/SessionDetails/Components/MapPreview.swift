import SwiftUI
import MapKit
import Shared

/// A lightweight, read‑only map preview that shows a recorded track with
/// optional start / end markers. This view **never** mutates state coming from
/// the model layer – it only reflects what it is given via `trackPoints`.
struct MapPreview: View {
    // MARK: ‑ Public API
    let trackPoints: [TrackPoint]

    // MARK: ‑ Private State
    @State private var cameraPosition: MapCameraPosition

    // MARK: ‑ Environment
    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

    // MARK: ‑ Styling
    private static let overlayLineWidth: CGFloat      = 3
    private static let segmentThresholdMeters: Double = 500

    // MARK: ‑ Init
    init(trackPoints: [TrackPoint]) {
        self.trackPoints = trackPoints

        // Extract coordinates once so we can use them for the initial region
        let coords = trackPoints.compactMap { point -> CLLocationCoordinate2D? in
            guard
                let lat = point.latitude?.double,
                let lon = point.longitude?.double
            else { return nil }
            return CLLocationCoordinate2D(latitude: lat, longitude: lon)
        }

        let region: MKCoordinateRegion = {
            // Multiple points → zoom to fit. Fallbacks to sensible defaults.
            if coords.count > 1 { return .fitting(coords) }
            return MKCoordinateRegion(
                center: coords.first ?? .init(latitude: 0, longitude: 0),
                span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
            )
        }()

        _cameraPosition = State(initialValue: .region(region))
    }

    // MARK: ‑ View
    var body: some View {
        ZStack {
            // MARK: Map
            Map(position: $cameraPosition) {
                // Native MapKit polylines for the route
                ForEach(routeOverlays.indices, id: \.self) { index in
                    MapPolyline(coordinates: routeOverlays[index])
                        .stroke(theme.primary, lineWidth: Self.overlayLineWidth)
                }

                // Markers
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
                // Log and handle tile loading issues here if needed
            }
        }
    }

    // MARK: ‑ Segmented polyline (split when GPS gaps are >500 m)
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

// MARK: ‑ MKCoordinateRegion helpers
private extension MKCoordinateRegion {
    /// Returns a region that fits all coordinates with 20 % padding.
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

// MARK: ‑ Misc helpers
private extension CLLocationCoordinate2D {
    /// Haversine distance in metres.
    func distance(to other: CLLocationCoordinate2D) -> Double {
        CLLocation(latitude: latitude, longitude: longitude)
            .distance(from: CLLocation(latitude: other.latitude, longitude: other.longitude))
    }
}

/// Simple equality check without conforming `CLLocationCoordinate2D` to `Equatable`.
private func coordinatesEqual(_ lhs: CLLocationCoordinate2D?, _ rhs: CLLocationCoordinate2D?) -> Bool {
    guard let a = lhs, let b = rhs else { return false }
    return a.latitude == b.latitude && a.longitude == b.longitude
}
