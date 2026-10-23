# Daily Metrics

Track the things you do every day, as a simple yes/no or as a number with a unit. Each entry gets a
date, a time and an optional comment. Daily Metrics shows your history on a chart and in a list, lets
you filter it, exports it to CSV and reminds you to log.

**Try it in your device: [dailymetrics-34f42.web.app](https://dailymetrics-34f42.web.app/)**

## Features

- **Tasks:** create Yes/No or Number tasks (with a unit such as km, glasses or minutes), each with its own colour
- **Logging:** record a value with date, time and comment
- **Charts and history:** see each task on a line chart and in a list, and filter by task
- **Insights:** an on-device summary of your trends
- **Reminders:** a notification for each task on the weekdays you choose
- **CSV export:** share or save your data
- **Private by design:** all data stays on your device, with no account and no server

## Tech stack

Kotlin Multiplatform with Compose Multiplatform, so one codebase drives the logic and the UI on every platform.

- UI: Compose Multiplatform, Material 3, Navigation 3
- Storage: Room (KMP) with SQLite
- Dates: kotlinx-datetime
- Tests: Kotest

| Module | Contents |
|---|---|
| `shared` | Business logic, Room database, view models and the Compose UI |
| `chart` | Reusable line-chart composable |
| `androidApp` | Android entry point (`MainActivity`, manifest, icons) |
| `iosApp` | Xcode project hosting the shared UI |

## Getting started

Requirements: JDK 17+, and Android Studio (for Android). For iOS you also need macOS with Xcode.

```bash
git clone https://github.com/Eenact/DailyMetrics.git
cd DailyMetrics
```

- **Android:** open the project in Android Studio and run `androidApp`, or run
  `./gradlew :androidApp:installDebug` with a device or emulator connected.
- **iOS (macOS only):** open `iosApp/iosApp.xcodeproj` in Xcode and run. To run on a physical
  device, set your own Apple developer team in `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`.
  The first build compiles the Kotlin framework through Gradle.
- **Tests:**

  ```bash
  ./gradlew :shared:testAndroidHostTest :chart:testAndroidHostTest
  ```

## Contributing

Issues and pull requests are welcome. [CLAUDE.md](CLAUDE.md) describes the architecture and the
project rules (common code first, keep existing users' data safe, portable dependencies), and
[docs/BACKLOG.md](docs/BACKLOG.md) lists the planned work.

Release signing keys, store credentials and Firebase config files are not part of this repository.
Use your own when you build a release.

## License

Licensed under the [Apache License 2.0](LICENSE).
