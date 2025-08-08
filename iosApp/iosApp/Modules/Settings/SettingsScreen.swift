import SwiftUI
import Shared

struct SettingsView: View {
    @ObservedObject private(set) var viewModel = ViewModelWrapper<SettingsState, SettingsViewModel>()
    @State private var showSignOutDialog = false
    @State private var showDeleteAccountDialog = false

    // State for editing sections
    @State private var isEditingProfile = false
    @State private var isEditingPreferences = false
    @State private var isEditingThresholds = false

    @EnvironmentObject private var themeManager: AppThemeManager
    @Environment(\.appTheme) private var theme

    var body: some View {
        ZStack {
            theme.background.ignoresSafeArea()

            switch onEnum(of: viewModel.uiState) {
            case .loading:
                SettingsLoadingContent(theme: theme)
            case .loaded(let state):
                ScrollView {
                    LazyVStack(spacing: 0, pinnedViews: []) {
                        ProfileCard(
                            user: state.user,
                            isEditing: isEditingProfile,
                            onToggleEdit: { isEditingProfile = $0 },
                            onUpdateUser: { user in viewModel.viewModel.updateUser(user: user) },
                            theme: theme
                        )
                        .zIndex(1)
                        .padding(.top, 8)

                        PreferencesCard(
                            user: state.user,
                            isEditing: isEditingPreferences,
                            onToggleEdit: { isEditingPreferences = $0 },
                            onUpdateUser: { user in viewModel.viewModel.updateUser(user: user) },
                            theme: theme
                        )
                        .zIndex(2)

                        ThresholdsCard(
                            user: state.user,
                            isEditing: isEditingThresholds,
                            onToggleEdit: { isEditingThresholds = $0 },
                            onUpdateUser: { user in viewModel.viewModel.updateUser(user: user) },
                            theme: theme
                        )
                        .zIndex(3)

                        ThemeCard(theme: theme, themeManager: themeManager)
                        .zIndex(4)
                    }
                }
            case .error(let state):
                SettingsErrorContent(errorMessage: state.errorMessage, theme: theme)

            @unknown default:
                EmptyView()
            }
        }
        .onAppear { viewModel.startObserving() }
    }
}

// MARK: - Card Components

struct ProfileCard: View {
    let user: User
    let isEditing: Bool
    let onToggleEdit: (Bool) -> Void
    let onUpdateUser: (User) -> Void
    let theme: AppTheme

    @State private var editableUser: EditableUser

    init(user: User, isEditing: Bool, onToggleEdit: @escaping (Bool) -> Void, onUpdateUser: @escaping (User) -> Void, theme: AppTheme) {
        self.user = user
        self.isEditing = isEditing
        self.onToggleEdit = onToggleEdit
        self.onUpdateUser = onUpdateUser
        self.theme = theme
        _editableUser = State(initialValue: EditableUser(user: user))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(spacing: 8) {
                // Leading: Icon + Title
                Image(systemName: "person.fill")
                    .foregroundColor(theme.primary)
                Text("Profile")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(theme.onSurface)
                Spacer() // Pushes the button to the right

                // Trailing: Edit button (shown only when not editing)
                if !isEditing {
                    Button(action: {
                        editableUser = EditableUser(user: user)
                        onToggleEdit(true)
                    }) {
                        Circle()
                            .fill(theme.primary)
                            .frame(width: 40, height: 40)
                            .overlay(
                                Image(systemName: "pencil")
                                    .foregroundColor(theme.onPrimary)
                            )
                    }
                }
            }


            Group {
                EditableField(
                    label: "Name",
                    value: $editableUser.name,
                    isEditing: isEditing,
                    icon: "person.fill",
                    theme: theme
                )
                EditableField(
                    label: "Email",
                    value: $editableUser.email,
                    isEditing: isEditing,
                    icon: "envelope.fill",
                    keyboardType: .emailAddress,
                    theme: theme
                )
                EditableField(
                    label: "Image URL",
                    value: $editableUser.imgUrl,
                    isEditing: isEditing,
                    icon: "photo.fill",
                    theme: theme
                )
                ReadOnlyField(
                    label: "User ID",
                    value: editableUser.id,
                    icon: "key.fill",
                    theme: theme
                )
                ReadOnlyField(
                    label: "Created At",
                    value: formatDate(editableUser.createdAt),
                    icon: "calendar",
                    theme: theme
                )
            }

            if isEditing {
                HStack(spacing: 8) {
                    Button(action: {
                        onToggleEdit(false)
                        editableUser = EditableUser(user: user)
                    }) {
                        Text("Cancel")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(theme.surfaceVariant)
                            .foregroundColor(theme.onSurfaceVariant)
                            .cornerRadius(8)
                    }
                    Button(action: {
                        onUpdateUser(editableUser.toUser())
                        onToggleEdit(false)
                    }) {
                        Text("Save")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(theme.primary)
                            .foregroundColor(theme.onPrimary)
                            .cornerRadius(8)
                    }
                }
            }
        }
        .padding(20)
        .background(
            theme.surface
                .clipShape(
                    .rect(
                        topLeadingRadius: 24,
                        bottomLeadingRadius: 0,
                        bottomTrailingRadius: 0,
                        topTrailingRadius: 24
                    )
                )
        )
        .topShadow(blur: 12, distance: 6)          // first soft penumbra
        .topShadow(color: .black.opacity(0.08),
                   blur: 24,
                   distance: 12)                    // second larger bloom

    }
}

