# TopOut 🧗‍♂️

**Kotlin Multiplatform app for climbers. Track your climbing sessions with real-time alerts on height changes, natively on iOS and Android.**

## 🎯 Features

• **Clean Architecture** - Backend business logic shared in Kotlin, native UIs with Jetpack Compose (Android) and SwiftUI (iOS)  
• **Declarative DI** - Koin setup with Singleton & Factory patterns, bridging ViewModels to native layers  
• **Platform Features** - expect/actual structure for sensors and notifications across platforms  
• **Background Tracking** - Sensor data aggregation with Coroutines + WakeLock (Android), Background Tasks (iOS)  
• **Storage** - SQLDelight for offline database + Firestore for remote sync. Anonymous sign-in, no user sign-up required  
• **Offline First** - Full offline mode with conflict resolution. Changes sync seamlessly when connectivity returns  
• **Native UX** - Native UI components, theme design system, and Lottie animations  

## 🏗️ Architecture

```
├── composeApp/     # Android - Jetpack Compose UI
├── iosApp/         # iOS - SwiftUI UI  
└── shared/         # Shared Kotlin business logic
    ├── commonMain/ # Domain, data, DI modules
    ├── androidMain/# Android platform implementations
    └── iosMain/    # iOS platform implementations
```

**Tech Stack:**
- **Shared**: Kotlin Multiplatform, Koin DI, SQLDelight, Ktor, Firebase Firestore, Coroutines
- **Android**: Jetpack Compose, Material 3, Navigation, Maps, Vico Charts, Lottie
- **iOS**: SwiftUI, Core Motion, Background Tasks, Core Location

## 🚀 Quick Start

**Prerequisites:** JDK 17+, Android Studio, Xcode 15+

```bash
git clone <repo-url>
cd TopOut

# Android
./gradlew :composeApp:installDebug

# iOS  
./gradlew :shared:syncFramework
# Then open iosApp.xcodeproj in Xcode
```

## 📱 Key Components

**LiveSessionManager** - Real-time session tracking with sensor fusion  
**expect/actual** - Platform abstractions for sensors, notifications, background tasks  
**Offline Sync** - Conflict resolution with sync flags in SQLDelight schema  
**Anonymous Auth** - Firebase auth without registration, data persists until app deletion  

---

*Built for the climbing community* 🧗‍♀️🧗‍♂️

