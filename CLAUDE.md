# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

Variant task names are `<devType><AppType><BuildType>` (e.g. `SoftwarekeyboardLauncherDebug`).

```bash
# Launcher variants
./gradlew assembleSoftwarekeyboardLauncherDebug
./gradlew assembleBlackberryLauncherDebug
./gradlew assembleMinimalphoneLauncherDebug

# Spotlight variant (overlay app search, not a launcher)
./gradlew assembleSoftwarekeyboardSpotlightDebug

# Install on connected device
./gradlew installSoftwarekeyboardLauncherDebug

# Release builds need CLZ_RELEASE_STORE_FILE / _STORE_PASSWORD / _KEY_ALIAS / _KEY_PASSWORD in gradle.properties
./gradlew assembleSoftwarekeyboardLauncherRelease
```

There are no unit or instrumented tests yet (`app/src/test` and `app/src/androidTest` don't exist). To check that a change compiles everywhere, build at least one Launcher variant and one Spotlight variant, because each app type compiles a different set of sources.

Application IDs: `dev.coletz.voidlauncher`, with `.spotlight` added for Spotlight and `.dev` added for debug builds. All four combinations can be installed side by side.

## Architecture Overview

**Void Launcher** is a minimalist, keyboard-driven Android launcher for devices with physical keyboards (BlackBerry, MinimalPhone) and for standard touchscreen devices. **Void Spotlight** is a companion app built from the same codebase. It is not a launcher: it shows an app-search overlay that an accessibility service triggers with a key combination. Open work items are in `TODO.md`.

### Product Flavors

Two flavor dimensions:

- **`dev_type`**: `softwarekeyboard`, `blackberry`, `minimalphone`
- **`app_type`**: `launcher` (`BuildConfig.IS_LAUNCHER=true`), `spotlight` (`IS_LAUNCHER=false`)

### Source Sets: where code lives

- `src/main/`: shared code (ViewModels, Room, repositories, adapters, dialogs)
- `src/launcher/` and `src/spotlight/`: app-type code, manifests, layouts
- `src/<devType>Launcher/` (e.g. `src/blackberryLauncher/`): **all device-specific Kotlin code.** Each one provides `keyboard/KeyboardInfoProvider.kt` with top-level functions `deviceHasPhysicalKeyboard(context)`, `provideKeyboardMapper()`, `provideCustomKeyManager()`, and the `SOFTWARE_KEYBOARD_LAYOUT` constant, along with that device's `KeyboardMapper`/`CustomKeyManager` implementations.
- `src/<devType>/` (plain dev_type sets): resources only. For example, `minimalphone` overrides colors and themes. There is no Kotlin here.

Consequence: keyboard mapping and custom keys exist **only for Launcher variants**. Spotlight code in `src/spotlight/` must not depend on `KeyboardInfoProvider` or the `KeyboardMapper`/`CustomKeyManager` abstractions, which are defined in `src/launcher/`. If you add a new device-specific function, add it to every `<devType>Launcher` source set.

`src/launcher/.../keyboard/Keyboard.java` and `KeyboardView.java` are vendored copies of the deprecated Android `inputmethodservice` keyboard classes. They draw the on-screen keyboard in `AppListFragment`.

### Data Flow

- `PackageManagerDao` gets the launchable activities from `LauncherApps`. `AppRepository.updateApps()` syncs them into Room: it deletes uninstalled apps, inserts new ones, and refreshes official names. User edits such as renamed apps, hidden apps, favorites, tags and folders are kept only in Room.
- `AppListChangeReceiver` re-syncs when packages are added or removed.
- `AppViewModel` (shared) exposes `apps` (LiveData) and filters it with `filter` (MutableLiveData). Both apps and folders are shown as `MainListUiItem` (`AppUiItem` / `FolderUiItem`) in `AppsAdapter`.
- `VoidDatabase` (Room, entities `AppEntity`, `TagEntity`, `FolderEntity`, `FoldersAppsCrossRef`) exports its schemas to `app/schemas/` through KSP. The current DB is `dev.coletz.voidlauncher.room.VoidDatabase` version 1. `com.coletz...` is a schema from the old package name. If you change an entity, bump the version and add a migration.

### Preferences

Preferences are stored as **strings** in the default `SharedPreferences`, even booleans and ints. Each one is declared as a `Preference.Info` (`bool`/`int`/`enum`/`keyCombination`). `PreferencesViewModel` (main) defines the shared preferences. `LauncherPreferencesViewModel` and `SpotlightPreferencesViewModel` subclass it and override `getAllPreferenceDefinitions()` to add flavor-specific ones; for example, the launcher creates one `CUSTOM_ACTION_*` pref per custom key that `CustomKeyManager` provides. `PreferenceManagerDialog` builds the settings UI generically from these definitions. To add a setting, declare an `Info`, add it to `getAll()`, and add a typed getter.

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
