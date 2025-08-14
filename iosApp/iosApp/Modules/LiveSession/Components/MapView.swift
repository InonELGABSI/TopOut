import SwiftUI
import MapKit
import Shared

struct MapView: UIViewRepresentable {
    let trackPoints: [TrackPoint]
    @Binding var region: MKCoordinateRegion

    // Add this if you want to show the End annotation
    let showEndAnnotation: Bool = false
    // Optional: Only auto-center if this is true
    var followLastPoint: Bool = true

    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        context.coordinator.mapView = mapView // keep a weak ref for actions

        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        mapView.userTrackingMode  = .follow   // starts in follow; we’ll disable on gesture.  // Apple docs: userTrackingMode .follow recenters the map.

        // --- Built-in Apple controls (best practice) ---
        mapView.showsCompass = false // we’ll place our own MKCompassButton.

        // Compass (adaptive visibility = appears when not north-up)
        let compass = MKCompassButton(mapView: mapView)
        compass.translatesAutoresizingMaskIntoConstraints = false
        compass.compassVisibility = .adaptive
        mapView.addSubview(compass)

        // Focus / user tracking
        let tracking = MKUserTrackingButton(mapView: mapView)
        tracking.translatesAutoresizingMaskIntoConstraints = false
        tracking.layer.cornerRadius = 10
        tracking.clipsToBounds = true

        // Scale (legend)
        let scale = MKScaleView(mapView: mapView)
        scale.translatesAutoresizingMaskIntoConstraints = false
        scale.legendAlignment = .trailing

        mapView.addSubview(tracking)
        mapView.addSubview(scale)

        // --- Custom zoom (+/–) for iPhone (iOS has no built-in zoom stepper) ---
        let blur = UIVisualEffectView(effect: UIBlurEffect(style: .systemMaterial))
        blur.translatesAutoresizingMaskIntoConstraints = false
        blur.layer.cornerRadius = 12
        blur.clipsToBounds = true

        let plus = UIButton(type: .system)
        plus.setImage(UIImage(systemName: "plus"), for: .normal)
        plus.widthAnchor.constraint(equalToConstant: 44).isActive = true
        plus.heightAnchor.constraint(equalToConstant: 44).isActive = true
        plus.addAction(UIAction { _ in context.coordinator.adjustZoom(factor: 0.5) }, for: .touchUpInside) // zoom in

        let minus = UIButton(type: .system)
        minus.setImage(UIImage(systemName: "minus"), for: .normal)
        minus.widthAnchor.constraint(equalToConstant: 44).isActive = true
        minus.heightAnchor.constraint(equalToConstant: 44).isActive = true
        minus.addAction(UIAction { _ in context.coordinator.adjustZoom(factor: 2.0) }, for: .touchUpInside) // zoom out

        // Optional: focus last trackpoint (route tail)
        let focusTail = UIButton(type: .system)
        focusTail.setImage(UIImage(systemName: "scope"), for: .normal)
        focusTail.widthAnchor.constraint(equalToConstant: 44).isActive = true
        focusTail.heightAnchor.constraint(equalToConstant: 44).isActive = true
        focusTail.addAction(UIAction { _ in
            context.coordinator.clearCooldown()
            context.coordinator.centerOnLastTrackPoint()
        }, for: .touchUpInside)

        let vstack = UIStackView(arrangedSubviews: [plus, minus, focusTail])
        vstack.axis = .vertical
        vstack.spacing = 0
        vstack.translatesAutoresizingMaskIntoConstraints = false

        blur.contentView.addSubview(vstack)
        mapView.addSubview(blur)

        // Layout (respect safe area; pad away from legal link)
        NSLayoutConstraint.activate([
            // Compass top-right
            compass.topAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.topAnchor, constant: 12),
            compass.trailingAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.trailingAnchor, constant: -12),

            // Tracking under compass
            tracking.topAnchor.constraint(equalTo: compass.bottomAnchor, constant: 12),
            tracking.trailingAnchor.constraint(equalTo: compass.trailingAnchor),

