//
//  Mountain.swift
//  iosApp
//
//  Created by Inon Elgabsi on 30/07/2025.
//

import SwiftUI
import Lottie

// Define a ViewModifier for customizing the animation
struct MountainAnimationModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
    }
}

struct MountainAnimation: View {
    let animationAsset: String
    let speed: Float
    let animationSize: CGFloat
    let iterations: Int
    
    init(
        animationAsset: String = "Travel_Mountain",
        speed: Float = 1.0,
        animationSize: CGFloat = 200,
        iterations: Int = 1
    ) {
        self.animationAsset = animationAsset
        self.speed = speed
        self.animationSize = animationSize
        self.iterations = iterations
    }
    
    var body: some View {
        MountainAnimationContent(
            animationAsset: animationAsset,
            speed: speed,
            iterations: iterations
        )
        .frame(width: animationSize, height: animationSize)
    }
}

struct MountainAnimationContent: UIViewRepresentable {
    let animationAsset: String
    let speed: Float
    let iterations: Int
    
    func makeUIView(context: Context) -> UIView {
        let containerView = UIView()
        
        // Try to load the Lottie animation from bundle
        // Remove .json extension if present since LottieAnimation.named doesn't need it
        let animationName = animationAsset.replacingOccurrences(of: ".json", with: "")
        
        guard let animation = LottieAnimation.named(animationName) else {
            // Fallback to a simple mountain icon if Lottie animation is not found
            return createFallbackMountain()
        }
        
        let animationView = LottieAnimationView(animation: animation)
        animationView.contentMode = .scaleAspectFit
        animationView.animationSpeed = CGFloat(speed)
        
        // Set loop mode based on iterations
        if iterations == 1 {
            animationView.loopMode = .playOnce
        } else if iterations == 0 {
            animationView.loopMode = .loop
        } else {
            animationView.loopMode = .repeat(Float(iterations))
        }
        
        containerView.addSubview(animationView)
        animationView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            animationView.topAnchor.constraint(equalTo: containerView.topAnchor),
            animationView.leadingAnchor.constraint(equalTo: containerView.leadingAnchor),
            animationView.trailingAnchor.constraint(equalTo: containerView.trailingAnchor),
            animationView.bottomAnchor.constraint(equalTo: containerView.bottomAnchor)
        ])
        
        // Start the animation
        animationView.play()
        
        context.coordinator.animationView = animationView
        
        return containerView
    }
    
    func updateUIView(_ uiView: UIView, context: Context) {
        guard let animationView = context.coordinator.animationView else { return }
        
        // Update animation speed
        animationView.animationSpeed = CGFloat(speed)
        
        // Update loop mode if iterations changed
        if iterations == 1 {
            animationView.loopMode = .playOnce
        } else if iterations == 0 {
            animationView.loopMode = .loop
        } else {
            animationView.loopMode = .repeat(Float(iterations))
        }
        
        // Restart animation if it's not playing
        if !animationView.isAnimationPlaying {
            animationView.play()
        }
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator()
    }
    
    private func createFallbackMountain() -> UIView {
        let containerView = UIView()
        let imageView = UIImageView()
        
        // Create a mountain icon fallback
        if let mountainImage = UIImage(systemName: "mountain.2.fill") {
            imageView.image = mountainImage
            imageView.tintColor = UIColor.systemGray
            imageView.contentMode = .scaleAspectFit
        }
        
        containerView.addSubview(imageView)
        imageView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            imageView.centerXAnchor.constraint(equalTo: containerView.centerXAnchor),
            imageView.centerYAnchor.constraint(equalTo: containerView.centerYAnchor),
            imageView.widthAnchor.constraint(equalTo: containerView.widthAnchor, multiplier: 0.8),
            imageView.heightAnchor.constraint(equalTo: containerView.heightAnchor, multiplier: 0.8)
        ])
        
        return containerView
    }
    
    class Coordinator: NSObject {
        var animationView: LottieAnimationView?
    }
}

// SwiftUI wrapper with simplified interface
struct MountainAnimationView: View {
    let animationAsset: String
    let speed: Float
    let animationSize: CGFloat
    let iterations: Int
    
    init(
        animationAsset: String = "Travel_Mountain",
        speed: Float = 1.0,
        animationSize: CGFloat = 200,
        iterations: Int = 1
    ) {
        self.animationAsset = animationAsset
        self.speed = speed
        self.animationSize = animationSize
        self.iterations = iterations
    }
    
    var body: some View {
        MountainAnimationContent(
            animationAsset: animationAsset,
            speed: speed,
            iterations: iterations
        )
        .frame(width: animationSize, height: animationSize)
    }
}

// Preview for SwiftUI
#if DEBUG
struct MountainAnimation_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 20) {
            MountainAnimationView(
                animationAsset: "Travel_Mountain",
                speed: 1.0,
                animationSize: 200,
                iterations: 1
            )
            
            MountainAnimationView(
                animationAsset: "Travel_Mountain",
                speed: 0.5,
                animationSize: 150,
                iterations: 0 // Infinite loop
            )
        }
        .padding()
    }
}
#endif
