# DailyMetrics

Habit / metrics tracker. Users define tasks (Yes/No or Number with a unit), log values with a date,
time and comment, see them on a chart and a list, filter, export CSV, and get reminders.
Originally an Android View/XML app (still on the `main` branch); now Kotlin Multiplatform with
Compose Multiplatform UI for **Android and iOS**. **Web (wasmJs) is the next planned target.**

## Layout

```
shared/        KMP module: ALL logic and ALL UI
  src/commonMain/kotlin/com/eenact/dailymetrics/
    App.kt                 root composable + Route back stack (Navigation 3)
    di/AppContainer.kt     manual DI container, provided via LocalAppContainer
    domain/                pure logic: models, ChartDataBuilder, CsvExporter, ReminderSchedule,
                           InsightsGenerator, DateTimeFormatting
    data/                  TaskRepository (+ Room impl, entity<->domain mappers)
    data/db/               Room 3 entities, DAOs, AppDatabase, migrations
    platform/              interfaces for OS services (PlatformServices.kt) + tiny expect funs
    ui/                    Compose screens, view models, components, theme
  src/commonMain/composeResources/   strings, vector icons, Lottie files
  src/androidMain/         Android implementations, receivers, Android manifest entries
  src/iosMain/             iOS implementations, MainViewController()
  src/commonTest/          unit tests (run on the JVM host)
  schemas/                 exported Room schemas — commit every new version
chart/         KMP library: reusable LineChart composable + ChartData models (no app/DB knowledge;
               shared's ChartDataBuilder maps TaskRecords to ChartData)
androidApp/    thin Android app: MainActivity, app manifest, FileProvider, icons
iosApp/        Xcode project: SwiftUI shell hosting MainViewController()
```

## Rules

- **Common first.** Logic, view models, UI and strings live in `commonMain`. Platform code only for
  OS APIs, behind the interfaces in `platform/PlatformServices.kt` (`AppSettings`,
  `NotificationScheduler`, `FileExporter`, `AppInfo`). Keep `expect`/`actual` to small UI facts.
- **Existing users must keep their data.** Never change: Android `applicationId`
  (`com.eenact.dailymetrics`), `DATABASE_NAME` (`task_database`), table/column names, enum
  constant names (stored by name), the `notification_days` format (java.util.Calendar day numbers,
  Sunday = 1), or the onboarding preference (`stspref` / `STS_ID_APP_ONBOARDED`). Schema changes need
  a version bump, a `Migration`, the exported schema JSON, and an upgrade test.
- **Stay portable.** No `java.*`/Android APIs or JVM-only stdlib in `commonMain`; new common
  dependencies should support iOS and preferably wasmJs.
- Inject `Clock`/`TimeZone` into logic; format dates through `DateTimeFormatting`.
- User-visible text goes in `composeResources/values/strings.xml`.
- **Tests use Kotest** (specs + `kotest-assertions-core` matchers, `withData` for tables), not plain
  kotlin.test. A module with tests applies the `ksp` + `kotest` plugins, adds `kotest-framework-engine`
  and `kotest-assertions-core` to `commonTest`, `kotest-runner-junit5` to `androidHostTest`, and runs
  `Test` tasks with `useJUnitPlatform()` — see `chart/build.gradle.kts`. When a JVM/host test needs a
  real external service, use Testcontainers through Kotest's extension (JVM-only, not `commonTest`).

## Commands

```bash
./gradlew :shared:testAndroidHostTest      # common unit tests
./gradlew :chart:testAndroidHostTest       # chart module unit tests
./gradlew :androidApp:assembleDebug        # Android debug APK
./gradlew :androidApp:installDebug         # install on a running emulator/device
```

iOS (macOS only): open `iosApp/iosApp.xcodeproj` in Xcode and run, or
`./gradlew :shared:compileKotlinIosSimulatorArm64`. iOS targets cannot be compiled on Windows,
so iOS changes made there are unverified until built on a Mac.

## Team

Agents live in `.claude/agents/` (project-manager, kmp-developer, android-developer,
ios-developer, qa-tester, code-reviewer). Use the `/team <goal>` skill to run the
plan → build → test → review loop. Work items are tracked in `docs/BACKLOG.md`; manual test steps
are in `docs/QA_CHECKLIST.md`.
