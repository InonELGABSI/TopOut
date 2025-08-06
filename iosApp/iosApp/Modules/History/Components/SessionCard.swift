import SwiftUI
import Shared

struct SessionCard: View {
    let session: Session
    let onSessionClick: (Session) -> Void

    // Theme system
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue

    private var currentTheme: ThemePalette {
        ThemePalette(rawValue: selectedTheme) ?? .classicRed
    }
    private var colors: TopOutColorScheme {
        currentTheme.scheme(for: colorScheme)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Header with title and date
            SessionHeader(
                title: session.title ?? "Unnamed Session",
                startTime: session.startTime?.int64,
                colors: colors
            )

            // Stats section
            SessionStats(session: session, colors: colors)

            // Duration if available
            if let startTime = session.startTime.int64,
               let endTime = session.endTime.int64,
               endTime > startTime {
                SessionDuration(startTime: startTime, endTime: endTime, colors: colors)
            }
        }
        .padding(16)
        .background(
            colors.surface                      // your fill colour
                .clipShape(                     // ⬅️ choose which corners
                    .rect(
                        topLeadingRadius: 24,
                        topTrailingRadius: 24
                    )
                )
                .shadow(color: .black.opacity(0.08), radius: 3, x: 0, y: -3)
        )
        .onTapGesture {
            onSessionClick(session)
        }
    }
}

@ViewBuilder
private func SessionHeader(title: String, startTime: Int64?, colors: TopOutColorScheme) -> some View {
    HStack {
        Text(title)
            .font(.system(size: 18, weight: .bold))
            .foregroundColor(colors.onSurface)
            .lineLimit(1)
            .frame(maxWidth: .infinity, alignment: .leading)

        if let timestamp = startTime {
            Text(formatDate(timestamp))
                .font(.subheadline)
                .foregroundColor(colors.onSurfaceVariant)
        }
    }
}

@ViewBuilder
private func SessionStats(session: Session, colors: TopOutColorScheme) -> some View {
    HStack(spacing: 0) {
        // Total Ascent
        if let ascent = session.totalAscent {
            StatItem(
                value: "\(Int(truncating: ascent))m",
                label: "Ascent",
                color: Color.green,
                colors: colors
            )
        }

        // Total Descent
        if let descent = session.totalDescent {
            StatItem(
                value: "\(Int(truncating: descent))m",
                label: "Descent",
                color: Color.orange,
                colors: colors
            )
        }

        // Max Altitude
        if let altitude = session.maxAltitude {
            StatItem(
                value: "\(Int(truncating: altitude))m",
                label: "Max Alt",
                color: Color.blue,
                colors: colors
            )
        }

        // Average Rate
        if let rate = session.avgRate {
            StatItem(
                value: String(format: "%.1f", rate),
                label: "Avg Rate",
                color: Color.purple,
                colors: colors
            )
        }
    }
    .padding(16)
    .background(
        RoundedRectangle(cornerRadius: 12)
            .fill(colors.surfaceVariant.opacity(0.1))
    )
}

@ViewBuilder
private func StatItem(value: String, label: String, color: Color, colors: TopOutColorScheme) -> some View {
    VStack(spacing: 4) {
        Text(value)
            .font(.system(size: 14, weight: .bold))
            .foregroundColor(color)

        Text(label)
            .font(.system(size: 12))
            .foregroundColor(colors.onSurfaceVariant)
    }
    .frame(maxWidth: .infinity)
}

@ViewBuilder
private func SessionDuration(startTime: Int64, endTime: Int64, colors: TopOutColorScheme) -> some View {
    let durationMs = endTime - startTime
    let durationText = formatDuration(durationMs)

    HStack {
        Image(systemName: "clock")
            .foregroundColor(colors.primary)
            .font(.system(size: 16))

        Text("Duration: \(durationText)")
            .font(.system(size: 14, weight: .medium))
            .foregroundColor(colors.onSurface)
    }
    .padding(.horizontal, 12)
    .padding(.vertical, 8)
    .background(
        RoundedRectangle(cornerRadius: 8)
            .fill(colors.surfaceVariant.opacity(0.3))
    )
}

private func formatDate(_ timestamp: Int64?) -> String {
    guard let timestamp = timestamp else { return "N/A" }
    let date = Date(timeIntervalSince1970: TimeInterval(timestamp / 1000))
    let formatter = DateFormatter()
    formatter.dateFormat = "MMM dd, HH:mm"
    return formatter.string(from: date)
}
