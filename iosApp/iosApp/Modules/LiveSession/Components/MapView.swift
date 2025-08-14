import SwiftUI
import MapKit
import Shared
import UIKit

/// SwiftUI wrapper around MKMapView optimized for live tracks.
struct MapView: UIViewRepresentable {
    let trackPoints: [TrackPoint]
    @Binding var region: MKCoordinateRegion

    /// Show an "End" annotation at the tail of the track.
    var showEndAnnotation: Bool = true

    /// Auto-center camera on the last track point when not interacting.
    var followLastPoint: Bool = true

    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

    // MARK: - UIViewRepresentable

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        context.coordinator.mapView = mapView

        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        mapView.userTrackingMode  = .follow  // start in follow; turn off on user pan/zoom.

        // Optional: keep zoom reasonable (prevents "lost in space")
        // Adjust max/min to your product needs.
        if #available(iOS 13.0, *) {
            mapView.setCameraZoomRange(MKMapView.CameraZoomRange(maxCenterCoordinateDistance: 50_000), animated: false)
        }

        // Built-in Apple controls (compass, tracking, scale)
        mapView.showsCompass = false // We'll place our own compass button.

        let compass = MKCompassButton(mapView: mapView)
        compass.translatesAutoresizingMaskIntoConstraints = false
        compass.compassVisibility = .adaptive
        mapView.addSubview(compass)

        let tracking = MKUserTrackingButton(mapView: mapView)
        tracking.translatesAutoresizingMaskIntoConstraints = false
        tracking.layer.cornerRadius = 10
        tracking.clipsToBounds = true
        mapView.addSubview(tracking)

        let scale = MKScaleView(mapView: mapView)
        scale.translatesAutoresizingMaskIntoConstraints = false
        scale.legendAlignment = .trailing
        mapView.addSubview(scale)

        // Layout with safe area
        NSLayoutConstraint.activate([
            // Compass top-right
            compass.topAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.topAnchor, constant: 12),
            compass.trailingAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.trailingAnchor, constant: -12),

            // Tracking below compass
            tracking.topAnchor.constraint(equalTo: compass.bottomAnchor, constant: 12),
            tracking.trailingAnchor.constraint(equalTo: compass.trailingAnchor),

            // Scale bottom-left
            scale.leadingAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.leadingAnchor, constant: 12),
            scale.bottomAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.bottomAnchor, constant: -36),
        ])

        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        // Build coordinates once
        var coords: [CLLocationCoordinate2D] = []
        coords.reserveCapacity(trackPoints.count)
        for tp in trackPoints {
            if let lat = tp.latitude?.double, let lon = tp.longitude?.double {
                coords.append(CLLocationCoordinate2D(latitude: lat, longitude: lon))
            }
        }

        // Keep a single polyline; replace only when point count or last point changes
        if coords.count > 1 {
            let lastChanged =
                context.coordinator.polyline == nil ||
                context.coordinator.polyline!.pointCount != coords.count ||
                context.coordinator.lastCoordinate?.latitude  != coords.last!.latitude ||
                context.coordinator.lastCoordinate?.longitude != coords.last!.longitude

            if lastChanged {
                // Remove old polyline, add new (use geodesic if your tracks span long distances)
                if let old = context.coordinator.polyline {
                    mapView.removeOverlay(old)
                }
                let newPolyline = MKPolyline(coordinates: coords, count: coords.count)
                context.coordinator.polyline = newPolyline
                mapView.addOverlay(newPolyline)

                // Start annotation (create once)
                if context.coordinator.startAnnotation == nil, let first = coords.first {
                    let ann = MKPointAnnotation()
                    ann.coordinate = first
                    ann.title = "Start"
                    ann.accessibilityLabel = "Track start"
                    context.coordinator.startAnnotation = ann
                    mapView.addAnnotation(ann)
                }

                // End annotation (update or add/remove based on flag)
                if showEndAnnotation, let last = coords.last {
                    if let end = context.coordinator.endAnnotation {
                        end.coordinate = last
                    } else {
                        let end = MKPointAnnotation()
                        end.coordinate = last
                        end.title = "End"
                        end.accessibilityLabel = "Track end"
                        context.coordinator.endAnnotation = end
                        mapView.addAnnotation(end)
                    }
                } else if !showEndAnnotation, let end = context.coordinator.endAnnotation {
                    mapView.removeAnnotation(end)
                    context.coordinator.endAnnotation = nil
                }

                context.coordinator.lastCoordinate = coords.last
            }
        } else {
            // Track empty/short: clean up overlays (but leave user location)
            if let pl = context.coordinator.polyline {
                mapView.removeOverlay(pl)
                context.coordinator.polyline = nil
            }
            if let s = context.coordinator.startAnnotation {
                mapView.removeAnnotation(s)
                context.coordinator.startAnnotation = nil
            }
            if let e = context.coordinator.endAnnotation {
                mapView.removeAnnotation(e)
                context.coordinator.endAnnotation = nil
            }
            return
        }

        // Auto-center if following, not within cooldown, and we actually moved
        if followLastPoint,
           let last = context.coordinator.lastCoordinate,
           Date() >= context.coordinator.suppressAutoCenterUntil
        {
            let center = region.center
            let dist = CLLocation(latitude: last.latitude, longitude: last.longitude)
                .distance(from: CLLocation(latitude: center.latitude, longitude: center.longitude))
            if dist > 5 { // threshold to avoid jitter
                // Preserve current zoom (span)
                let newRegion = MKCoordinateRegion(center: last, span: mapView.region.span)
                region = newRegion
                context.coordinator.isProgrammaticChange = true
                mapView.setRegion(newRegion, animated: true)
                DispatchQueue.main.async { context.coordinator.isProgrammaticChange = false }
            }
        }
    }

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    // MARK: - Coordinator

    class Coordinator: NSObject, MKMapViewDelegate {
        var parent: MapView
        weak var mapView: MKMapView?

        // Stateful overlays/annotations
        var polyline: MKPolyline?
        var startAnnotation: MKPointAnnotation?
        var endAnnotation: MKPointAnnotation?
        var lastCoordinate: CLLocationCoordinate2D?

        // Follow control
        var suppressAutoCenterUntil: Date = .distantPast
        var isProgrammaticChange = false

        init(_ parent: MapView) {
            self.parent = parent
            super.init()
            // Observe app lifecycle to restore map interactivity after backgrounding.
            NotificationCenter.default.addObserver(self,
                                                   selector: #selector(appDidBecomeActive),
                                                   name: UIApplication.didBecomeActiveNotification,
                                                   object: nil)
        }
        deinit { NotificationCenter.default.removeObserver(self) }

        // Pause follow if the user pans/zooms (delegate-driven, not by poking recognizers)
        func mapView(_ mapView: MKMapView, regionWillChangeAnimated animated: Bool) {
            if !isProgrammaticChange {
                suppressAutoCenterUntil = Date().addingTimeInterval(6) // grace window
                mapView.setUserTrackingMode(.none, animated: true)
            }
        }

        // Keep SwiftUI binding in sync with the map's visible region
        func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
            // Avoid modifying SwiftUI state synchronously during view updates to silence runtime warning.
            let newRegion = mapView.region
            // Skip if effectively unchanged to avoid feedback loop.
            if regionsAreEqual(newRegion, parent.region) { return }
            DispatchQueue.main.async { [weak self] in
                guard let self else { return }
                // Double-check in case things changed again before dispatch executed.
                if !self.regionsAreEqual(newRegion, self.parent.region) {
                    self.parent.region = newRegion
                }
            }
        }

        // Polyline renderer
        func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
            guard let pl = overlay as? MKPolyline else {
                return MKOverlayRenderer(overlay: overlay)
            }
            let r = MKPolylineRenderer(polyline: pl)
            r.strokeColor = UIColor(parent.theme.primary)
            r.lineWidth = 4
            r.lineJoin = .round
            r.lineCap  = .round
            return r
        }

        // Annotations (Start/End markers)
        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            if annotation is MKUserLocation { return nil }
            let id = "TrackPin"
            let view = (mapView.dequeueReusableAnnotationView(withIdentifier: id) as? MKMarkerAnnotationView)
                ?? MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: id)
            view.canShowCallout = true

            switch annotation.title ?? "" {
            case "Start":
                view.markerTintColor = .systemGreen
                view.glyphImage = UIImage(systemName: "flag.fill")
            case "End":
                view.markerTintColor = .systemRed
                view.glyphImage = UIImage(systemName: "flag.checkered")
            default:
                view.markerTintColor = .systemBlue
                view.glyphImage = UIImage(systemName: "circle.fill")
            }

            view.annotation = annotation
            return view
        }

        // Helper: approximate region equality with tolerance to prevent jitter loops
        private func regionsAreEqual(_ a: MKCoordinateRegion, _ b: MKCoordinateRegion) -> Bool {
            let latEps  = 0.00001
            let lonEps  = 0.00001
            let spanLatEps = 0.0005
            let spanLonEps = 0.0005
            return abs(a.center.latitude  - b.center.latitude)  < latEps &&
                   abs(a.center.longitude - b.center.longitude) < lonEps &&
                   abs(a.span.latitudeDelta  - b.span.latitudeDelta)  < spanLatEps &&
                   abs(a.span.longitudeDelta - b.span.longitudeDelta) < spanLonEps
        }

        @objc private func appDidBecomeActive() {
            guard let mapView else { return }
            // Re-enable gesture recognizers (some iOS versions exhibit a dormant state after resume).
            mapView.isUserInteractionEnabled = true
            mapView.isScrollEnabled = true
            mapView.isZoomEnabled = true
            mapView.isRotateEnabled = true
            mapView.isPitchEnabled = true
            mapView.gestureRecognizers?.forEach { $0.isEnabled = true }
            // Ensure delegate still set (defensive) & overlays present.
            if mapView.delegate == nil { mapView.delegate = self }
            // Re-apply region silently if map got desynced.
            let desired = parent.region
            if !regionsAreEqual(mapView.region, desired) {
                isProgrammaticChange = true
                mapView.setRegion(desired, animated: false)
                DispatchQueue.main.async { [weak self] in self?.isProgrammaticChange = false }
            }
        }
    }
}
