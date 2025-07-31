//==============================================================
//  StartSessionContent.swift
//  (unchanged – uses your original implementation)
//==============================================================

import SwiftUI
import Shared

struct StartSessionContent: View {
    let hasLocationPermission: Bool
    let onStartClick: () -> Void
    let onRequestLocationPermission: () -> Void
    let mslHeightState: MSLHeightState
    let colors: TopOutColorScheme
    
    var body: some View {
        ScrollView {
            VStack(spacing: 32) {
                Text("Ready to Track")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(colors.onSurface)
                
                Text("Start your climbing session to track altitude, speed, and get real-time alerts")
                    .font(.body)
                    .multilineTextAlignment(.center)
                    .foregroundColor(colors.onSurfaceVariant)
                    .padding(.horizontal, 16)
                
                MSLCard(mslHeightState: mslHeightState, colors: colors)
                    .padding(.horizontal, 16)
                                
                Button(action: {
                    hasLocationPermission ? onStartClick() : onRequestLocationPermission()
                }) {
                    HStack(spacing: 12) {
                        Image(systemName: "play.fill")
                        Text("Start Session")
                    }
                    .font(.headline)
                    .foregroundColor(colors.onPrimary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(colors.primary)
                    .clipShape(RoundedRectangle(cornerRadius: 28))
                }
                .padding(.horizontal, 16)
                
                Spacer(minLength: 40)
            }
        }
    }
}

private struct MSLCard: View {
    let mslHeightState: MSLHeightState
    let colors: TopOutColorScheme
    
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 12)
                .fill(colors.primaryContainer.opacity(0.3))
            VStack(spacing: 16) {
                HStack {
                    Image(systemName: "arrow.up.and.down")
                        .foregroundColor(colors.primary)
                    Text("Current Mean Sea Level")
                        .font(.headline)
                        .foregroundColor(colors.primary)
                    Spacer()
                }
                VStack(spacing: 4) {
                    switch mslHeightState {
                    case is MSLHeightState.Loading:
                        ProgressView().scaleEffect(0.7)
                        Text("Getting location…")
                            .font(.caption)
                            .foregroundColor(colors.onSurfaceVariant)
                    case let success as MSLHeightState.Success:
                        Text("\(Int(success.data.mslHeight)) m")
                            .font(.system(size: 24, weight: .bold))
                            .foregroundColor(colors.primary)
                        Text("GPS \(Int(success.data.ellipsoidHeight)) m | Geoid \(Int(success.data.geoidHeight)) m")
                            .font(.caption)
                            .foregroundColor(colors.onSurfaceVariant)
                        Text(success.data.accuracy)
                            .font(.caption2)
                            .foregroundColor(colors.onSurfaceVariant)
                    case let error as MSLHeightState.Error:
                        Image(systemName: "exclamationmark.triangle")
                            .foregroundColor(colors.error)
                        Text(error.message)
                            .font(.caption)
                            .multilineTextAlignment(.center)
                            .foregroundColor(colors.error)
                    default:
                        EmptyView()
                    }
                }
                WaveAnimationView(colors: colors)
                    .frame(height: 60)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(16)
        }
    }
}
