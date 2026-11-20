# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

The project has three Gradle modules: `:launcher` and `:spotlight` (apps) and `:core` (Android library). Each app has one flavor dimension, `dev_type`, so task names are `:<app>:<verb><DevType><BuildType>` (e.g. `:launcher:assembleSoftwarekeyboardDebug`).

```bash
# Launcher variants
./gradlew :launcher:assembleSoftwarekeyboardDebug
./gradlew :launcher:assembleBlackberryDebug
./gradlew :launcher:assembleMinimalphoneDebug

# Spotlight variant (overlay app search, not a launcher)
./gradlew :spotlight:assembleSoftwarekeyboardDebug

# Everything (all 6 debug variants)
./gradlew assembleDebug

# Install on connected device
./gradlew :launcher:installSoftwarekeyboardDebug

# Release builds need CLZ_RELEASE_STORE_FILE / _STORE_PASSWORD / _KEY_ALIAS / _KEY_PASSWORD as Gradle properties
./gradlew :launcher:assembleSoftwarekeyboardRelease
```

There are no unit or instrumented tests yet. To check that a change compiles everywhere, run `./gradlew assembleDebug`. A change in `:core` affects both apps. Also run `assembleRelease` from time to time: only release builds run `:core:verifyReleaseResources`, which fails if a core resource references something defined only in an app (e.g. `@string/app_name`).

Application IDs: `dev.coletz.voidlauncher` (launcher) and `dev.coletz.voidlauncher.spotlight` (spotlight), with `.dev` added for debug builds. All four combinations can be installed side by side.

## Architecture Overview

**Void Launcher** is a minimalist, keyboard-driven Android launcher for devices with physical keyboards (BlackBerry, MinimalPhone) and for standard touchscreen devices. **Void Spotlight** is a companion app built from the same codebase. It is not a launcher: it shows an app-search overlay that an accessibility service triggers with a key combination. Open work items are in `TODO.md`.

### Gradle setup

- Shared build config lives in the `build-logic` included build as two convention plugins: `voidlauncher.android.application` (SDK levels, signing, build types, the `dev_type` flavors `softwarekeyboard`/`blackberry`/`minimalphone`) and `voidlauncher.android.library`. SDK levels and the flavor list are in `build-logic/convention/src/main/kotlin/VoidBuildConstants.kt`.
- Each app module sets only its `namespace`, `applicationId` and version (`versionCode`/`versionName` are per app).
- `:core` has no flavors. It exposes the AndroidX/Room/coroutines dependencies to the apps as `api`.

### Modules: where code lives

- `core/src/main/`: shared code (ViewModels, Room, repositories, adapters, dialogs) and shared resources. Namespace `dev.coletz.voidlauncher.core`, so core code imports `dev.coletz.voidlauncher.core.R`. Kotlin packages are still `dev.coletz.voidlauncher.*`. Core must not reference app-only resources, and its manifest uses fully qualified class names.
- `launcher/src/main/` and `spotlight/src/main/`: app code, manifests (including the `<application>` attributes: icon, label, theme), layouts. Both apps use namespace `dev.coletz.voidlauncher`. Because `android.nonTransitiveRClass=false`, the app `R` also contains core resources.
- `launcher/src/<devType>/` (e.g. `launcher/src/blackberry/`): **all device-specific Kotlin code.** Each one provides `keyboard/KeyboardInfoProvider.kt` with top-level functions `deviceHasPhysicalKeyboard(context)`, `provideKeyboardMapper()`, `provideCustomKeyManager()`, and the `SOFTWARE_KEYBOARD_LAYOUT` constant, along with that device's `KeyboardMapper`/`CustomKeyManager` implementations.
- `spotlight/src/<devType>/`: resources only. Right now only `minimalphone` exists; it overrides colors and themes. The same overrides are duplicated in `launcher/src/minimalphone/res/`, so change both copies.

App resources override `:core` resources of the same name. For example, spotlight overrides `BaseAppTheme` with a DayNight parent.

Consequence: keyboard mapping and custom keys exist **only in `:launcher`**. `:spotlight` and `:core` must not depend on `KeyboardInfoProvider` or the `KeyboardMapper`/`CustomKeyManager` abstractions, which are defined in `launcher/src/main/`. If you add a new device-specific function, add it to every `launcher/src/<devType>` source set.

`launcher/src/main/.../keyboard/Keyboard.java` and `KeyboardView.java` are vendored copies of the deprecated Android `inputmethodservice` keyboard classes. They draw the on-screen keyboard in `AppListFragment`.

### Data Flow

- `PackageManagerDao` gets the launchable activities from `LauncherApps`. `AppRepository.updateApps()` syncs them into Room: it deletes uninstalled apps, inserts new ones, and refreshes official names. User edits such as renamed apps, hidden apps, favorites, tags and folders are kept only in Room.
- `AppListChangeReceiver` re-syncs when packages are added or removed.
- `AppViewModel` (shared) exposes `apps` (LiveData) and filters it with `filter` (MutableLiveData). Both apps and folders are shown as `MainListUiItem` (`AppUiItem` / `FolderUiItem`) in `AppsAdapter`.
- `VoidDatabase` (Room, entities `AppEntity`, `TagEntity`, `FolderEntity`, `FoldersAppsCrossRef`) lives in `:core` and exports its schemas to `core/schemas/` through KSP. The current DB is `dev.coletz.voidlauncher.room.VoidDatabase` version 1. `com.coletz...` is a schema from the old package name. If you change an entity, bump the version and add a migration.

### Preferences

Preferences are stored as **strings** in the default `SharedPreferences`, even booleans and ints. Each one is declared as a `Preference.Info` (`bool`/`int`/`enum`/`keyCombination`). `PreferencesViewModel` (core) defines the shared preferences. `LauncherPreferencesViewModel` and `SpotlightPreferencesViewModel` subclass it and override `getAllPreferenceDefinitions()` to add app-specific ones; for example, the launcher creates one `CUSTOM_ACTION_*` pref per custom key that `CustomKeyManager` provides. `PreferenceManagerDialog` builds the settings UI generically from these definitions. To add a setting, declare an `Info`, add it to `getAll()`, and add a typed getter.

### Launcher Flow

1. `MainActivity.onKeyDown()` captures physical key events.
2. Key codes go through the flavor's `KeyboardMapper`, and device-specific keys are handled by its `CustomKeyManager`, which maps them to a user-configurable `CustomAction`.
3. Events go to the fragment that implements `KeyboardView.OnKeyboardActionListener`, which is `AppListFragment`. It filters on character input and runs actions for special keys.
4. System actions (power menu, notifications, screen off) go through the `Accessible` accessibility service and `DeviceAdmin`.

### Spotlight Flow

`KeyForwarderAccessibility` listens for key events and, when it sees the configured activation `KeyCombination`, starts or forwards keys to `OverlayService`. `OverlayService` is a `LifecycleService` that acts as its own `ViewModelStoreOwner` and creates `AppViewModel`/`SpotlightPreferencesViewModel` directly, because there is no Activity. `SpotlightSetupActivity` walks the user through the overlay and accessibility permissions and settings.

### Conventions

- ViewModels run DB work with `viewModelScope.launch(Dispatchers.IO)`. UI state is exposed as LiveData, using `MediatorLiveData` for combined filtering.
- Voice search goes through `SpeechRecognizerManager`.
