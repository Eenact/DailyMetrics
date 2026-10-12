# Backlog

Maintained by the `project-manager` agent. IDs are `DM-<n>`; keep numbering increasing.

## In progress

- **DM-1 Verify iOS build on a Mac** — owner: ios-developer. iOS code (`shared/src/iosMain`, `iosApp/`)
  was written on Windows and has never been compiled. Build in Xcode, fix interop errors, run on a
  simulator, walk `docs/QA_CHECKLIST.md`. Set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig` for devices.

## Planned

- **DM-2 Real AI suggestions** — owner: kmp-developer (+ PM for product decisions). The "AI Suggestions"
  card from the design was not in the original source; it currently shows an on-device statistical
  summary (`InsightsGenerator`). Decide provider, privacy/consent and offline behaviour, then implement
  behind an interface so the card does not change.
- **DM-3 Web target (wasmJs)** — owner: kmp-developer. Add `wasmJs` target, a `webApp` entry, browser
  implementations of `AppSettings` (localStorage), `FileExporter` (download link), and
  `NotificationScheduler` (no-op or Web Notifications). Room 3 supports wasmJs with a web SQLite driver;
  verify persistence (OPFS) before committing to it.
- **DM-4 Records sharing a timestamp** — owner: kmp-developer. `TaskValue.time` is the primary key, so two
  values logged in the same minute (for different tasks too) collide; the second is rejected with a
  message. Fixing it needs a schema migration to a surrogate key.
- **DM-5 Chart interaction** — owner: kmp-developer. The old MPAndroidChart chart supported pinch-zoom and
  drag; the Compose chart is static. Add zoom/pan and tap-to-inspect if users miss it.
- **DM-6 Release pipeline** — owner: android-developer / ios-developer. Signing configs, CI (GitHub Actions:
  Android build + tests on Linux, iOS build on macOS runner), store metadata.
- **DM-7 Higher-resolution iOS app icon** — owner: ios-developer. Current 1024px icon is upscaled from the
  512px Play Store icon.
- **DM-8 Exact alarm policy** — owner: android-developer. `USE_EXACT_ALARM` is restricted by Google Play to
  alarm/calendar apps. Consider `SCHEDULE_EXACT_ALARM` with the settings prompt, or inexact alarms.

- **DM-9 Run the minified release build** — owner: qa-tester. `assembleRelease` (R8) builds, but the
  release APK has not been run yet. Install a signed release build (different app id or a clean device) and
  walk the checklist to catch shrinker issues.

## Done

- **DM-0 KMP migration** — Android XML/View app converted to KMP + Compose Multiplatform (Android & iOS),
  Room 3 with migration path from the shipped v3 database, platform services for notifications, export,
  settings. Verified on Android (Pixel 7 emulator, API 37): 19 common unit tests pass; upgrade from the
  original app keeps tasks, values, colors, reminders and onboarding state; fresh-install onboarding, value
  entry, CSV share and a reminder notification (with automatic re-arming) work. Also fixed from the old app:
  reminders only ever fired once, and new tasks scheduled their reminder with id 0.

## Blocked

_Nothing._