struct PreferencesCard: View {
    let user: User
    let isEditing: Bool
    let onToggleEdit: (Bool) -> Void
    let onUpdateUser: (User) -> Void
    let theme: AppTheme

    @State private var editableUser: EditableUser

    init(user: User, isEditing: Bool, onToggleEdit: @escaping (Bool) -> Void, onUpdateUser: @escaping (User) -> Void, theme: AppTheme) {
        self.user = user
        self.isEditing = isEditing
        self.onToggleEdit = onToggleEdit
        self.onUpdateUser = onUpdateUser
        self.theme = theme
        _editableUser = State(initialValue: EditableUser(user: user))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            ZStack {
                HStack(spacing: 8) {
                    Image(systemName: "gearshape.fill")
                        .foregroundColor(theme.primary)
                    Text("Preferences")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(theme.onSurface)
                }
                if !isEditing {
                    HStack {
                        Spacer()
                        Button(action: {
                            editableUser = EditableUser(user: user)
                            onToggleEdit(true)
                        }) {
                            Circle()
                                .fill(theme.primary)
                                .frame(width: 40, height: 40)
                                .overlay(
                                    Image(systemName: "pencil")
                                        .foregroundColor(theme.onPrimary)
                                )
                        }
                    }
                }
            }

            UnitPreferenceSelector(
                currentUnit: editableUser.unitPreference,
                onUnitChange: { unit in editableUser.unitPreference = unit },
                isEditing: isEditing,
                theme: theme
            )
            NotificationToggle(
                enabled: editableUser.enabledNotifications,
                onToggle: { enabled in editableUser.enabledNotifications = enabled },
                isEditing: isEditing,
                theme: theme
            )

            if isEditing {
                HStack(spacing: 8) {
                    Button(action: {
                        onToggleEdit(false)
                        editableUser = EditableUser(user: user)
                    }) {
                        Text("Cancel")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(theme.surfaceVariant)
                            .foregroundColor(theme.onSurfaceVariant)
                            .cornerRadius(8)
                    }
                    Button(action: {
                        onUpdateUser(editableUser.toUser())
                        onToggleEdit(false)
                    }) {
                        Text("Save")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(theme.primary)
                            .foregroundColor(theme.onPrimary)
                            .cornerRadius(8)
                    }
                }
            }
        }
        .padding(20)
        .background(
            theme.surface
                .clipShape(
                    .rect(
                        topLeadingRadius: 24,
                        bottomLeadingRadius: 0,
                        bottomTrailingRadius: 0,
                        topTrailingRadius: 24
                    )
                )
        )
        .topShadow(blur: 12, distance: 6)          // first soft penumbra
        .topShadow(color: .black.opacity(0.08),
                   blur: 24,
                   distance: 12)                    // second larger bloom

    }
}

struct ThresholdsCard: View {
    let user: User
    let isEditing: Bool
    let onToggleEdit: (Bool) -> Void
    let onUpdateUser: (User) -> Void
    let theme: AppTheme

    @State private var editableUser: EditableUser

