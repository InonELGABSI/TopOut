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

    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue

    private var colors: TopOutColorScheme {
        (ThemePalette(rawValue: selectedTheme) ?? .classicRed).scheme(for: colorScheme)
    }

    var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            switch onEnum(of: viewModel.uiState) {
            case .loading:
                SettingsLoadingContent(colors: colors)
            case .loaded(let state):
                ScrollView {
                    VStack(spacing: 0) {
                        Spacer().frame(height: 20)

                        ProfileCard(
                            user: state.user,
                            isEditing: isEditingProfile,
                            onToggleEdit: { isEditingProfile = $0 },
                            onUpdateUser: { user in viewModel.viewModel.updateUser(user: user) },
                            colors: colors
                        )
                        .zIndex(4)
                        .padding(.horizontal)

                        PreferencesCard(
                            user: state.user,
                            isEditing: isEditingPreferences,
                            onToggleEdit: { isEditingPreferences = $0 },
                            onUpdateUser: { user in viewModel.viewModel.updateUser(user: user) },
                            colors: colors
                        )
                        .zIndex(3)
                        .padding(.horizontal)
                        .offset(y: -20)

                        ThresholdsCard(
                            user: state.user,
                            isEditing: isEditingThresholds,
                            onToggleEdit: { isEditingThresholds = $0 },
                            onUpdateUser: { user in viewModel.viewModel.updateUser(user: user) },
                            colors: colors
                        )
                        .zIndex(2)
                        .padding(.horizontal)
                        .offset(y: -40)

                        ThemeCard(colors: colors)
                            .zIndex(1)
                            .padding(.horizontal)
                            .offset(y: -60)

                        Spacer().frame(height: 80)
                    }
                }
            case .error(let state):
                SettingsErrorContent(errorMessage: state.errorMessage, colors: colors)
            }
        }
        .navigationTitle("Settings")
        .onAppear { viewModel.startObserving() }
    }
}

// MARK: - Card Components

struct ProfileCard: View {
    let user: User
    let isEditing: Bool
    let onToggleEdit: (Bool) -> Void
    let onUpdateUser: (User) -> Void
    let colors: TopOutColorScheme

    @State private var editableUser: EditableUser

    init(user: User, isEditing: Bool, onToggleEdit: @escaping (Bool) -> Void, onUpdateUser: @escaping (User) -> Void, colors: TopOutColorScheme) {
        self.user = user
        self.isEditing = isEditing
        self.onToggleEdit = onToggleEdit
        self.onUpdateUser = onUpdateUser
        self.colors = colors
        _editableUser = State(initialValue: EditableUser(user: user))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            ZStack {
                HStack(spacing: 8) {
                    Image(systemName: "person.fill")
                        .foregroundColor(colors.primary)
                    Text("Profile")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(colors.onSurface)
                }
                if !isEditing {
                    HStack {
                        Spacer()
                        Button(action: {
                            // Reset before editing!
                            editableUser = EditableUser(user: user)
                            onToggleEdit(true)
                        }) {
                            Circle()
                                .fill(colors.primary)
                                .frame(width: 40, height: 40)
                                .overlay(
                                    Image(systemName: "pencil")
                                        .foregroundColor(colors.onPrimary)
                                )
                        }
                    }
                }
            }
            Group {
                EditableField(
                    label: "Name",
                    value: $editableUser.name,
                    isEditing: isEditing,
                    icon: "person.fill",
                    colors: colors
                )
                EditableField(
                    label: "Email",
                    value: $editableUser.email,
                    isEditing: isEditing,
                    icon: "envelope.fill",
                    keyboardType: .emailAddress,
                    colors: colors
                )
                EditableField(
                    label: "Image URL",
                    value: $editableUser.imgUrl,
                    isEditing: isEditing,
                    icon: "photo.fill",
                    colors: colors
                )
                ReadOnlyField(
                    label: "User ID",
                    value: editableUser.id,
                    icon: "key.fill",
                    colors: colors
                )
                ReadOnlyField(
                    label: "Created At",
                    value: formatDate(editableUser.createdAt),
                    icon: "calendar",
                    colors: colors
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
                            .background(colors.surfaceVariant)
                            .foregroundColor(colors.onSurfaceVariant)
                            .cornerRadius(8)
                    }
                    Button(action: {
                        onUpdateUser(editableUser.toUser())
                        onToggleEdit(false)
                    }) {
                        Text("Save")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(colors.primary)
                            .foregroundColor(colors.onPrimary)
                            .cornerRadius(8)
                    }
                }
            }
        }
        .padding(20)
        .background(colors.surface)
        .cornerRadius(16)
    }
}


