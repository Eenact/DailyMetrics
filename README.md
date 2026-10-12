# Daily Metrics

Track day-to-day activities (yes/no or numeric), see them on charts, export CSV and get reminders.
Kotlin Multiplatform + Compose Multiplatform: one codebase for **Android** and **iOS** (web planned).

| Module | Contents |
|---|---|
| `shared` | All business logic, Room database, view models and the Compose UI |
| `androidApp` | Android entry point (`MainActivity`, manifest, icons) |
| `iosApp` | Xcode project hosting the shared UI |

## Run

- **Android:** open the project in Android Studio and run `androidApp`, or `./gradlew :androidApp:installDebug`.
- **iOS (macOS):** open `iosApp/iosApp.xcodeproj` in Xcode, set `TEAM_ID` in
  `iosApp/Configuration/Config.xcconfig` for a device, and run. The first build compiles the
  Kotlin framework through Gradle.
- **Tests:** `./gradlew :shared:testAndroidHostTest`

See [CLAUDE.md](CLAUDE.md) for architecture and rules, and [docs/BACKLOG.md](docs/BACKLOG.md) for
planned work.
