import SwiftUI
import MapKit
import Shared

struct MapView: UIViewRepresentable {
    let trackPoints: [TrackPoint]
    @Binding var region: MKCoordinateRegion

    // Add this if you want to show the End annotation
    let showEndAnnotation: Bool = true
    // Optional: Only auto-center if this is true
    var followLastPoint: Bool = true

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        mapView.userTrackingMode = .follow
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

            // Only animate/center if last point has changed significantly (and follow is on)
            if followLastPoint, let last = coordinates.last {
                let distance = CLLocation(latitude: last.latitude, longitude: last.longitude)
                    .distance(from: CLLocation(latitude: region.center.latitude, longitude: region.center.longitude))
                if distance > 5 {
                    region = MKCoordinateRegion(center: last, span: region.span)
                    mapView.setRegion(region, animated: true)
                }
            }
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, MKMapViewDelegate {
        var parent: MapView

        init(_ parent: MapView) {
            self.parent = parent
        }

        func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
            if let polyline = overlay as? MKPolyline {
                let renderer = MKPolylineRenderer(polyline: polyline)
                renderer.strokeColor = UIColor(red: 0.871, green: 0.169, blue: 0.169, alpha: 1.0) // climbingRed
                renderer.lineWidth = 4
                return renderer
            }
            return MKOverlayRenderer(overlay: overlay)
        }

        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            if annotation is MKUserLocation {
                return nil
            }
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