    init(user: User, isEditing: Bool, onToggleEdit: @escaping (Bool) -> Void, onUpdateUser: @escaping (User) -> Void, theme: AppTheme) {
        self.user = user
        self.isEditing = isEditing
        self.onToggleEdit = onToggleEdit
        self.onUpdateUser = onUpdateUser
        self.theme = theme
        _editableUser = State(initialValue: EditableUser(user: user))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            ZStack {
                HStack(spacing: 8) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .foregroundColor(theme.primary)
                    Text("Alert Thresholds")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(theme.onSurface)
                }
                if !isEditing {
                    HStack {
                        Spacer()
                        Button(action: {
                            editableUser = EditableUser(user: user)
                            onToggleEdit(true)
                        }) {
                            Circle()
                                .fill(theme.primary)
                                .frame(width: 40, height: 40)
                                .overlay(
                                    Image(systemName: "pencil")
                                        .foregroundColor(theme.onPrimary)
                                )
                        }
                    }
                }
            }

            ThresholdField(
                label: "Relative Height Threshold",
                value: $editableUser.relativeHeightFromStartThr,
                isEditing: isEditing,
                unit: editableUser.unitPreference,
                theme: theme
            )
            ThresholdField(
                label: "Total Height Threshold",
                value: $editableUser.totalHeightFromStartThr,
                isEditing: isEditing,
                unit: editableUser.unitPreference,
                theme: theme
            )
            ThresholdField(
                label: "Average Speed Threshold",
                value: $editableUser.currentAvgHeightSpeedThr,
                isEditing: isEditing,
                unit: "\(editableUser.unitPreference)/min",
                theme: theme
            )

            if isEditing {
                HStack(spacing: 8) {
                    Button(action: {
                        onToggleEdit(false)
                        editableUser = EditableUser(user: user)
                    }) {
                        Text("Cancel")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(theme.surfaceVariant)
                            .foregroundColor(theme.onSurfaceVariant)
                            .cornerRadius(8)
                    }
                    Button(action: {
                        onUpdateUser(editableUser.toUser())
                        onToggleEdit(false)
                    }) {
                        Text("Save")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(theme.primary)
                            .foregroundColor(theme.onPrimary)
                            .cornerRadius(8)
                    }
                }
            }
        }
        .padding(20)
        .background(
            theme.surface
                .clipShape(
                    .rect(
                        topLeadingRadius: 24,
                        bottomLeadingRadius: 0,
                        bottomTrailingRadius: 0,
                        topTrailingRadius: 24
                    )
                )
        )
        .topShadow(blur: 12, distance: 6)          // first soft penumbra
        .topShadow(color: .black.opacity(0.08),
                   blur: 24,
                   distance: 12)                    // second larger bloom

    }
}

struct ThemeCard: View {
    let theme: AppTheme
    let themeManager: AppThemeManager
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            // — Header row —
            HStack {
                Label("Theme", systemImage: "paintpalette.fill")
                    .foregroundColor(theme.primary)
                    .labelStyle(.titleAndIcon)
                    .font(.title2.bold())
                    .foregroundColor(theme.onSurface)

                Spacer(minLength: 0)

                // Dark mode toggle with Lottie animation
                LottieToggleButton(
                    isToggled: colorScheme == .dark,
                    onToggle: { isDark in
                        // Toggle system appearance
                        UIApplication.shared.connectedScenes
                            .compactMap { $0 as? UIWindowScene }
                            .first?.windows.first?
                            .overrideUserInterfaceStyle = isDark ? .dark : .light
                    },
                    height: 60
                )
                .frame(width: 80, height: 60)
                .fixedSize()
            }
            .frame(maxWidth: .infinity)

            Text("Color Palette")
                .font(.subheadline.weight(.medium))
                .foregroundColor(theme.onSurfaceVariant)

            VStack(spacing: 12) {
                ForEach(AppTheme.allCases, id: \.self) { appTheme in
                    ThemeOptionItem(
                        appTheme: appTheme,
                        isSelected: themeManager.selectedTheme == appTheme.rawValue,
                        name: appTheme.displayName,
                        theme: theme,
                        onSelect: { themeManager.setTheme($0) }
                    )
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(20)
        .background(
            theme.surface
                .clipShape(
                    .rect(
                        topLeadingRadius: 24,
                        bottomLeadingRadius: 0,
                        bottomTrailingRadius: 0,
                        topTrailingRadius: 24
                    )
                )
        )
        .topShadow(blur: 12, distance: 6)
        .topShadow(color: .black.opacity(0.08),
                   blur: 24,
                   distance: 12)
    }
}

// MARK: - Field Components

struct EditableField: View {
    let label: String
    @Binding var value: String  // Non-optional!
    let isEditing: Bool
    let icon: String
    var keyboardType: UIKeyboardType = .default
    let theme: AppTheme

