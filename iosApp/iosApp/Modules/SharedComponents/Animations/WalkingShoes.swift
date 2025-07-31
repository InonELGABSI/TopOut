//
//  WalkingShoes.swift
//  iosApp
//
//  Created by Inon Elgabsi on 30/07/2025.
//

import SwiftUI
import Shared
import Lottie

struct LoadingAnimation: View {
    let text: String
    let animationAsset: String
    let speed: Float
    let animationSize: CGFloat
    let containerWidth: CGFloat
    let containerHeight: CGFloat
    let spacing: CGFloat
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    init(
        text: String = "Loading...",
        animationAsset: String = "outdoor_boots_animation",
        speed: Float = 1.5,
        animationSize: CGFloat = 350,
        containerWidth: CGFloat = 350,
        containerHeight: CGFloat = 200,
        spacing: CGFloat = 16
    ) {
        self.text = text
        self.animationAsset = animationAsset
        self.speed = speed
        self.animationSize = animationSize
        self.containerWidth = containerWidth
        self.containerHeight = containerHeight
        self.spacing = spacing
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Animation container
            ZStack {
                if let animation = LottieAnimation.named(animationAsset) {
                    LottieView(animation: animation)
                        .playing(loopMode: .loop)
                        .animationSpeed(Double(speed))
                        .frame(width: animationSize, height: animationSize)
                } else {
                    // Fallback to system icon
                    Image(systemName: "figure.walk")
                        .font(.system(size: animationSize * 0.3))
                        .foregroundColor(colors.primary)
                }
            }
            .frame(width: containerWidth, height: containerHeight)
            
            Spacer().frame(height: spacing)
            
            Text(text)
                .font(.body)
                .foregroundColor(colors.onBackground)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.clear)
    }
}
