//==============================================================
//  ErrorContent.swift
//==============================================================

import SwiftUI
import Shared

struct ErrorContent: View {
    let errorMessage: String
    let onRetryClick: () -> Void
    let theme: AppTheme

    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 64))
                .foregroundColor(theme.error)
            Text("Session Error")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(theme.onSurface)
            Text(errorMessage)
                .multilineTextAlignment(.center)
                .foregroundColor(theme.onSurfaceVariant)
                .padding(.horizontal, 32)
            Button(action: onRetryClick) {
                Label("Try Again", systemImage: "arrow.clockwise")
                    .font(.headline)
                    .foregroundColor(theme.onPrimary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(theme.primary)
                    .cornerRadius(28)
            }
            .padding(.horizontal, 24)
            .padding(.top, 16)
            Spacer()
        }
        .padding()
    }
}
