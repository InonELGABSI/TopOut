import SwiftUI
#if canImport(UIKit)
import UIKit
#endif

extension AnyTransition {
    static var feedbackToast: AnyTransition {
        let insertion = AnyTransition.move(edge: .bottom)
            .combined(with: .opacity)
        let removal = AnyTransition.move(edge: .bottom)
            .combined(with: .opacity)
            .combined(with: .scale(scale: 0.9, anchor: .center))
        return .asymmetric(insertion: insertion, removal: removal)
    }
}

struct FeedbackToast: View {
    let message: String
    let success: Bool
    let onDismiss: () -> Void

    // Gesture state for swipe-to-dismiss
    @State private var dragOffset: CGFloat = 0

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            Image(systemName: success ? "checkmark.circle.fill" : "xmark.octagon.fill")
                .font(.system(size: 22, weight: .semibold))
                .foregroundStyle(success ? Color.green : Color.red)
                .symbolRenderingMode(.hierarchical)
                .accessibilityHidden(true)
            Text(message)
                .font(.system(.subheadline, weight: .semibold))
                .foregroundStyle(Color.primary)
                .multilineTextAlignment(.leading)
                .lineLimit(nil)
                .minimumScaleFactor(0.9)
                .accessibilityLabel(message)
        }
        .padding(.vertical, 14)
        .padding(.horizontal, 18)
        .frame(minWidth: 200, idealWidth: 320, maxWidth: min(360, UIScreen.main.bounds.width - 32), alignment: .leading)
        .background(.regularMaterial, in: Capsule())
        .overlay(
            Capsule()
                .strokeBorder((success ? Color.green : Color.red).opacity(0.25), lineWidth: 0.8)
        )
        .shadow(color: Color.black.opacity(0.15), radius: 12, y: 6)
        .offset(y: dragOffset)
        .gesture(
            DragGesture(minimumDistance: 8)
                .onChanged { value in
                    if value.translation.height > 0 { dragOffset = value.translation.height }
                }
                .onEnded { value in
                    if value.translation.height > 60 { withAnimation(.spring(response:0.35,dampingFraction:0.85)) { onDismiss() } }
                    dragOffset = 0
                }
        )
        .contentShape(Rectangle())
        .onTapGesture { withAnimation(.easeInOut(duration:0.2)) { onDismiss() } }
        .transition(.feedbackToast)
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.updatesFrequently)
    }
}

// Keep the old name for backward compatibility
typealias SessionFeedbackToast = FeedbackToast

#Preview {
    ZStack {
        Color(.systemBackground)
        VStack(spacing: 16) {
            FeedbackToast(message: "Operation completed successfully", success: true) {}
            FeedbackToast(message: "Failed to complete operation. Check your connection & retry.", success: false) {}
        }
        .padding(.bottom, 40)
        .frame(maxHeight: .infinity, alignment: .bottom)
    }
}
