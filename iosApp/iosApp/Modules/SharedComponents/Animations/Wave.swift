//
//  WaveAnimationView.swift
//  iosApp
//
//  Created by Inon Elgabsi on 30/07/2025.
//

import SwiftUI
import Lottie

struct WaveAnimationView: View {
    let animationAsset: String
    let speed: Double
    let animationSize: CGFloat
    let iterations: Int

    /// Initializes a Lottie-based wave animation view.
    /// - Parameters:
    ///   - animationAsset: Name of the Lottie asset in your bundle (no ".json" needed).
    ///   - speed: Playback speed multiplier.
    ///   - animationSize: Width/height in points.
    ///   - iterations: 0 for infinite, 1 for once, N for N times.
    init(
        animationAsset: String = "Waves",
        speed: Double = 1.0,
        animationSize: CGFloat = 200,
        iterations: Int = 0  // 0 means infinite loop (like in Mountain)
    ) {
        self.animationAsset = animationAsset
        self.speed = speed
        self.animationSize = animationSize
        self.iterations = iterations
    }

    var body: some View {
        ZStack {
            if let animation = LottieAnimation.named(animationAsset) {
                LottieView(animation: animation)
                    .playing(loopMode: loopMode)
                    .animationSpeed(speed)
                    .frame(width: animationSize, height: animationSize)
                    .accessibilityLabel(Text("Wave animation"))
            } else {
                // Fallback: simple wave icon, styled like the mountain fallback
                Image(systemName: "waveform.path.ecg")
                    .resizable()
                    .scaledToFit()
                    .frame(width: animationSize * 0.8, height: animationSize * 0.8)
                    .foregroundColor(.blue)
                    .opacity(0.6)
                    .accessibilityLabel(Text("Wave fallback icon"))
            }
        }
        .frame(width: animationSize, height: animationSize)
    }

    private var loopMode: LottieLoopMode {
        switch iterations {
        case 0: return .loop
        case 1: return .playOnce
        default: return .repeat(Float(iterations))
        }
    }
}

#if DEBUG
struct WaveAnimationView_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 12) {
            WaveAnimationView(
                animationAsset: "Waves",
                speed: 1.0,
                animationSize: 200,
                iterations: 0
            )
            WaveAnimationView(
                animationAsset: "Waves",
                speed: 0.6,
                animationSize: 120,
                iterations: 1
            )
            WaveAnimationView(
                animationAsset: "not_found_asset",
                speed: 1.0,
                animationSize: 100,
                iterations: 2
            )
        }
        .padding()
        .previewLayout(.sizeThatFits)
    }
}
#endif
