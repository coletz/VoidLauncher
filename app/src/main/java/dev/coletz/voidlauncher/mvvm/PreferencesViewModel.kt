package dev.coletz.voidlauncher.mvvm

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import dev.coletz.voidlauncher.models.Preference
import dev.coletz.voidlauncher.models.support.CustomAction

class PreferencesViewModel(application: Application): AndroidViewModel(application){

    companion object {
        object AllPrefsInfo {
            internal const val CUSTOM_ACTION_BASE_KEY = "key.CUSTOM_ACTION_"

            val KEYBOARD_BOTTOM_MARGIN = Preference.Info.int("key.KBD_BOTTOM_MARGIN", "Bottom keyboard margin (px)")
            val CUSTOM_ACTIONS = (0 until CustomAction.MAX_ACTIONS).map { Preference.Info.enum("$CUSTOM_ACTION_BASE_KEY$it", "Custom action ${it + 1}", CustomAction.entries) }

            val ALL: Array<Preference.Info> = arrayOf(
                KEYBOARD_BOTTOM_MARGIN,
                *CUSTOM_ACTIONS.toTypedArray()
            )
        }
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    fun getAllPreferences(): List<Preference.Entity> {
        val sharedPrefs = prefs.all
        return AllPrefsInfo.ALL.map { info ->
            Preference.Entity(
                info = info,
                rawValue = sharedPrefs.entries.firstOrNull { it.key == info.key }?.value?.toString()
            )
        }
    }

    fun updatePreference(preference: Preference.Entity) {
        prefs.edit {
            val rawValue = preference.rawValue
            if (rawValue == null) {
                remove(preference.info.key)
            } else {
                putString(preference.info.key, rawValue)
            }
        }
    }

    val keyboardBottomMargin: Int
        get() = prefs.getString(AllPrefsInfo.KEYBOARD_BOTTOM_MARGIN.key,"")?.toIntOrNull() ?: 26

    fun getCustomAction(index: Int): CustomAction {
        require(index in 0 until CustomAction.MAX_ACTIONS)
        return prefs.getString("${AllPrefsInfo.CUSTOM_ACTION_BASE_KEY}$index", "").let(CustomAction.Companion::getById)
    }
}