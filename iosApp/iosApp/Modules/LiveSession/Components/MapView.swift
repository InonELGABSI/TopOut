import SwiftUI
import MapKit
import Shared

struct MapView: UIViewRepresentable {
    let trackPoints: [TrackPoint]
    @Binding var region: MKCoordinateRegion
    
    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        mapView.userTrackingMode = .follow
        
        return mapView
    }
    
    func updateUIView(_ mapView: MKMapView, context: Context) {
        // Update the map region if needed
        if !trackPoints.isEmpty, let lastPoint = trackPoints.last,
           let latitude = lastPoint.latitude?.doubleValue,
           let longitude = lastPoint.longitude?.doubleValue {
            
            let newCoordinate = CLLocationCoordinate2D(
                latitude: latitude,
                longitude: longitude
            )
            
            region = MKCoordinateRegion(
                center: newCoordinate,
                span: region.span
            )
            mapView.setRegion(region, animated: true)
        }
        
        // Update the route overlay
        updateRouteOverlay(on: mapView)
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    private func updateRouteOverlay(on mapView: MKMapView) {
        // Remove existing overlays
        mapView.removeOverlays(mapView.overlays)
        
        // Create a new polyline from track points if available
        if trackPoints.count > 1 {
            var coordinates: [CLLocationCoordinate2D] = []
            
            for point in trackPoints {
                if let latitude = point.latitude?.doubleValue,
                   let longitude = point.longitude?.doubleValue {
                    coordinates.append(CLLocationCoordinate2D(
                        latitude: latitude,
                        longitude: longitude
                    ))
                }
            }
            
            if !coordinates.isEmpty {
                let polyline = MKPolyline(coordinates: coordinates, count: coordinates.count)
                mapView.addOverlay(polyline)
                
                // Add start marker if we have points
                if let firstCoordinate = coordinates.first {
                    let startAnnotation = MKPointAnnotation()
                    startAnnotation.coordinate = firstCoordinate
                    startAnnotation.title = "Start"
                    mapView.addAnnotation(startAnnotation)
                }
            }
        }
    }
    
    class Coordinator: NSObject, MKMapViewDelegate {
        var parent: MapView
        
        init(_ parent: MapView) {
            self.parent = parent
        }
        
        func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
            if let polyline = overlay as? MKPolyline {
                let renderer = MKPolylineRenderer(polyline: polyline)
                renderer.strokeColor = UIColor(red: 0.871, green: 0.169, blue: 0.169, alpha: 1.0) // BrandColors.climbingRed
                renderer.lineWidth = 4
                return renderer
            }
            return MKOverlayRenderer(overlay: overlay)
        }
        
        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            // Don't customize the user location annotation
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
            
            if let markerAnnotationView = annotationView as? MKMarkerAnnotationView {
                if annotation.title == "Start" {
                    markerAnnotationView.markerTintColor = .green
                    markerAnnotationView.glyphImage = UIImage(systemName: "flag.fill")
                }
            }
            
            return annotationView
        }
    }
}
