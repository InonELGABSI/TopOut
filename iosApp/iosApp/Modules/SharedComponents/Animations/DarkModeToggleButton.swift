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
        guard let animation = LottieAnimation.named("Dark_Mode_Button") else {
            // Fallback to a simple toggle button if Lottie animation is not found
            return createFallbackButton(context: context)
        }
        
        let animationView = LottieAnimationView(animation: animation)
        animationView.loopMode = .playOnce
        animationView.animationSpeed = 1.0
        animationView.contentMode = .scaleAspectFit
        
        // Use a square container to maintain aspect ratio
        containerView.frame = CGRect(x: 0, y: 0, width: height, height: height)
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

        context.coordinator.isToggled = isToggled

        let from = animationView.currentProgress
        let to = targetProgress

        animationView.animationSpeed = 1.0

        // If you want to instantly snap when already at target:
        if abs(to - from) < 0.001 {
            animationView.currentProgress = to
        } else {
            animationView.play(fromProgress: from, toProgress: to, loopMode: .playOnce, completion: nil)
        }
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
        var isToggled: Bool        // change from `let` to `var`
        let onToggle: (Bool) -> Void
        var animationView: LottieAnimationView?
        
        init(onToggle: @escaping (Bool) -> Void, isToggled: Bool) {
            self.onToggle  = onToggle
            self.isToggled = isToggled
        }
        
        @objc func handleTap() {
            onToggle(!isToggled)   // will now flip correctly
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
