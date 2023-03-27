package com.coletz.voidlauncher.mvvm

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.coletz.voidlauncher.models.Preference

class PreferencesViewModel(application: Application): AndroidViewModel(application){

    companion object {
        object Keys {
            val KEYBOARD_BOTTOM_MARGIN = Preference.int("key.KBD_BOTTOM_MARGIN", "Bottom keyboard margin")

            val ALL = listOf(
                KEYBOARD_BOTTOM_MARGIN
            )
        }
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    fun getAllPreferences(): List<Preference.Entity> {
        val sharedPrefs = prefs.all

        prefs.all.forEach { (x, y) ->
            android.util.Log.e("KEY", x)
            android.util.Log.e("VALUE", y?.toString() ?: "NOPE")
        }

        return Keys.ALL.map { key ->
            Preference.Entity(
                key = key,
                value = sharedPrefs.entries.firstOrNull { it.key == key.id }?.value,
            )
        }
    }

    fun updatePreference(preference: Preference.Entity) {
        prefs.edit {
            when(preference.key.type) {
                Preference.Type.INTEGER -> {
                    if (preference.value == null) {
                        remove(preference.key.id)
                    } else {
                        require(preference.value is String)
                        preference.value.toIntOrNull()?.also {
                            putInt(preference.key.id, it)
                        } ?: run {
                            TODO("Invalid value for integer preference")
                        }
                    }
                }
            }
        }
    }

    var keyboardBottomMargin: Int
        get() = prefs.getInt(Keys.KEYBOARD_BOTTOM_MARGIN.id, 26)
        set(value) { prefs.edit { putInt(Keys.KEYBOARD_BOTTOM_MARGIN.id, value) } }
}