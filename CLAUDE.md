# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK (combines dev_type + app_type flavors)
./gradlew assembleSoftwarekeyboardLauncherDebug
./gradlew assembleBlackberryLauncherDebug
./gradlew assembleMinimalphoneLauncherDebug

# Build Spotlight variant (overlay app search, not a launcher)
./gradlew assembleSoftwarekeyboardSpotlightDebug

# Build release (requires CLZ_RELEASE_* properties in gradle.properties)
./gradlew assembleSoftwarekeyboardLauncherRelease
./gradlew assembleBlackberryLauncherRelease

# Install on connected device
./gradlew installSoftwarekeyboardLauncherDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

## Build Environment

- **Gradle:** 9.1.0, **AGP:** 9.0.1, **Java:** 17
- **SDK:** compileSdk=36, minSdk=26, targetSdk=36
- **KSP** (Kotlin Symbol Processing) used for Room annotation processing
- Debug builds add `.dev` applicationId suffix
- Release builds require `CLZ_RELEASE_STORE_FILE`, `CLZ_RELEASE_STORE_PASSWORD`, `CLZ_RELEASE_KEY_ALIAS`, `CLZ_RELEASE_KEY_PASSWORD` in `gradle.properties`
- Build config generates `IS_LAUNCHER` and `IS_SPOTLIGHT` boolean flags per app_type flavor

## Architecture Overview

**Void Launcher** is a minimalist Android launcher designed for devices with physical keyboards (BlackBerry, MinimalPhone) and standard touchscreen devices. **Void Spotlight** is a companion overlay app that provides quick app search without being a launcher.

### Product Flavors

Two flavor dimensions combine to create variants:

**`dev_type` dimension** - targets different device types:
- **softwarekeyboard**: Standard Android devices (shows on-screen keyboard)
- **blackberry**: BlackBerry devices with physical keyboards (always returns `deviceHasPhysicalKeyboard()=true`, keyboard layout=-1)
- **minimalphone**: MinimalPhone devices with physical keyboards (always returns `deviceHasPhysicalKeyboard()=true`, keyboard layout=-1)

Each `dev_type` flavor provides its own `KeyboardInfoProvider.kt` with standalone functions:
- `deviceHasPhysicalKeyboard()`: Returns whether to hide the software keyboard
- `provideKeyboardMapper()`: Returns device-specific key code mapper
- `provideCustomKeyManager()`: Returns device-specific custom key handler

**`app_type` dimension** - determines app behavior:
- **launcher**: Full launcher with home screen replacement (`IS_LAUNCHER=true`)
- **spotlight**: Overlay-based app search triggered by accessibility service (`IS_LAUNCHER=false`)

### Source Set Structure

Resources and code are loaded based on variant combination. Available source sets:
- `src/main/` - Shared across all variants
- `src/launcher/`, `src/spotlight/` - App type specific
- `src/blackberry/`, `src/minimalphone/`, `src/softwarekeyboard/` - Device type specific
- `src/softwarekeyboardLauncher/`, `src/blackberryLauncher/`, `src/minimalphoneLauncher/` - Combined flavor source sets (use camelCase: `<devType><AppType>`)

### Core Components

**Shared (in `src/main/`):**
- **AppViewModel**: Central ViewModel managing app list state, filtering, folders, and launching apps
- **VoidDatabase (Room)**: Version 1, stores apps, tags, and folders. Schema exports in `app/schemas/`.
  - Entities: `AppEntity`, `TagEntity`, `FolderEntity`, `FoldersAppsCrossRef`
  - DAOs in `room/dao/` package — `AppEntityDao` uses multi-step CTEs for complex app-folder-tag joins
- **Repositories** (AppRepository, TagRepository, FolderRepository): Data layer with suspend functions wrapping DAOs
- **PackageManagerDao**: Multi-user aware app enumeration via LauncherApps + UserManager (singleton with synchronized companion)

**Launcher flavor (in `src/launcher/`):**
- **BaseMainActivity/MainActivity**: Entry point, handles physical keyboard events via `onKeyDown()`
- **AppListFragment**: Main UI showing app list with filtering (implements `KeyboardView.OnKeyboardActionListener`)
- **Accessible**: Accessibility service for system actions (power menu, notifications, screen off). Screen off: API 28+ uses `GLOBAL_ACTION_LOCK_SCREEN`, pre-28 uses `DevicePolicyManager`.
- **Keyboard.java**: Custom keyboard implementation (Java, Apache-licensed) with XML layout parsing, proximity grid, modifier key state

**Spotlight flavor (in `src/spotlight/`):**
- **OverlayService**: `LifecycleService` implementing `ViewModelStoreOwner` — renders floating overlay via WindowManager with manual LifecycleRegistry management
- **KeyForwarderAccessibility**: Captures key events and forwards to OverlayService
- **SpotlightSetupActivity**: Configuration UI for permissions
- **KeyCombination**: Serializable model for configurable activation key binding

### Dependency Management

- **No DI framework** — manual singleton/factory pattern throughout. VoidDatabase and PackageManagerDao use synchronized companion objects with double-checked locking.
- Dependencies managed via `gradle/libs.versions.toml` version catalog

### Preferences System

Uses SharedPreferences via `PreferenceManager.getDefaultSharedPreferences()` with a custom type-safe `Preference.Info` metadata system.

- **PreferencesViewModel** (`src/main/`): Base preferences (vibrate, auto-launch, voice search language)
- **LauncherPreferencesViewModel** (`src/launcher/`): Extends with keyboard margin, custom actions
- **SpotlightPreferencesViewModel** (`src/spotlight/`): Extends with show-all-on-start, sorting, width, activation key

### Keyboard Input Flow (Launcher)

1. Physical key events captured in `MainActivity.onKeyDown()`
2. Key codes mapped via flavor-specific `KeyboardMapper`
3. Events dispatched to fragments implementing `KeyboardView.OnKeyboardActionListener`
4. `AppListFragment.onKey()` handles character input for filtering, special keys for actions

### UI Data Flow

- **AppsAdapter**: RecyclerView `ListAdapter` with DiffUtil, renders polymorphic `MainListUiItem` types (`AppUiItem`, `FolderUiItem`)
- ViewModels use `viewModelScope.launch(Dispatchers.IO)` for database operations
- LiveData with MediatorLiveData for reactive filtering
- **AppListChangeReceiver**: BroadcastReceiver for `ACTION_PACKAGE_ADDED`/`ACTION_PACKAGE_REMOVED`, triggers async app sync via AppRepository
- Speech recognition via `SpeechRecognizerManager` (weak reference singleton, partial results, configurable language)
