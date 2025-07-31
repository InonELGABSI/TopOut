
//==============================================================
//  ActiveSessionContent.swift
//  (unchanged – your implementation)
//==============================================================

import SwiftUI
import MapKit
import Shared

struct ActiveSessionContent: View {
    let trackPoint: TrackPoint
    let trackPoints: [TrackPoint]
    @Binding var mapRegion: MKCoordinateRegion
    let onStopClicked: () -> Void
    let onCancelClicked: () -> Void
    let colors: TopOutColorScheme
    
    var body: some View {
        ZStack {
            MapView(trackPoints: trackPoints, region: $mapRegion)
                .ignoresSafeArea(edges: .top)
                .frame(height: UIScreen.main.bounds.height * 0.6)
            
            VStack {
                Spacer(minLength: UIScreen.main.bounds.height * 0.55)
                LiveDataCard(trackPoint: trackPoint, colors: colors)
                BottomControls(onStopClicked: onStopClicked, onCancelClicked: onCancelClicked)
                    .padding(.bottom, 32)
            }
        }
    }
}

// MARK: – Sub-views used inside ActiveSessionContent

private struct LiveDataCard: View {
    let trackPoint: TrackPoint
    let colors: TopOutColorScheme
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HeaderRow(timestamp: trackPoint.timestamp, colors: colors)
            LocationRow(trackPoint: trackPoint, colors: colors)
            SpeedAltitudeRow(trackPoint: trackPoint, colors: colors)
        }
        .padding(20)
        .background(colors.surface)
        .cornerRadius(24, corners: [.topLeft, .topRight])
    }
}

private struct HeaderRow: View {
    let timestamp: Int64
    let colors: TopOutColorScheme
    
    var body: some View {
        HStack {
            HStack(spacing: 8) {
                Circle().fill(Color.red).frame(width: 8, height: 8)
                Text("Live Data")
                    .font(.title2).bold()
                    .foregroundColor(colors.onSurface)
            }
            Spacer()
            Text(formatTime(timestamp))
                .font(.subheadline).fontWeight(.medium)
                .foregroundColor(colors.onSurfaceVariant)
        }
    }
    
    private func formatTime(_ ts: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(ts) / 1000)
        let f = DateFormatter(); f.dateFormat = "HH:mm:ss"
        return f.string(from: date)
    }
}

private struct LocationRow: View {
    let trackPoint: TrackPoint
    let colors: TopOutColorScheme
    
    var body: some View {
        HStack {
            Label("Location", systemImage: "location.fill")
                .font(.headline)
                .foregroundColor(colors.primary)
            Spacer()
            DataTriplet(
                first:   formatted(trackPoint.latitude?.double, suffix: "°"),
                second:  formatted(trackPoint.longitude?.double, suffix: "°"),
                third:   formatted(trackPoint.altitude?.double, suffix: " m"),
                firstLab: "Lat",
                secondLab:"Lon",
                thirdLab: "Alt",
                colors: colors
            )
        }
    }
    
    private func formatted(_ v: Double?, suffix: String) -> String { v == nil ? "-" : String(format: "%.4f%@", v!, suffix) }
}

private struct SpeedAltitudeRow: View {
    let trackPoint: TrackPoint
    let colors: TopOutColorScheme
    
    var body: some View {
        HStack(spacing: 16) {
            StatCard(
                title: "Speed",
                icon: "speedometer",
                triplet: (
                    String(format: "%.1f", trackPoint.vHorizontal), "H",
                    String(format: "%.1f", trackPoint.vVertical),   "V",
                    String(format: "%.1f", trackPoint.avgVertical), "Avg-V"
                ),
                background: colors.secondaryContainer.opacity(0.3),
                colors: colors
            )
            StatCard(
                title: "Altitude",
                icon: "mountain.2",
                triplet: (
                    "\(Int(trackPoint.gain))", "Gain",
                    "\(Int(trackPoint.loss))", "Loss",
                    "\(Int(trackPoint.relAltitude))", "Rel"
                ),
                background: colors.tertiaryContainer.opacity(0.3),
                colors: colors
            )
        }
    }
}

private struct StatCard: View {
    let title: String
    let icon: String
    let triplet: (String,String,String,String,String,String)
    let background: Color
    let colors: TopOutColorScheme
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 14)).foregroundColor(colors.primary)
                Text(title)
                    .font(.subheadline).bold()
            }
            DataTriplet(
                first:  triplet.0, second: triplet.2, third: triplet.4,
                firstLab: triplet.1, secondLab: triplet.3, thirdLab: triplet.5,
                colors: colors
            )
        }
        .padding(12)
        .background(background)
        .cornerRadius(12)
        .frame(maxWidth: .infinity)
    }
}

private struct DataTriplet: View {
    let first: String; let second: String; let third: String
    let firstLab: String; let secondLab: String; let thirdLab: String
    let colors: TopOutColorScheme
    
    var body: some View {
        HStack {
            ValueLabel(value: first, label: firstLab, colors: colors)
            ValueLabel(value: second, label: secondLab, colors: colors)
            ValueLabel(value: third, label: thirdLab, colors: colors)
        }
    }
}

private struct ValueLabel: View {
    let value: String; let label: String; let colors: TopOutColorScheme
    
    var body: some View {
        VStack {
            Text(value).font(.caption).bold()
            Text(label).font(.caption2).foregroundColor(colors.onSurfaceVariant)
        }.frame(maxWidth: .infinity)
    }
}

// MARK: – Bottom controls  (fixed corner names)

private struct BottomControls: View {
    let onStopClicked:   () -> Void
    let onCancelClicked: () -> Void
    
    var body: some View {
        HStack {
            ControlButton(
                label:    "Cancel",
                icon:     "xmark",
                gradient: [Color(red: 0.90, green: 0.45, blue: 0.45),
                           Color(red: 0.80, green: 0.18, blue: 0.18)],
                corners:  [.topLeft, .bottomLeft],      // ← fixed
                action:   onCancelClicked
            )
            ControlButton(
                label:    "Stop & Save",
                icon:     "stop.fill",
                gradient: [Color(red: 0.22, green: 0.55, blue: 0.24),
                           Color(red: 0.40, green: 0.73, blue: 0.42)],
                corners:  [.topRight, .bottomRight],    // ← fixed
                action:   onStopClicked
            )
        }
    }
}


private struct ControlButton: View {
    let label: String
    let icon: String
    let gradient: [Color]
    let corners: UIRectCorner
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image(systemName: icon).font(.system(size: 16))
                Text(label).bold()
            }
            .foregroundColor(.white)
            .frame(height: 48).frame(maxWidth: .infinity)
            .background(
                LinearGradient(colors: gradient, startPoint: .leading, endPoint: .trailing)
            )
            .cornerRadius(24, corners: corners)
        }
    }
}