            // Scale bottom-left (lifted above legal links)
            scale.leadingAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.leadingAnchor, constant: 12),
            scale.bottomAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.bottomAnchor, constant: -36),

            // Zoom stack bottom-right
            blur.trailingAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.trailingAnchor, constant: -12),
            blur.bottomAnchor.constraint(equalTo: mapView.safeAreaLayoutGuide.bottomAnchor, constant: -20),

            vstack.topAnchor.constraint(equalTo: blur.contentView.topAnchor),
            vstack.leadingAnchor.constraint(equalTo: blur.contentView.leadingAnchor),
            vstack.trailingAnchor.constraint(equalTo: blur.contentView.trailingAnchor),
            vstack.bottomAnchor.constraint(equalTo: blur.contentView.bottomAnchor)
        ])

        // --- Pause auto-follow while user interacts (prevents zoom “snap back”) ---
        // Add targets to built-in gesture recognizers to detect user pans/zooms.
        (mapView.gestureRecognizers ?? []).forEach {
            $0.addTarget(context.coordinator, action: #selector(Coordinator.handleMapGesture(_:)))
        }

        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        // Remove overlays and annotations except user location
        mapView.removeOverlays(mapView.overlays)
        mapView.removeAnnotations(mapView.annotations.filter { !($0 is MKUserLocation) })

        // Create polyline and annotations if there are enough points
        guard trackPoints.count > 1 else { return }
        var coordinates: [CLLocationCoordinate2D] = []
        for point in trackPoints {
            if let latitude = point.latitude?.double,
               let longitude = point.longitude?.double {
                coordinates.append(CLLocationCoordinate2D(latitude: latitude, longitude: longitude))
            }
        }

        if !coordinates.isEmpty {
            let polyline = MKPolyline(coordinates: coordinates, count: coordinates.count)
            mapView.addOverlay(polyline)

            // Start marker
            if let first = coordinates.first {
                let startAnnotation = MKPointAnnotation()
                startAnnotation.coordinate = first
                startAnnotation.title = "Start"
                mapView.addAnnotation(startAnnotation)
            }

            // End marker (optional)
            if showEndAnnotation, let last = coordinates.last {
                let endAnnotation = MKPointAnnotation()
                endAnnotation.coordinate = last
                endAnnotation.title = "End"
                mapView.addAnnotation(endAnnotation)
            }

            // Always keep last coordinate for the "focus tail" button and location updates
            // This ensures location updates happen regardless of whether the End marker is shown
            if let last = coordinates.last {
                context.coordinator.lastCoordinate = last

                // Only recenter if follow is enabled and the cooldown has passed
                if followLastPoint && Date() >= context.coordinator.suppressAutoCenterUntil {
                    // distance threshold so we don't spam small moves
                    let dist = CLLocation(latitude: last.latitude, longitude: last.longitude)
                        .distance(from: CLLocation(latitude: region.center.latitude, longitude: region.center.longitude))

                    if dist > 5 {
                        // Use the map's *current* span, so user zoom is preserved
                        let newRegion = MKCoordinateRegion(center: last, span: mapView.region.span)
                        region = newRegion
                        mapView.setRegion(newRegion, animated: true) // only center changes; span stays
                    }
                }
            }
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, MKMapViewDelegate {
        var parent: MapView
        weak var mapView: MKMapView?
        var lastCoordinate: CLLocationCoordinate2D?

        // Cooldown window to suppress auto-centering after gestures
        var suppressAutoCenterUntil: Date = .distantPast

        init(_ parent: MapView) {
            self.parent = parent
        }

        // Keep the SwiftUI binding in sync with any user pan/zoom
        func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
            parent.region = mapView.region
        }

        // Zoom helpers (iPhone has no native zoom stepper)
        func adjustZoom(factor: Double) {
            guard let mapView else { return }
            var r = mapView.region
            r.span.latitudeDelta  = max(1e-6, r.span.latitudeDelta  * factor)
            r.span.longitudeDelta = max(1e-6, r.span.longitudeDelta * factor)
            mapView.setRegion(r, animated: true)
        }

        func centerOnLastTrackPoint() {
            guard let mapView, let last = lastCoordinate else { return }
            mapView.setUserTrackingMode(.follow, animated: true)
            let newRegion = MKCoordinateRegion(center: last, span: mapView.region.span)
            mapView.setRegion(newRegion, animated: true)
        }

        func clearCooldown() {
            suppressAutoCenterUntil = .distantPast
        }

        // Detect any pan/zoom and pause following for a short time
        @objc func handleMapGesture(_ gr: UIGestureRecognizer) {
            switch gr.state {
            case .began:
                suppressAutoCenterUntil = Date().addingTimeInterval(6) // 6s grace
                mapView?.setUserTrackingMode(.none, animated: true)    // stop Apple’s follow mode
            case .ended, .cancelled, .failed:
                suppressAutoCenterUntil = Date().addingTimeInterval(6)
            default: break
            }
        }

        // Renderers / annotations unchanged
        func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
            if let polyline = overlay as? MKPolyline {
                let renderer = MKPolylineRenderer(polyline: polyline)
                renderer.strokeColor = UIColor(parent.theme.primary)
                renderer.lineWidth = 4
                return renderer
            }
            return MKOverlayRenderer(overlay: overlay)
        }

        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            if annotation is MKUserLocation { return nil }
            let identifier = "CustomPin"
            var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: identifier)
            if annotationView == nil {
                annotationView = MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: identifier)
                annotationView?.canShowCallout = true
            } else {
                annotationView?.annotation = annotation
            }
            if let markerView = annotationView as? MKMarkerAnnotationView {
                switch annotation.title ?? "" {
                case "Start":
                    markerView.markerTintColor = .systemGreen
                    markerView.glyphImage = UIImage(systemName: "flag.fill")
                case "End":
                    markerView.markerTintColor = .systemRed
                    markerView.glyphImage = UIImage(systemName: "flag.checkered")
                default:
                    markerView.markerTintColor = .systemBlue
                    markerView.glyphImage = UIImage(systemName: "circle.fill")
                }
            }
            return annotationView
        }
    }
}
