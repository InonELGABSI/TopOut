//==============================================================
//  ErrorContent.swift
//==============================================================

import SwiftUI
import Shared

struct ErrorContent: View {
    let errorMessage: String
    let onRetryClick: () -> Void
    let colors: TopOutColorScheme
    
    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 64))
                .foregroundColor(colors.error)
            Text("Session Error")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(colors.onSurface)
            Text(errorMessage)
                .multilineTextAlignment(.center)
                .foregroundColor(colors.onSurfaceVariant)
                .padding(.horizontal, 32)
            Button(action: onRetryClick) {
                Label("Try Again", systemImage: "arrow.clockwise")
                    .font(.headline)
                    .foregroundColor(colors.onPrimary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(colors.primary)
                    .cornerRadius(28)
            }
            .padding(.horizontal, 24)
            .padding(.top, 16)
            Spacer()
        }
        .padding()
    }
}
