//
//  DarkModeToggleButton.swift
//  iosApp
//
//  Created by Inon Elgabsi on 30/07/2025.
//

import SwiftUI
import Lottie

struct LottieToggleButton: View {
    let isToggled: Bool
    let onToggle: (Bool) -> Void
    let height: CGFloat
    
    @State private var animatedProgress: CGFloat = 0
    @State private var animationView: LottieAnimationView?
    
    init(
        isToggled: Bool,
        onToggle: @escaping (Bool) -> Void,
        height: CGFloat = 48
    ) {
        self.isToggled = isToggled
        self.onToggle = onToggle
        self.height = height
    }
    
    var body: some View {
        LottieToggleButtonContent(
            isToggled: isToggled,
            onToggle: onToggle,
            height: height
        )
    }
}

struct LottieToggleButtonContent: UIViewRepresentable {
    let isToggled: Bool
    let onToggle: (Bool) -> Void
    let height: CGFloat
    
    // Animation frame constants to match Android
    private let startFrame: CGFloat = 0
    private let endFrame: CGFloat = 40
    private let totalFrames: CGFloat = 320
    
    private var startProgress: CGFloat {
        startFrame / totalFrames
    }
    
    private var endProgress: CGFloat {
        endFrame / totalFrames
    }
    
    private var targetProgress: CGFloat {
        isToggled ? startProgress : endProgress
    }
    
    func makeUIView(context: Context) -> UIView {
        let containerView = UIView()
        
        // Try to load the Lottie animation from bundle
        guard let animation = LottieAnimation.named("Dark Mode Button") else {
            // Fallback to a simple toggle button if Lottie animation is not found
            return createFallbackButton(context: context)
        }
        
        let animationView = LottieAnimationView(animation: animation)
        animationView.loopMode = .playOnce
        animationView.animationSpeed = 1.0
        animationView.contentMode = .scaleAspectFit
        
        // Calculate aspect ratio
        let aspectRatio = animation.bounds.width / animation.bounds.height
        let width = height * aspectRatio
        
        containerView.frame = CGRect(x: 0, y: 0, width: width, height: height)
        animationView.frame = containerView.bounds
        
        containerView.addSubview(animationView)
        
        // Add tap gesture
        let tapGesture = UITapGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.handleTap))
        containerView.addGestureRecognizer(tapGesture)
        containerView.isUserInteractionEnabled = true
        
        // Set initial progress
        animationView.currentProgress = targetProgress
        
        context.coordinator.animationView = animationView
        
        return containerView
    }
    
    func updateUIView(_ uiView: UIView, context: Context) {
        guard let animationView = context.coordinator.animationView else { return }
        
        // Animate to target progress with easing
        let animation = CABasicAnimation(keyPath: "currentProgress")
        animation.fromValue = animationView.currentProgress
        animation.toValue = targetProgress
        animation.duration = 0.6
        animation.timingFunction = CAMediaTimingFunction(name: .easeInEaseOut)
        animation.fillMode = .forwards
        animation.isRemovedOnCompletion = false
        
        animationView.layer.add(animation, forKey: "progressAnimation")
        animationView.currentProgress = targetProgress
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(onToggle: onToggle, isToggled: isToggled)
    }
    
    private func createFallbackButton(context: Context) -> UIView {
        let button = UIButton(type: .system)
        button.frame = CGRect(x: 0, y: 0, width: height, height: height)
        button.layer.cornerRadius = height / 2
        button.backgroundColor = isToggled ? UIColor.systemBlue : UIColor.systemGray4
        
        let iconName = isToggled ? "moon.fill" : "sun.max.fill"
        let iconColor = isToggled ? UIColor.white : UIColor.black
        
        if let image = UIImage(systemName: iconName) {
            button.setImage(image, for: .normal)
            button.tintColor = iconColor
        }
        
        button.addTarget(context.coordinator, action: #selector(Coordinator.handleTap), for: .touchUpInside)
        
        return button
    }
    
    class Coordinator: NSObject {
        let onToggle: (Bool) -> Void
        let isToggled: Bool
        var animationView: LottieAnimationView?
        
        init(onToggle: @escaping (Bool) -> Void, isToggled: Bool) {
            self.onToggle = onToggle
            self.isToggled = isToggled
        }
        
        @objc func handleTap() {
            onToggle(!isToggled)
        }
    }
}

// SwiftUI wrapper with proper sizing
struct LottieToggleButtonView: View {
    let isToggled: Bool
    let onToggle: (Bool) -> Void
    let height: CGFloat
    
    init(
        isToggled: Bool,
        onToggle: @escaping (Bool) -> Void,
        height: CGFloat = 48
    ) {
        self.isToggled = isToggled
        self.onToggle = onToggle
        self.height = height
    }
    
    var body: some View {
        LottieToggleButtonContent(
            isToggled: isToggled,
            onToggle: onToggle,
            height: height
        )
        .frame(height: height)
        .aspectRatio(contentMode: .fit)
    }
}

// Preview for SwiftUI
#if DEBUG
struct LottieToggleButton_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 20) {
            LottieToggleButtonView(
                isToggled: false,
                onToggle: { _ in },
                height: 48
            )
            
            LottieToggleButtonView(
                isToggled: true,
                onToggle: { _ in },
                height: 48
            )
        }
        .padding()
    }
}
#endif
