import SwiftUI
import Shared

struct SessionCard: View {
    let session: Session
    let onSessionClick: (Session) -> Void
    let theme: AppTheme

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            SessionHeader(
                title: session.title ?? "Unnamed Session",
                startTime: session.startTime?.int64,
                theme: theme
            )

            SessionStats(session: session, theme: theme)

            if let startTime = session.startTime.int64,
               let endTime = session.endTime.int64,
               endTime > startTime {
                SessionDuration(startTime: startTime, endTime: endTime, theme: theme)
            }
        }
        .padding(16)
        .background(
            theme.surface
                .clipShape(
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
private func SessionHeader(title: String, startTime: Int64?, theme: AppTheme) -> some View {
    HStack {
        Text(title)
            .font(.system(size: 18, weight: .bold))
            .foregroundColor(theme.onSurface)
            .lineLimit(1)
            .frame(maxWidth: .infinity, alignment: .leading)

        if let timestamp = startTime {
            Text(formatDate(timestamp))
                .font(.subheadline)
                .foregroundColor(theme.onSurfaceVariant)
        }
    }
}

@ViewBuilder
private func SessionStats(session: Session, theme: AppTheme) -> some View {
    HStack(spacing: 0) {
        if let ascent = session.totalAscent {
            StatItem(
                value: "\(Int(truncating: ascent))m",
                label: "Ascent",
                color: Color.green,
                theme: theme
            )
        }

        if let descent = session.totalDescent {
            StatItem(
                value: "\(Int(truncating: descent))m",
                label: "Descent",
                color: Color.orange,
                theme: theme
            )
        }

        if let altitude = session.maxAltitude {
            StatItem(
                value: "\(Int(truncating: altitude))m",
                label: "Max Alt",
                color: Color.blue,
                theme: theme
            )
        }

        if let avgH = session.avgHorizontal {
            StatItem(
                value: String(format: "%.1f", avgH),
                label: "Avg-H",
                color: Color.purple,
                theme: theme
            )
        }

        if let avgV = session.avgVertical {
            StatItem(
                value: String(format: "%.1f", avgV),
                label: "Avg-V",
                color: Color.brown,
                theme: theme
            )
        }
    }
    .padding(16)
    .background(
        RoundedRectangle(cornerRadius: 12)
            .fill(theme.surfaceVariant.opacity(0.1))
    )
}

@ViewBuilder
private func StatItem(value: String, label: String, color: Color, theme: AppTheme) -> some View {
    VStack(spacing: 4) {
        Text(value)
            .font(.system(size: 14, weight: .bold))
            .foregroundColor(color)

        Text(label)
            .font(.system(size: 12))
            .foregroundColor(theme.onSurfaceVariant)
    }
    .frame(maxWidth: .infinity)
}

@ViewBuilder
private func SessionDuration(startTime: Int64, endTime: Int64, theme: AppTheme) -> some View {
    let durationMs = endTime - startTime
    let durationText = formatDuration(durationMs)

    HStack {
        Image(systemName: "clock")
            .foregroundColor(theme.primary)
            .font(.system(size: 16))

        Text("Duration: \(durationText)")
            .font(.system(size: 14, weight: .medium))
            .foregroundColor(theme.onSurface)
    }
    .padding(.horizontal, 12)
    .padding(.vertical, 8)
    .background(
        RoundedRectangle(cornerRadius: 8)
            .fill(theme.surfaceVariant.opacity(0.3))
    )
}

private func formatDate(_ timestamp: Int64?) -> String {
    guard let timestamp = timestamp else { return "N/A" }
    let date = Date(timeIntervalSince1970: TimeInterval(timestamp / 1000))
    let formatter = DateFormatter()
    formatter.dateFormat = "MMM dd, HH:mm"
    return formatter.string(from: date)
}
