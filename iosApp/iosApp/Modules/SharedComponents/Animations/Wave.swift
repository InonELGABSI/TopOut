//
//  Wave.swift
//  iosApp
//
//  Created by Inon Elgabsi on 30/07/2025.
//

import SwiftUI
import Shared

struct WaveShape: Shape {
    var yOffset: CGFloat
    var amplitude: CGFloat
    var frequency: CGFloat
    var phase: CGFloat
    
    var animatableData: CGFloat {
        get { phase }
        set { phase = newValue }
    }
    
    func path(in rect: CGRect) -> Path {
        let width = rect.width
        let height = rect.height
        let midHeight = height / 2 + yOffset
        
        var path = Path()
        path.move(to: CGPoint(x: 0, y: midHeight))
        
        for x in stride(from: 0, to: width, by: 1) {
            let relativeX = x / width
            let sine = sin(2 * .pi * (relativeX * frequency + phase))
            let y = midHeight + sine * amplitude
            path.addLine(to: CGPoint(x: x, y: y))
        }
        
        path.addLine(to: CGPoint(x: width, y: height))
        path.addLine(to: CGPoint(x: 0, y: height))
        path.closeSubpath()
        
        return path
    }
}

struct WaveView: View {
    var color: Color
    var speed: Double
    var frequency: Double
    var amplitude: Double
    var yOffset: Double
    
    @State private var phase: CGFloat = 0
    
    var body: some View {
        WaveShape(
            yOffset: CGFloat(yOffset),
            amplitude: CGFloat(amplitude),
            frequency: CGFloat(frequency),
            phase: phase
        )
        .fill(color)
        .onAppear {
            withAnimation(Animation.linear(duration: speed).repeatForever(autoreverses: false)) {
                phase = 1
            }
        }
    }
}

struct WaveAnimation: View {
    var speed: Float
    
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    
    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }
    
    init(speed: Float = 1.0) {
        self.speed = speed
    }
    
    var body: some View {
        ZStack {
            // First wave - larger, slower
            WaveView(
                color: colors.primary.opacity(0.3),
                speed: 3.0 / Double(speed),
                frequency: 1.5,
                amplitude: 10,
                yOffset: 0
            )
            
            // Second wave - medium
            WaveView(
                color: colors.primary.opacity(0.5),
                speed: 2.0 / Double(speed),
                frequency: 2.0,
                amplitude: 8,
                yOffset: 3
            )
            
            // Third wave - small, faster
            WaveView(
                color: colors.primary.opacity(0.7),
                speed: 1.5 / Double(speed),
                frequency: 2.5,
                amplitude: 6,
                yOffset: 5
            )
        }
    }
}
