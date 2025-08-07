import SwiftUI

extension View {
    // Apply theme-aware background, passing the current theme color
    func themedBackground(_ color: Color) -> some View {
        self.background(color)
    }
    
    // Conditional view modifier
    @ViewBuilder
    func `if`<Content: View>(_ condition: Bool, transform: (Self) -> Content) -> some View {
        if condition {
            transform(self)
        } else {
            self
        }
    }
    
    // Hide keyboard
    func hideKeyboard() {
        UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    }
    
    // Corner radius for specific corners (keep ONLY this one in the project)
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
    
    // Apply shadow with theme colors (color could be injected too)
    func themedShadow() -> some View {
        self.shadow(
            color: Color.black.opacity(0.1),
            radius: 4,
            x: 0,
            y: 2
        )
    }
    
    // Apply border with a color from your theme
    func themedBorder(_ color: Color, width: CGFloat = 1) -> some View {
        self.overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(color, lineWidth: width)
        )
    }
    
    func topShadow(
        color: Color = .black.opacity(0.15),
        blur:  CGFloat = 12,
        distance: CGFloat = 6
    ) -> some View {
        // Shadow offset upward to create the stacking effect
        shadow(color: color,
               radius: blur,
               x: 0,
               y: -distance)
    }
}

// Custom shape for specific corner radius
struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}