struct PreferencesCard: View {
    let user: User
    let isEditing: Bool
    let onToggleEdit: (Bool) -> Void
    let onUpdateUser: (User) -> Void
    let colors: TopOutColorScheme

    @State private var editableUser: EditableUser

    init(user: User, isEditing: Bool, onToggleEdit: @escaping (Bool) -> Void, onUpdateUser: @escaping (User) -> Void, colors: TopOutColorScheme) {
        self.user = user
        self.isEditing = isEditing
        self.onToggleEdit = onToggleEdit
        self.onUpdateUser = onUpdateUser
        self.colors = colors
        _editableUser = State(initialValue: EditableUser(user: user))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            ZStack {
                HStack(spacing: 8) {
                    Image(systemName: "gearshape.fill")
                        .foregroundColor(colors.primary)
                    Text("Preferences")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(colors.onSurface)
                }
                if !isEditing {
                    HStack {
                        Spacer()
                        Button(action: {
                            editableUser = EditableUser(user: user)
                            onToggleEdit(true)
                        }) {
                            Circle()
                                .fill(colors.primary)
                                .frame(width: 40, height: 40)
                                .overlay(
                                    Image(systemName: "pencil")
                                        .foregroundColor(colors.onPrimary)
                                )
                        }
                    }
                }
            }
            UnitPreferenceSelector(
                currentUnit: editableUser.unitPreference,
                onUnitChange: { unit in editableUser.unitPreference = unit },
                isEditing: isEditing,
                colors: colors
            )
            NotificationToggle(
                enabled: editableUser.enabledNotifications,
                onToggle: { enabled in editableUser.enabledNotifications = enabled },
                isEditing: isEditing,
                colors: colors
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
                            .background(colors.surfaceVariant)
                            .foregroundColor(colors.onSurfaceVariant)
                            .cornerRadius(8)
                    }
                    Button(action: {
                        onUpdateUser(editableUser.toUser())
                        onToggleEdit(false)
                    }) {
                        Text("Save")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(colors.primary)
                            .foregroundColor(colors.onPrimary)
                            .cornerRadius(8)
                    }
                }
            }
        }
        .padding(20)
        .background(colors.surfaceContainer)
        .cornerRadius(16)
    }
}

struct ThresholdsCard: View {
    let user: User
    let isEditing: Bool
    let onToggleEdit: (Bool) -> Void
    let onUpdateUser: (User) -> Void
    let colors: TopOutColorScheme

    @State private var editableUser: EditableUser

    init(user: User, isEditing: Bool, onToggleEdit: @escaping (Bool) -> Void, onUpdateUser: @escaping (User) -> Void, colors: TopOutColorScheme) {
        self.user = user
        self.isEditing = isEditing
        self.onToggleEdit = onToggleEdit
        self.onUpdateUser = onUpdateUser
        self.colors = colors
        _editableUser = State(initialValue: EditableUser(user: user))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            ZStack {
                HStack(spacing: 8) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .foregroundColor(colors.primary)
                    Text("Alert Thresholds")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(colors.onSurface)
                }
                if !isEditing {
                    HStack {
                        Spacer()
                        Button(action: {
                            editableUser = EditableUser(user: user)
                            onToggleEdit(true)
                        }) {
                            Circle()
                                .fill(colors.primary)
                                .frame(width: 40, height: 40)
                                .overlay(
                                    Image(systemName: "pencil")
                                        .foregroundColor(colors.onPrimary)
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
                colors: colors
            )
            ThresholdField(
                label: "Total Height Threshold",
                value: $editableUser.totalHeightFromStartThr,
                isEditing: isEditing,
                unit: editableUser.unitPreference,
                colors: colors
            )
            ThresholdField(
                label: "Average Speed Threshold",
                value: $editableUser.currentAvgHeightSpeedThr,
                isEditing: isEditing,
                unit: "\(editableUser.unitPreference)/min",
                colors: colors
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
                            .background(colors.surfaceVariant)
                            .foregroundColor(colors.onSurfaceVariant)
                            .cornerRadius(8)
                    }
                    Button(action: {
                        onUpdateUser(editableUser.toUser())
                        onToggleEdit(false)
                    }) {
                        Text("Save")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(colors.primary)
                            .foregroundColor(colors.onPrimary)
                            .cornerRadius(8)
                    }
                }
            }
        }
        .padding(20)
        .background(colors.surface)
        .cornerRadius(16)
    }
}


