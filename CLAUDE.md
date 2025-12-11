# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Bird Launcher is a minimalist command-line style Android launcher built with Kotlin and Jetpack Compose. It allows users to launch apps by typing their names in a terminal-like interface, search the web, and customize the launcher appearance.

## Build Commands

### Build Debug APK
```bash
./gradlew assembleDebug
```
On Windows:
```bash
gradlew.bat assembleDebug
```

### Build Release APK
```bash
./gradlew assembleRelease
```
Release builds use ProGuard for code minification and resource shrinking.

### Run Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Install on Device
```bash
./gradlew installDebug
```

## Architecture

The project follows **Clean Architecture** principles with clear separation of concerns:

### Layer Structure

**Domain Layer** (`domain/`)
- **Models**: `AppItem`, `LauncherSettings` - Core data structures with no Android dependencies
- **Use Cases**: Business logic implementations
  - `SearchAppsUseCase` - Searches installed apps by query
  - `GetInstalledAppsUseCase` - Retrieves all installed apps with pinned status
  - `ManagePinnedAppsUseCase` - Handles pinning/unpinning apps
  - `LauncherSettingsUseCase` - Manages launcher configuration

**Data Layer** (`data/`)
- **Repositories**: Data access abstractions
  - `AppsRepository` - Interfaces with PackageManager to fetch installed apps
  - `LauncherPreferencesRepository` - Manages SharedPreferences for settings and pinned apps
- `AppIconFetcher` - Handles app icon loading with Coil

**Presentation Layer** (root package)
- **ViewModels**:
  - `TerminalViewModel` - Manages terminal state, command processing, and settings
  - `AppsListViewModel` - Handles apps list, pinning, and loading states
- **Composables**:
  - `TerminalScreen` - Main terminal UI with command input
  - `AppsListBottomSheet` - Bottom sheet displaying all apps with icons
  - `SettingsBottomSheet` - Settings UI for customization

### Key Architectural Patterns

**MVVM with Use Cases**: ViewModels depend on use cases (not repositories directly), enforcing business logic separation.

**Repository Pattern**: Data layer abstracts data sources (PackageManager, SharedPreferences).

**State Flow**: Reactive state management using Kotlin Flow for settings and app lists.

**Event Pattern**: One-time events (like launching intents) use `Channel` rather than StateFlow to avoid re-emission on configuration changes.

## Terminal Command Processing

Commands are processed in `TerminalViewModel.processInput()`:
- `clear` - Clears terminal output
- `web <query>` - Opens Google search with query
- `<app name>` - Searches and launches matching app(s)

Search is case-insensitive and uses substring matching. When multiple apps match, all are displayed.

## Launcher Configuration

Settings are stored in SharedPreferences and exposed via Flow:
- Prompt customization (username, hostname, arrow)
- Visual effects (blur radius, overlay alpha)
- Display toggles (date, username, hostname, etc.)

Pinned apps are stored as a StringSet in SharedPreferences and refreshed on every app list load.

## Image Loading

Uses **Coil** (v2.5.0) for async app icon loading in Compose. Icons are fetched via `AppIconFetcher` which loads drawables from PackageManager.

## Manifest Configuration

The app declares itself as a HOME category launcher in AndroidManifest.xml, allowing it to be set as the default launcher. It requires `QUERY_ALL_PACKAGES` permission to access installed apps.

## Development Notes

- **Minimum SDK**: 35 (Android 14)
- **Target SDK**: 36
- **Compose Compiler**: 1.5.3
- **Material 3**: Full Material You theming support
- Theme uses dynamic colors from wallpaper via `Theme.kt`
