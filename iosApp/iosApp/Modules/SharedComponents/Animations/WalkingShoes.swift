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
    
    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

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
            ZStack {
                if let animation = LottieAnimation.named(animationAsset) {
                    LottieView(animation: animation)
                        .playing(loopMode: .loop)
                        .animationSpeed(Double(speed))
                        .frame(width: animationSize, height: animationSize)
                } else {
                    Image(systemName: "figure.walk")
                        .font(.system(size: animationSize * 0.3))
                        .foregroundColor(theme.primary)
                }
            }
            .frame(width: containerWidth, height: containerHeight)
            
            Spacer().frame(height: spacing)
            
            Text(text)
                .font(.body)
                .foregroundColor(theme.onBackground)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.clear)
    }
}