    var body: some View {
        if isEditing {
            VStack(alignment: .leading, spacing: 4) {
                Text(label)
                    .font(.caption)
                    .foregroundColor(theme.onSurfaceVariant)
                HStack {
                    Image(systemName: icon)
                        .foregroundColor(theme.primary)
                    TextField(label, text: $value)
                        .keyboardType(keyboardType)
                }
                .padding()
                .background(theme.surfaceVariant.opacity(0.3))
                .cornerRadius(8)
            }
        } else {
            ReadOnlyField(
                label: label,
                value: value.isEmpty ? "Not set" : value,
                icon: icon,
                theme: theme
            )
        }
    }
}


struct ReadOnlyField: View {
    let label: String
    let value: String
    let icon: String
    let theme: AppTheme

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .foregroundColor(theme.onSurfaceVariant)
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.caption)
                    .foregroundColor(theme.onSurfaceVariant)
                Text(value)
                    .font(.body)
                    .foregroundColor(theme.onSurface)
            }
        }
    }
}

struct UnitPreferenceSelector: View {
    let currentUnit: String
    let onUnitChange: (String) -> Void
    let isEditing: Bool
    let theme: AppTheme
    @State private var showingPicker = false

    var body: some View {
        if isEditing {
            VStack(alignment: .leading, spacing: 4) {
                Text("Unit Preference")
                    .font(.caption)
                    .foregroundColor(theme.onSurfaceVariant)
                Button(action: {
                    showingPicker = true
                }) {
                    HStack {
                        Image(systemName: "ruler")
                            .foregroundColor(theme.primary)
                        Text(currentUnit.capitalized)
                            .foregroundColor(theme.onSurface)
                        Spacer()
                        Image(systemName: "chevron.down")
                            .foregroundColor(theme.onSurfaceVariant)
                    }
                    .padding()
                    .background(theme.surfaceVariant.opacity(0.3))
                    .cornerRadius(8)
                }
                .actionSheet(isPresented: $showingPicker) {
                    ActionSheet(
                        title: Text("Select Unit"),
                        buttons: [
                            .default(Text("Meters")) { onUnitChange("meters") },
                            .default(Text("Feet")) { onUnitChange("feet") },
                            .cancel()
                        ]
                    )
                }
            }
        } else {
            ReadOnlyField(
                label: "Unit Preference",
                value: currentUnit.capitalized,
                icon: "ruler",
                theme: theme
            )
        }
    }
}

struct NotificationToggle: View {
    let enabled: Bool
    let onToggle: (Bool) -> Void
    let isEditing: Bool
    let theme: AppTheme

    var body: some View {
        HStack {
            Image(systemName: "bell.fill")
                .foregroundColor(theme.onSurfaceVariant)
            Text("Notifications")
                .foregroundColor(theme.onSurface)
            Spacer()
            Toggle("", isOn: Binding(
                get: { enabled },
                set: { onToggle($0) }
            ))
            .labelsHidden()
            .disabled(!isEditing)
        }
    }
}

struct ThresholdField: View {
    let label: String
    @Binding var value: Double // non-optional!
    let isEditing: Bool
    let unit: String
    let theme: AppTheme

    var body: some View {
        if isEditing {
            VStack(alignment: .leading, spacing: 4) {
                Text("\(label) (\(unit))")
                    .font(.caption)
                    .foregroundColor(theme.onSurfaceVariant)
                HStack {
                    Image(systemName: "speedometer")
                        .foregroundColor(theme.primary)
                    TextField(label, text: Binding(
                        get: { value == 0.0 ? "" : String(value) },
                        set: { value = Double($0) ?? 0.0 }
                    ))
                    .keyboardType(.decimalPad)
                }
                .padding()
                .background(theme.surfaceVariant.opacity(0.3))
                .cornerRadius(8)
            }
        } else {
            ReadOnlyField(
                label: "\(label) (\(unit))",
                value: value == 0.0 ? "Not set" : String(value),
                icon: "speedometer",
                theme: theme
            )
        }
    }
}


struct ThemeOptionItem: View {
    let appTheme: AppTheme
    let isSelected: Bool
    let name: String
    let theme: AppTheme
    let onSelect: (AppTheme) -> Void