struct ThemeCard: View {
    @AppStorage("selectedTheme") private var selectedTheme: String = ThemePalette.classicRed.rawValue
    @Environment(\.colorScheme) private var colorScheme
    @State private var isDarkMode: Bool = false

    let colors: TopOutColorScheme

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                HStack(spacing: 8) {
                    Image(systemName: "paintpalette.fill")
                        .foregroundColor(colors.primary)
                    Text("Theme")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(colors.onSurface)
                }
                Spacer()
                // Optional: Toggle for dark mode (if you want)
            }
            Text("Color Palette")
                .font(.subheadline)
                .fontWeight(.medium)
                .foregroundColor(colors.onSurfaceVariant)

            VStack(spacing: 12) {
                ForEach(ThemePalette.allCases, id: \.self) { palette in
                    ThemeOptionItem(
                        themePalette: palette,
                        isSelected: selectedTheme == palette.rawValue,
                        name: palette.displayName,
                        colors: colors,
                        onSelect: { selected in
                            selectedTheme = selected.rawValue
                        }
                    )
                }
            }
        }
        .padding(20)
        .background(colors.surface)
        .cornerRadius(16, corners: [.bottomLeft, .bottomRight])
    }
}

// MARK: - Field Components

struct EditableField: View {
    let label: String
    @Binding var value: String  // Non-optional!
    let isEditing: Bool
    let icon: String
    var keyboardType: UIKeyboardType = .default
    let colors: TopOutColorScheme

    var body: some View {
        if isEditing {
            VStack(alignment: .leading, spacing: 4) {
                Text(label)
                    .font(.caption)
                    .foregroundColor(colors.onSurfaceVariant)
                HStack {
                    Image(systemName: icon)
                        .foregroundColor(colors.primary)
                    TextField(label, text: $value)
                        .keyboardType(keyboardType)
                }
                .padding()
                .background(colors.surfaceVariant.opacity(0.3))
                .cornerRadius(8)
            }
        } else {
            ReadOnlyField(
                label: label,
                value: value.isEmpty ? "Not set" : value,
                icon: icon,
                colors: colors
            )
        }
    }
}


struct ReadOnlyField: View {
    let label: String
    let value: String
    let icon: String
    let colors: TopOutColorScheme

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .foregroundColor(colors.onSurfaceVariant)
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.caption)
                    .foregroundColor(colors.onSurfaceVariant)
                Text(value)
                    .font(.body)
                    .foregroundColor(colors.onSurface)
            }
        }
    }
}

struct UnitPreferenceSelector: View {
    let currentUnit: String
    let onUnitChange: (String) -> Void
    let isEditing: Bool
    let colors: TopOutColorScheme
    @State private var showingPicker = false

    var body: some View {
        if isEditing {
            VStack(alignment: .leading, spacing: 4) {
                Text("Unit Preference")
                    .font(.caption)
                    .foregroundColor(colors.onSurfaceVariant)
                Button(action: {
                    showingPicker = true
                }) {
                    HStack {
                        Image(systemName: "ruler")
                            .foregroundColor(colors.primary)
                        Text(currentUnit.capitalized)
                            .foregroundColor(colors.onSurface)
                        Spacer()
                        Image(systemName: "chevron.down")
                            .foregroundColor(colors.onSurfaceVariant)
                    }
                    .padding()
                    .background(colors.surfaceVariant.opacity(0.3))
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
                colors: colors
            )
        }
    }
}

struct NotificationToggle: View {
    let enabled: Bool
    let onToggle: (Bool) -> Void
    let isEditing: Bool
    let colors: TopOutColorScheme

