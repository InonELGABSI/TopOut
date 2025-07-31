//
//  MountainAnimationView.swift
//  iosApp
//
//  Created by Inon Elgabsi on 30/07/2025.
//

import SwiftUI
import Lottie

struct MountainAnimationView: View {
    let animationAsset: String
    let speed: Double
    let animationSize: CGFloat
    let iterations: Int

    init(
        animationAsset: String = "Travel_Mountain",
        speed: Double = 1.0,
        animationSize: CGFloat = 200,
        iterations: Int = 1
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
                    .playing(loopMode: loopMode)   // <-- add 'loopMode:' here
                    .animationSpeed(speed)
                    .frame(width: animationSize, height: animationSize)
                    .accessibilityLabel(Text("Mountain animation"))
            } else {
                Image(systemName: "mountain.2.fill")
                    .resizable()
                    .scaledToFit()
                    .frame(width: animationSize * 0.8, height: animationSize * 0.8)
                    .foregroundColor(.gray)
                    .opacity(0.6)
                    .accessibilityLabel(Text("Mountain fallback icon"))
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
struct MountainAnimationView_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 12) {
            MountainAnimationView(
                animationAsset: "Travel_Mountain",
                speed: 1.0,
                animationSize: 200,
                iterations: 1
            )
            MountainAnimationView(
                animationAsset: "Travel_Mountain",
                speed: 0.6,
                animationSize: 120,
                iterations: 0 // infinite
            )
            MountainAnimationView(
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
