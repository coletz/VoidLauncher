package dev.coletz.voidlauncher.mvvm

import android.app.Application
import androidx.preference.PreferenceManager
import dev.coletz.voidlauncher.models.Preference
import dev.coletz.voidlauncher.models.support.SortingDirection

class SpotlightPreferencesViewModel(application: Application) : PreferencesViewModel(application) {

    companion object {

        object SpotlightPrefsInfo {
            val SHOW_ALL_ON_START = Preference.Info.bool("key.SHOW_ALL_ON_START", "Show all apps on start")
            val SORTING_DIRECTION = Preference.Info.enum("key.SORTING_DIRECTION", "Sorting direction", SortingDirection.entries)
            val SPOTLIGHT_WIDTH = Preference.Info.int("key.SPOTLIGHT_WIDTH", "Spotlight width (%)")

            fun getAll(): Array<Preference.Info> = arrayOf(
                SHOW_ALL_ON_START,
                SORTING_DIRECTION,
                SPOTLIGHT_WIDTH,
            )
        }
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    override fun getAllPreferenceDefinitions(): Array<Preference.Info> {
        return super.getAllPreferenceDefinitions() + SpotlightPrefsInfo.getAll()
    }

    val showAllOnStart: Boolean
        get() = prefs.getString(SpotlightPrefsInfo.SHOW_ALL_ON_START.key, "")?.toBooleanStrictOrNull() ?: false

    val sortingDirection: SortingDirection
        get() = prefs.getString(SpotlightPrefsInfo.SORTING_DIRECTION.key, null)?.let(SortingDirection::getById) ?: SortingDirection.ASCENDING

    val spotlightWidthPercentage: Int
        get() = prefs.getString(SpotlightPrefsInfo.SPOTLIGHT_WIDTH.key, "")?.toIntOrNull()?.coerceIn(10, 100) ?: 85
}