    var body: some View {
        HStack {
            Image(systemName: "bell.fill")
                .foregroundColor(colors.onSurfaceVariant)
            Text("Notifications")
                .foregroundColor(colors.onSurface)
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
    let colors: TopOutColorScheme

    var body: some View {
        if isEditing {
            VStack(alignment: .leading, spacing: 4) {
                Text("\(label) (\(unit))")
                    .font(.caption)
                    .foregroundColor(colors.onSurfaceVariant)
                HStack {
                    Image(systemName: "speedometer")
                        .foregroundColor(colors.primary)
                    TextField(label, text: Binding(
                        get: { value == 0.0 ? "" : String(value) },
                        set: { value = Double($0) ?? 0.0 }
                    ))
                    .keyboardType(.decimalPad)
                }
                .padding()
                .background(colors.surfaceVariant.opacity(0.3))
                .cornerRadius(8)
            }
        } else {
            ReadOnlyField(
                label: "\(label) (\(unit))",
                value: value == 0.0 ? "Not set" : String(value),
                icon: "speedometer",
                colors: colors
            )
        }
    }
}


struct ThemeOptionItem: View {
    let themePalette: ThemePalette
    let isSelected: Bool
    let name: String
    let colors: TopOutColorScheme
    let onSelect: (ThemePalette) -> Void

    var body: some View {
        Button(action: { onSelect(themePalette) }) {
            HStack {
                ColorPalettePreview(palette: themePalette, colors: colors)
                    .frame(width: 60, height: 30)
                VStack(alignment: .leading) {
                    Text(name)
                        .font(.body)
                        .fontWeight(.medium)
                        .foregroundColor(colors.onSurface)
                    Text("Light & Dark compatible")
                        .font(.caption)
                        .foregroundColor(colors.onSurfaceVariant)
                }
                Spacer()
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(colors.primary)
                }
            }
            .padding()
            .background(colors.surface)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? colors.primary : Color.clear, lineWidth: 2)
            )
        }
    }
}

struct ColorPalettePreview: View {
    let palette: ThemePalette
    let colors: TopOutColorScheme

    var body: some View {
        HStack(spacing: 2) {
            RoundedRectangle(cornerRadius: 4)
                .fill(colors.primary)
                .frame(maxWidth: .infinity)
            RoundedRectangle(cornerRadius: 4)
                .fill(colors.secondary)
                .frame(maxWidth: .infinity)
            RoundedRectangle(cornerRadius: 4)
                .fill(colors.surfaceVariant)
                .frame(maxWidth: .infinity)
            RoundedRectangle(cornerRadius: 4)
                .fill(colors.primaryContainer)
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
    let colors: TopOutColorScheme

    var body: some View {
        VStack {
            Spacer()
            ProgressView()
                .scaleEffect(1.5)
            Text("Loading settings...")
                .font(.headline)
                .foregroundColor(colors.onSurface)
                .padding(.top, 16)
            Spacer()
        }
    }
}

struct SettingsErrorContent: View {
    let errorMessage: String
    let colors: TopOutColorScheme

    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 64))
                .foregroundColor(colors.error)
            Text("Settings Error")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(colors.error)
                .multilineTextAlignment(.center)
            Text(errorMessage)
                .font(.body)
                .foregroundColor(colors.onSurfaceVariant)
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
        self.relativeHeightFromStartThr = user.relativeHeightFromStartThr?.doubleValue ?? 0.0
        self.totalHeightFromStartThr = user.totalHeightFromStartThr?.doubleValue ?? 0.0
        self.currentAvgHeightSpeedThr = user.currentAvgHeightSpeedThr?.doubleValue ?? 0.0
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

