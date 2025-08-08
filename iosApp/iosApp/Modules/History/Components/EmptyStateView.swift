import SwiftUI
import Lottie

struct EmptyStateView: View {
    let title: String
    let message: String
    let actionText: String
    let systemImage: String
    let onActionTapped: () -> Void
    let theme: AppTheme

    // Name of your Lottie asset (string literal)
    private let emptyListLottie = "empty_list_animation"

    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            
            // Use the shared LoadingAnimation if the Lottie file exists.
            if LottieAnimation.named(emptyListLottie) != nil {
                LoadingAnimation(
                    text: "",
                    animationAsset: emptyListLottie,
                    speed: 1.2,
                    animationSize: 200,
                    containerWidth: 220,
                    containerHeight: 180,
                    spacing: 0
                )
            } else {
                // Fallbacks: emoji + SF Symbol
                Text("📊")
                    .font(.system(size: 64))
                    .padding(.bottom, 8)
                
                Image(systemName: systemImage)
                    .font(.system(size: 72))
                    .foregroundColor(theme.primary.opacity(0.7))
            }
            
            Text(title)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(theme.onSurface)
                .multilineTextAlignment(.center)
            
            Text(message)
                .font(.body)
                .multilineTextAlignment(.center)
                .foregroundColor(theme.onSurfaceVariant)
                .padding(.horizontal, 32)
            
            Button(action: onActionTapped) {
                Text(actionText)
                    .font(.headline)
                    .foregroundColor(theme.onPrimary)
                    .padding(.horizontal, 32)
                    .padding(.vertical, 12)
                    .background(theme.primary)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            .padding(.top, 8)
            
            Spacer()
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 20)
    }
}
