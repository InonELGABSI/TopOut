import SwiftUI
import Lottie

struct EmptyStateView: View {
    let title: String
    let message: String
    let actionText: String
    let systemImage: String
    let onActionTapped: () -> Void
    let theme: AppTheme


    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            
            // Fallbacks: emoji + SF Symbol
            Image(systemName: systemImage)
                .font(.system(size: 72))
                .foregroundColor(theme.primary.opacity(0.7))
            
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
