import SwiftUI
import MapKit
import Shared
import UIKit

/// SwiftUI wrapper around MKMapView optimized for live tracks.
struct MapView: UIViewRepresentable {
    let trackPoints: [TrackPoint]
    @Binding var region: MKCoordinateRegion

    /// Show an "End" annotation at the tail of the track.
    var showEndAnnotation: Bool = false

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
        mapView.userTrackingMode  = .none // custom soft-follow

        // Keep zoom reasonable
        if #available(iOS 13.0, *) {
            if let zoomRange = MKMapView.CameraZoomRange(minCenterCoordinateDistance: 50,
                                                         maxCenterCoordinateDistance: 50_000) {
                mapView.setCameraZoomRange(zoomRange, animated: false)
                context.coordinator.minZoomDistance = zoomRange.minCenterCoordinateDistance
                context.coordinator.maxZoomDistance = zoomRange.maxCenterCoordinateDistance
            } else {
                context.coordinator.minZoomDistance = 50
                context.coordinator.maxZoomDistance = 50_000
            }
        }

        // Built-in Apple controls (compass, tracking, scale)
        mapView.showsCompass = false // We'll place our own compass button.

        let compass = MKCompassButton(mapView: mapView)
        compass.translatesAutoresizingMaskIntoConstraints = false
        compass.compassVisibility = .adaptive
        mapView.addSubview(compass)

        // Custom Focus (soft follow) button
        let focusButton = UIButton(type: .system)
        focusButton.translatesAutoresizingMaskIntoConstraints = false
        focusButton.layer.cornerRadius = 10
        focusButton.clipsToBounds = true
        focusButton.accessibilityLabel = "Focus on current location"
        focusButton.addTarget(context.coordinator, action: #selector(Coordinator.didTapFocus), for: .touchUpInside)
        if #available(iOS 15.0, *) {
            var config = UIButton.Configuration.plain()
            config.image = UIImage(systemName: "location")
            config.baseBackgroundColor = .secondarySystemBackground
            config.contentInsets = NSDirectionalEdgeInsets(top: 8, leading: 8, bottom: 8, trailing: 8)
            focusButton.configuration = config
            focusButton.layer.cornerRadius = 10
        } else {
            focusButton.backgroundColor = .secondarySystemBackground
            focusButton.setImage(UIImage(systemName: "location"), for: .normal)
            focusButton.tintColor = .label
            focusButton.contentEdgeInsets = UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8)
        }
        mapView.addSubview(focusButton)
        context.coordinator.focusButton = focusButton

        // Zoom buttons (+ / -) stacked vertically above the focus button
        let zoomInButton = UIButton(type: .system)
        zoomInButton.translatesAutoresizingMaskIntoConstraints = false
        zoomInButton.accessibilityLabel = "Zoom in"
        if #available(iOS 15.0, *) {
            var config = UIButton.Configuration.plain()
            config.image = UIImage(systemName: "plus")
            config.baseBackgroundColor = .secondarySystemBackground
            config.contentInsets = NSDirectionalEdgeInsets(top: 8, leading: 8, bottom: 8, trailing: 8)
            zoomInButton.configuration = config
        } else {
            zoomInButton.backgroundColor = .secondarySystemBackground
            zoomInButton.setImage(UIImage(systemName: "plus"), for: .normal)
            zoomInButton.tintColor = .label
            zoomInButton.contentEdgeInsets = UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8)
        }
        zoomInButton.layer.cornerRadius = 10
        zoomInButton.addTarget(context.coordinator, action: #selector(Coordinator.didTapZoomIn), for: .touchUpInside)

        let zoomOutButton = UIButton(type: .system)
        zoomOutButton.translatesAutoresizingMaskIntoConstraints = false
        zoomOutButton.accessibilityLabel = "Zoom out"
        if #available(iOS 15.0, *) {
            var config = UIButton.Configuration.plain()
            config.image = UIImage(systemName: "minus")
            config.baseBackgroundColor = .secondarySystemBackground
            config.contentInsets = NSDirectionalEdgeInsets(top: 8, leading: 8, bottom: 8, trailing: 8)
            zoomOutButton.configuration = config
        } else {
            zoomOutButton.backgroundColor = .secondarySystemBackground
            zoomOutButton.setImage(UIImage(systemName: "minus"), for: .normal)
            zoomOutButton.tintColor = .label
            zoomOutButton.contentEdgeInsets = UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8)
        }
        zoomOutButton.layer.cornerRadius = 10
        zoomOutButton.addTarget(context.coordinator, action: #selector(Coordinator.didTapZoomOut), for: .touchUpInside)

        mapView.addSubview(zoomInButton)
        mapView.addSubview(zoomOutButton)
        context.coordinator.zoomInButton = zoomInButton
        context.coordinator.zoomOutButton = zoomOutButton

        let scale = MKScaleView(mapView: mapView)
        scale.translatesAutoresizingMaskIntoConstraints = false
        scale.legendAlignment = .trailing
        mapView.addSubview(scale)

        // Layout with safe area
        NSLayoutConstraint.activate([
                                        // Compass top-right
                                        compass.topAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.topAnchor, constant: 12),
                                        compass.trailingAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.trailingAnchor, constant: -12),

                                        // Focus button bottom-right
                                        focusButton.trailingAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.trailingAnchor, constant: -12),
                                        focusButton.bottomAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.bottomAnchor, constant: -36),

                                        // Zoom out just above focus
                                        zoomOutButton.trailingAnchor.constraint(equalTo: focusButton.trailingAnchor),
                                        zoomOutButton.bottomAnchor.constraint(equalTo: focusButton.topAnchor, constant: -12),

                                        // Zoom in above zoom out
                                        zoomInButton.trailingAnchor.constraint(equalTo: focusButton.trailingAnchor),
                                        zoomInButton.bottomAnchor.constraint(equalTo: zoomOutButton.topAnchor, constant: -12),

                                        // Equal sizes for buttons
                                        zoomInButton.widthAnchor.constraint(equalTo: focusButton.widthAnchor),
                                        zoomOutButton.widthAnchor.constraint(equalTo: focusButton.widthAnchor),

                                        // Scale bottom-left
                                        scale.leadingAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.leadingAnchor, constant: 12),
                                        scale.bottomAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.bottomAnchor, constant: -36),
                                    ])

        // 🔹 Try to center immediately if the system already has a cached user fix
        DispatchQueue.main.async {
            context.coordinator.tryInitialCenterIfPossible(animated: false)
        }

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
                // Remove old polyline, add new
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

        // Passive follow to last track point (only when NOT in user-focus mode)
        if followLastPoint,
           !context.coordinator.isUserFocusEnabled,
           let last = context.coordinator.lastCoordinate,
           Date() >= context.coordinator.suppressAutoCenterUntil
        {
            let center = region.center
            let dist = CLLocation(latitude: last.latitude, longitude: last.longitude)
                .distance(from: CLLocation(latitude: center.latitude, longitude: center.longitude))
            if dist > 5 { // threshold to avoid jitter
                // Update camera center only, keep distance/heading/pitch so zoom persists.
                let cam = mapView.camera.copy() as! MKMapCamera
                cam.centerCoordinate = last
                context.coordinator.isProgrammaticChange = true
                mapView.setCamera(cam, animated: true)
                // Keep SwiftUI region binding center updated while leaving span untouched.
                var newRegion = region
                newRegion.center = last
                region = newRegion
                DispatchQueue.main.async { context.coordinator.isProgrammaticChange = false }
            }
        }

        // No forcing .follow tracking mode
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

        // Focus button
        weak var focusButton: UIButton?
        var isUserFocusEnabled: Bool = false { didSet { updateFocusUI() } }

        // Zoom buttons
        weak var zoomInButton: UIButton?
        weak var zoomOutButton: UIButton?
        var minZoomDistance: CLLocationDistance = 50
        var maxZoomDistance: CLLocationDistance = 50_000

        // “Locked” distance while focus is ON (updated by user's zoom actions)
        var lastUserZoomDistance: CLLocationDistance?

        // Apply the initial near-max zoom only once per focus activation
        private var appliedInitialZoomThisFocus = false

        // 🔹 One-time initial centering on first user fix
        private var hasCenteredToUserOnFirstFix = false
        private let initialUserCenterDistance: CLLocationDistance = 1_000 // 1km default

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

        // Capture user zoom changes; pause passive track follow when not focused
        func mapView(_ mapView: MKMapView, regionWillChangeAnimated animated: Bool) {
            if !isProgrammaticChange {
                // Always remember current distance: keeps user's zoom when focus is ON
                lastUserZoomDistance = mapView.camera.centerCoordinateDistance
            }
            if !isProgrammaticChange && !isUserFocusEnabled {
                suppressAutoCenterUntil = Date().addingTimeInterval(6) // grace window
            }
        }

        // Keep SwiftUI binding in sync; maintain constant follow when focus is ON
        func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
            let newRegion = mapView.region
            if !regionsAreEqual(newRegion, parent.region) {
                DispatchQueue.main.async { [weak self] in
                    guard let self else { return }
                    if !self.regionsAreEqual(newRegion, self.parent.region) {
                        self.parent.region = newRegion
                    }
                }
            }

            // If user panned/zoomed while focus is ON, snap center back to the user
            // but keep the user's chosen zoom (lastUserZoomDistance).
            if !isProgrammaticChange && isUserFocusEnabled {
                recenterOnUser(distance: lastUserZoomDistance, animated: true)
            }
        }

        // Constant follow + initial centering
        func mapView(_ mapView: MKMapView, didUpdate userLocation: MKUserLocation) {
            guard CLLocationCoordinate2DIsValid(userLocation.coordinate) else { return }

            // 🔹 First live fix: center once to the user (even if focus is OFF)
            if !hasCenteredToUserOnFirstFix {
                tryInitialCenterIfPossible(animated: false)
                // Avoid immediate passive track re-centering fighting this
                suppressAutoCenterUntil = Date().addingTimeInterval(2)
            }

            // If focus is ON, keep following (preserving zoom)
            if isUserFocusEnabled {
                recenterOnUser(distance: lastUserZoomDistance, animated: true)
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

        // MARK: - Focus toggle (constant follow + single near-max zoom)
        @objc func didTapFocus() {
            isUserFocusEnabled.toggle()
            guard let mapView else { return }

            if isUserFocusEnabled {
                // Apply near-max zoom ONCE when focus is enabled.
                if !appliedInitialZoomThisFocus {
                    let nearMax = minZoomDistance * 3.0   // e.g. 3× min distance
                    lastUserZoomDistance = max(minZoomDistance,
                                               min(maxZoomDistance, nearMax))
                    appliedInitialZoomThisFocus = true
                } else if lastUserZoomDistance == nil {
                    // If for some reason we don't have a distance, capture current.
                    lastUserZoomDistance = mapView.camera.centerCoordinateDistance
                }
                recenterOnUser(distance: lastUserZoomDistance, animated: true)
            } else {
                // Turning focus OFF resets the one-time-zoom flag for next activation.
                appliedInitialZoomThisFocus = false
            }
        }

        /// One-time initial center on current user if available.
        func tryInitialCenterIfPossible(animated: Bool) {
            guard let mapView else { return }
            guard !hasCenteredToUserOnFirstFix,
                  let coord = mapView.userLocation.location?.coordinate,
                  CLLocationCoordinate2DIsValid(coord) else { return }

            let cam = mapView.camera.copy() as! MKMapCamera
            cam.centerCoordinate = coord
            // Use a reasonable default zoom for first load.
            let dist = max(minZoomDistance, min(maxZoomDistance, initialUserCenterDistance))
            cam.centerCoordinateDistance = dist
            cam.pitch = 0
            cam.heading = 0

            isProgrammaticChange = true
            mapView.setCamera(cam, animated: animated)
            hasCenteredToUserOnFirstFix = true

            // Sync SwiftUI binding (async to avoid "modifying state during view update")
            DispatchQueue.main.async { [weak self] in
                guard let self, let mv = self.mapView else { return }
                self.parent.region = mv.region
                self.isProgrammaticChange = false
            }
        }

        private func recenterOnUser(distance: CLLocationDistance? = nil, animated: Bool) {
            guard let mapView,
                  let coord = mapView.userLocation.location?.coordinate,
                  CLLocationCoordinate2DIsValid(coord) else { return }
            let cam = mapView.camera.copy() as! MKMapCamera
            cam.centerCoordinate = coord
            if let dist = distance ?? lastUserZoomDistance {
                cam.centerCoordinateDistance = max(minZoomDistance, min(maxZoomDistance, dist))
            }
            // Normalize pitch & heading so the user dot stays clear (optional)
            cam.pitch = 0
            cam.heading = 0
            isProgrammaticChange = true
            mapView.setCamera(cam, animated: animated)
            DispatchQueue.main.async { [weak self] in self?.isProgrammaticChange = false }
        }

        // MARK: - Zoom Handling
        @objc func didTapZoomIn() { adjustZoom(factor: 0.5) }
        @objc func didTapZoomOut() { adjustZoom(factor: 2.0) }

        private func adjustZoom(factor: Double) {
            guard let mapView else { return }
            let currentDistance = mapView.camera.centerCoordinateDistance
            var target = currentDistance * factor
            target = max(minZoomDistance, min(maxZoomDistance, target))
            guard abs(target - currentDistance) > 0.5 else { return }

            let camera = mapView.camera.copy() as! MKMapCamera
            camera.centerCoordinateDistance = target
            isProgrammaticChange = true
            mapView.setCamera(camera, animated: true)
            DispatchQueue.main.async { [weak self] in self?.isProgrammaticChange = false }

            // Remember this as the “locked” zoom while focus is ON.
            lastUserZoomDistance = target

            // Keep SwiftUI region binding roughly in sync after animation
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.35) { [weak self] in
                guard let self, let mv = self.mapView else { return }
                self.parent.region = mv.region
            }
        }

        private func updateFocusUI() {
            guard let button = focusButton else { return }
            let imageName = isUserFocusEnabled ? "location.fill" : "location"
            if #available(iOS 15.0, *) {
                if var config = button.configuration {
                    config.image = UIImage(systemName: imageName)
                    config.baseForegroundColor = isUserFocusEnabled ? .systemBlue : .label
                    button.configuration = config
                }
            } else {
                button.setImage(UIImage(systemName: imageName), for: .normal)
                button.tintColor = isUserFocusEnabled ? .systemBlue : .label
            }
            button.accessibilityValue = isUserFocusEnabled ? "On" : "Off"
        }

        @objc private func appDidBecomeActive() {
            guard let mapView else { return }
            // Re-enable gesture recognizers (defensive)
            mapView.isUserInteractionEnabled = true
            mapView.isScrollEnabled = true
            mapView.isZoomEnabled = true
            mapView.isRotateEnabled = true
            mapView.isPitchEnabled = true
            mapView.gestureRecognizers?.forEach { $0.isEnabled = true }
            if mapView.delegate == nil { mapView.delegate = self }

            // Re-apply region silently if desynced.
            let desired = parent.region
            if !regionsAreEqual(mapView.region, desired) {
                isProgrammaticChange = true
                mapView.setRegion(desired, animated: false)
                DispatchQueue.main.async { [weak self] in self?.isProgrammaticChange = false }
            }
            updateFocusUI()

            // If we resumed and never centered (e.g., permission granted while backgrounded), try now
            tryInitialCenterIfPossible(animated: false)
        }
    }
}
