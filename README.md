# SongLister

An offline-first Android app for browsing, searching, and filtering song libraries for **UltraStar** and **YARG**, backed by JSON catalogs hosted on GitHub Gists.

---

## Features

- **Game Selection Home Screen**: Choose between **UltraStar** (karaoke & vocal tracks) and **YARG** (full-band rhythm charts), with live sync status and cached song counts.
- **Offline-First Local Database**: Song lists are downloaded and indexed into a local **Room** database for instant offline querying across thousands of tracks.
- **Intelligent Startup Sync (`ETag` Caching)**: Syncs both song libraries concurrently on app startup using HTTP conditional requests (`If-None-Match` / `304 Not Modified`), avoiding unnecessary re-downloads when lists haven't changed and falling back seamlessly to cached data when offline.
- **Fast Search**: Debounced real-time search by song title or artist.
- **Game-Specific Filter Bottom Sheet**: Dedicated filter button (with active-filter badge) opening a Modal Bottom Sheet populated dynamically from distinct values in the local database:
  - **UltraStar**: Genre, Language, Year range, Duet, and Solo.
  - **YARG**: Genre, Album substring search, Format (`CON`, `INI`, `SNG`), Year range, and per-instrument toggles with minimum difficulty tiers (`Guitar`, `Bass`, `Drums`, `Keys`, `Vocals`).
- **Song Detail Screen**: Full track metadata, chart source file paths, and visual instrument difficulty breakdowns.
- **Localization**: Full support for **English** and **Spanish** (`values-es`).
- **Adaptive Launcher Icon**: Minimal custom vector launcher icon with Android 13+ Material You monochrome themed-icon support.

---

## Data Sources

- **UltraStar**: [`ultrastar_songs.json`](https://gist.github.com/leandroruiz96/d845362c9a00cba7f32e9216cb210876/raw/ultrastar_songs.json)
- **YARG**: [`yarg_songs.json`](https://gist.github.com/leandroruiz96/8a7b242e82040fd39178184701cf11f9/raw/yarg_songs.json)

---

## Tech Stack & Architecture

- **Language**: Kotlin (`2.2.10`)
- **UI**: Jetpack Compose + Material Design 3 + Navigation Compose (type-safe routes)
- **Architecture**: MVVM + Repository pattern + Manual DI (`AppContainer`)
- **Local Persistence**: Room (`2.8.5`) with KSP (`2.2.10-2.0.2`) and indexed SQLite queries
- **Networking & Serialization**: Retrofit (`3.0.0`), OkHttp (`4.12.0`), Kotlinx Serialization JSON (`1.9.0`)
- **Testing**: JUnit 4, `kotlinx-coroutines-test`, OkHttp `MockWebServer`
- **Target**: `minSdk 24`, `compileSdk / targetSdk 37`

---

## How This Vibe-Coded App Was Built

This project was built end-to-end using an iterative **vibe-coding** workflow:

- **AI Coding Agent**: Built with **Google Antigravity** (agentic AI assistant) — from inspecting the live JSON gist schemas and designing an iterative 6-step roadmap, to implementing each layer, writing unit tests, adding Spanish localization, and verifying UI changes.
- **AI Logo Design**: Minimal launcher icon concepts were brainstormed and generated via Antigravity's image-generation workflow, then hand-crafted into crisp Android Vector Drawables (`ic_launcher_foreground.xml`, `ic_launcher_background.xml`, and `ic_launcher_monochrome.xml`).
- **Android Tooling**: **Android Studio**, **Gradle 9.6** (with AGP 9.5), and **Android Emulator** (`Medium_Phone_API_37.0`) via `adb` for live visual verification of syncing, filtering, detail views, localization, and launcher icons.

---

## Building & Testing

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Build Debug APK
```bash
./gradlew assembleDebug
```