    var body: some View {
        Button(action: { onSelect(appTheme) }) {
            HStack {
                ColorPalettePreview(appTheme: appTheme, theme: theme)
                    .frame(width: 60, height: 30)
                VStack(alignment: .leading) {
                    Text(name)
                        .font(.body)
                        .fontWeight(.medium)
                        .foregroundColor(theme.onSurface)
                    Text("Light & Dark compatible")
                        .font(.caption)
                        .foregroundColor(theme.onSurfaceVariant)
                }
                Spacer()
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(theme.primary)
                }
            }
            .padding()
            .background(theme.surface)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? theme.primary : Color.clear, lineWidth: 2)
            )
        }
    }
}

struct ColorPalettePreview: View {
    let appTheme: AppTheme
    let theme: AppTheme

    var body: some View {
        HStack(spacing: 2) {
            RoundedRectangle(cornerRadius: 4)
                .fill(appTheme.primary)
                .frame(maxWidth: .infinity)
            RoundedRectangle(cornerRadius: 4)
                .fill(appTheme.secondary)
                .frame(maxWidth: .infinity)
            RoundedRectangle(cornerRadius: 4)
                .fill(appTheme.surfaceVariant)
                .frame(maxWidth: .infinity)
            RoundedRectangle(cornerRadius: 4)
                .fill(appTheme.primaryContainer)
                .frame(maxWidth: .infinity)
        }
        .frame(height: 30)
        .clipShape(RoundedRectangle(cornerRadius: 6))
        .overlay(
            RoundedRectangle(cornerRadius: 6)
                .stroke(Color(.systemGray4), lineWidth: 1)
        )
    }
}

// MARK: - Loading & Error Components

struct SettingsLoadingContent: View {
    let theme: AppTheme

    var body: some View {
        VStack {
            Spacer()
            ProgressView()
                .scaleEffect(1.5)
            Text("Loading settings...")
                .font(.headline)
                .foregroundColor(theme.onSurface)
                .padding(.top, 16)
            Spacer()
        }
    }
}

struct SettingsErrorContent: View {
    let errorMessage: String
    let theme: AppTheme

    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 64))
                .foregroundColor(theme.error)
            Text("Settings Error")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(theme.error)
                .multilineTextAlignment(.center)
            Text(errorMessage)
                .font(.body)
                .foregroundColor(theme.onSurfaceVariant)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Spacer()
        }
        .padding()
    }
}

// MARK: - Helper Functions

func formatDate(_ timestamp: Int64) -> String {
    if timestamp == 0 {
        return "Not available"
    } else {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp) / 1000)
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM dd, yyyy 'at' HH:mm"
        return formatter.string(from: date)
    }
}

import Shared

struct EditableUser {
    var id: String
    var name: String
    var email: String
    var imgUrl: String
    var unitPreference: String
    var enabledNotifications: Bool
    var relativeHeightFromStartThr: Double
    var totalHeightFromStartThr: Double
    var currentAvgHeightSpeedThr: Double
    var userUpdatedOffline: Bool
    var createdAt: Int64
    var updatedAt: Int64

    init(user: User) {
        self.id = user.id
        self.name = user.name ?? ""
        self.email = user.email ?? ""
        self.imgUrl = user.imgUrl ?? ""
        self.unitPreference = user.unitPreference ?? "meters"
        self.enabledNotifications = user.enabledNotifications?.boolValue ?? false
        self.relativeHeightFromStartThr = user.relativeHeightFromStartThr?.double ?? 0.0
        self.totalHeightFromStartThr = user.totalHeightFromStartThr?.double ?? 0.0
        self.currentAvgHeightSpeedThr = user.currentAvgHeightSpeedThr?.double ?? 0.0
        self.userUpdatedOffline = user.userUpdatedOffline?.boolValue ?? false
        self.createdAt = user.createdAt?.int64Value ?? 0
        self.updatedAt = user.updatedAt?.int64Value ?? 0
    }

    func toUser() -> User {
        User(
            id: id,
            name: name.isEmpty ? nil : name,
            email: email.isEmpty ? nil : email,
            imgUrl: imgUrl.isEmpty ? nil : imgUrl,
            unitPreference: unitPreference.isEmpty ? nil : unitPreference,
            enabledNotifications: KotlinBoolean(value: enabledNotifications),
            relativeHeightFromStartThr: KotlinDouble(value: relativeHeightFromStartThr),
            totalHeightFromStartThr: KotlinDouble(value: totalHeightFromStartThr),
            currentAvgHeightSpeedThr: KotlinDouble(value: currentAvgHeightSpeedThr),
            userUpdatedOffline: KotlinBoolean(value: userUpdatedOffline),
            createdAt: KotlinLong(value: createdAt),
            updatedAt: KotlinLong(value: updatedAt)
        )
    }
}
