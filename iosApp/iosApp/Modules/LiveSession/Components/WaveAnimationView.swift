//==============================================================
//  WaveAnimationView.swift
//==============================================================

import SwiftUI

struct WaveAnimationView: View {
    @State private var phase: CGFloat = 0
    let colors: TopOutColorScheme
    
    var body: some View {
        ZStack {
            waveLayer(amplitude: 10, frequency: 0.1, phase: phase,     opacity: 0.3)
            waveLayer(amplitude: 15, frequency: 0.15, phase: phase*0.8,opacity: 0.2)
            waveLayer(amplitude:  8, frequency: 0.2, phase: phase*1.2,opacity: 0.4)
        }
        .onAppear {
            withAnimation(.linear(duration: 2).repeatForever(autoreverses: false)) {
                phase = .pi * 2
            }
        }
    }
    
    private func waveLayer(amplitude: CGFloat, frequency: CGFloat, phase: CGFloat, opacity: CGFloat) -> some View {
        Canvas { context, size in
            context.opacity = opacity
            var path = Path(); let mid = size.height * 0.5
            path.move(to: CGPoint(x: 0, y: mid))
            for x in stride(from: 0, to: size.width, by: 1) {
                let y = mid + amplitude * sin(frequency * x + phase)
                path.addLine(to: CGPoint(x: x, y: y))
            }
            path.addLines([.init(x: size.width, y: size.height), .init(x: 0, y: size.height)])
            path.closeSubpath()
            context.fill(path, with: .color(colors.primary.opacity(0.3)))
        }
    }
}
