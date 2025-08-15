//==============================================================
//  StartSessionContent.swift
//==============================================================

import SwiftUI
import Shared
import Lottie
import UIKit // Added for haptics

struct StartSessionContent: View {
    let hasLocationPermission: Bool
    let onStartClick: () -> Void
    let onRequestLocationPermission: () -> Void
    let onRefreshMSL: () -> Void
    let mslHeightState: MSLHeightState
    let theme: AppTheme

    var body: some View {
        ScrollView {
            VStack(spacing: 32) {
                Text("Ready to Track")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(theme.onSurface)

                Text("Start your climbing session to track altitude, speed, and get real-time alerts")
                    .font(.body)
                    .multilineTextAlignment(.center)
                    .foregroundColor(theme.onSurfaceVariant)
                    .padding(.horizontal, 16)

                MSLCard(
                    mslHeightState: mslHeightState, 
                    theme: theme,
                    onRefresh: onRefreshMSL
                )
                    .padding(.horizontal, 16)

                Button(action: {
                    hasLocationPermission ? onStartClick() : onRequestLocationPermission()
                }) {
                    HStack(spacing: 12) {
                        Image(systemName: "play.fill")
                        Text("Start Session")
                    }
                    .font(.headline)
                    .foregroundColor(theme.onPrimary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(theme.primary)
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
    let theme: AppTheme
    let onRefresh: () -> Void

    // Derived state helper
    private var isLoading: Bool { mslHeightState is MSLHeightState.Loading }

    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .bottom) {
                // Card background
                RoundedRectangle(cornerRadius: 12)
                    .fill(theme.primaryContainer.opacity(0.3))

                // Card content (text and info)
                VStack(spacing: 16) {
                    HStack {
                        Image(systemName: "arrow.up.and.down")
                            .foregroundColor(theme.primary)
                        Text("Current Mean Sea Level")
                            .font(.headline)
                            .foregroundColor(theme.primary)
                        Spacer()
                        // Refresh button with loading feedback & haptics
                        Button(action: {
                            guard !isLoading else { return }
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            onRefresh()
                        }) {
                            RefreshIcon(isLoading: isLoading, theme: theme)
                                .frame(width: 20, height: 20)
                                .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .disabled(isLoading)
                        .accessibilityLabel(isLoading ? "Refreshing MSL height" : "Refresh MSL height")
                        .accessibilityHint("Fetch latest Mean Sea Level data")
                        .opacity(isLoading ? 0.8 : 1.0)
                    }
                    VStack(spacing: 4) {
                        switch mslHeightState {
                        case is MSLHeightState.Loading:
                            ProgressView().scaleEffect(0.7)
                            Text("Getting location…")
                                .font(.caption)
                                .foregroundColor(theme.onSurfaceVariant)
                        case let success as MSLHeightState.Success:
                            Text("\(Int(success.data.mslHeight)) m")
                                .font(.system(size: 24, weight: .bold))
                                .foregroundColor(theme.primary)
                            Text("GPS \(Int(success.data.ellipsoidHeight)) m | Geoid \(Int(success.data.geoidHeight)) m")
                                .font(.caption)
                                .foregroundColor(theme.onSurfaceVariant)
                            Text(success.data.accuracy)
                                .font(.caption2)
                                .foregroundColor(theme.onSurfaceVariant)
                        case let error as MSLHeightState.Error:
                            Image(systemName: "exclamationmark.triangle")
                                .foregroundColor(theme.error)
                            Text(error.message)
                                .font(.caption)
                                .multilineTextAlignment(.center)
                                .foregroundColor(theme.error)
                        default:
                            EmptyView()
                        }
                    }
                    Spacer(minLength: 4) // <-- Spacer for visual balance above wave
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 12)
                .zIndex(1)

                // --- Wave Animation overlays bottom, fills card, clipped by card shape ---
                WaveAnimationView(
                    animationAsset: "Waves",
                    speed: 1.0,
                    animationSize: geo.size.width,
                    iterations: 0 // Infinite loop
                )
                .frame(width: geo.size.width, height: 48)
                .offset(y: 18) // Tweak so just the crest is visible
                .allowsHitTesting(false)
                .zIndex(0)
            }
            .clipShape(RoundedRectangle(cornerRadius: 12)) // 👈 Card + wave clipped together
            .frame(width: geo.size.width, height: 180, alignment: .bottom)
        }
        .frame(height: 180)
        .padding(.bottom, 0)
    }
}

// MARK: - Refresh Icon View
private struct RefreshIcon: View {
    let isLoading: Bool
    let theme: AppTheme

    var body: some View {
        ZStack {
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
                    .tint(theme.primary)
                    .scaleEffect(0.7)
            } else {
                Image(systemName: "arrow.clockwise")
                    .foregroundColor(theme.primary)
            }
        }
        .frame(width: 20, height: 20)
    }
}
