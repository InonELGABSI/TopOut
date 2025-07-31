import SwiftUI

struct DangerToast: View {
    let message: String
    let isVisible: Bool
    let color: Color
    let onDismiss: () -> Void

    var body: some View {
        if isVisible {
            HStack(spacing: 12) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .foregroundColor(.white)
                Text(message)
                    .foregroundColor(.white)
                    .fontWeight(.medium)
                    .multilineTextAlignment(.leading)
            }
            .padding(.vertical, 14)
            .padding(.horizontal, 28)
            .background(
                RoundedRectangle(cornerRadius: 14)
                    .fill(color)
                    .shadow(radius: 8, y: 4)
            )
            .transition(.move(edge: .bottom).combined(with: .opacity))
            .onAppear {
                // Auto dismiss after 3 seconds
                DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                    onDismiss()
                }
            }
            .animation(.spring(), value: isVisible)
        }
    }
}
