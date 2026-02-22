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

## Architecture Overview

**Void Launcher** is a minimalist Android launcher designed for devices with physical keyboards (BlackBerry, MinimalPhone) and standard touchscreen devices. **Void Spotlight** is a companion overlay app that provides quick app search without being a launcher.

### Product Flavors

Two flavor dimensions combine to create variants:

**`dev_type` dimension** - targets different device types:
- **softwarekeyboard**: Standard Android devices (shows on-screen keyboard)
- **blackberry**: BlackBerry devices with physical keyboards
- **minimalphone**: MinimalPhone devices with physical keyboards

Each `dev_type` flavor provides its own `KeyboardInfoProvider.kt` that configures:
- `HAS_PHYSICAL_KEYBOARD`: Whether to hide the on-screen keyboard
- `provideKeyboardMapper()`: Maps physical key codes to standard Android key codes
- `provideCustomKeyManager()`: Handles device-specific custom key bindings

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
- **VoidDatabase (Room)**: Stores apps, tags, and folders
  - Entities: `AppEntity`, `TagEntity`, `FolderEntity`, `FoldersAppsCrossRef`
  - DAOs in `room/dao/` package

**Launcher flavor (in `src/launcher/`):**
- **BaseMainActivity/MainActivity**: Entry point, handles physical keyboard events via `onKeyDown()`
- **AppListFragment**: Main UI showing app list with filtering (implements `KeyboardView.OnKeyboardActionListener`)
- **Accessible**: Accessibility service for system actions (power menu, notifications, screen off)

**Spotlight flavor (in `src/spotlight/`):**
- **OverlayService**: Floating overlay window showing app search
- **KeyForwarderAccessibility**: Forwards key events to overlay service
- **SpotlightSetupActivity**: Configuration UI for permissions

### Keyboard Input Flow (Launcher)

1. Physical key events captured in `MainActivity.onKeyDown()`
2. Key codes mapped via flavor-specific `KeyboardMapper`
3. Events dispatched to fragments implementing `KeyboardView.OnKeyboardActionListener`
4. `AppListFragment.onKey()` handles character input for filtering, special keys for actions

### Key Patterns

- ViewModels use `viewModelScope.launch(Dispatchers.IO)` for database operations
- LiveData with MediatorLiveData for reactive filtering
- Speech recognition via `SpeechRecognizerManager` for voice search