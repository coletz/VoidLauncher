package dev.coletz.voidlauncher.mvvm

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import dev.coletz.voidlauncher.models.Preference
import dev.coletz.voidlauncher.models.support.VoiceSearchLanguage

open class PreferencesViewModel(application: Application): AndroidViewModel(application){

    companion object {

        object AllPrefsInfo {
            val VIBRATE_ON_KEYPRESS = Preference.Info.bool("key.VIBRATE_KEYPRESS", "Vibrate on keypress")
            val AUTO_LAUNCH_IF_SINGLE_APP_FOUND = Preference.Info.bool("key.AUTO_LAUNCH_IF_SINGLE_APP", "Auto-open if 1 app found")
            val VOICE_SEARCH_LANGUAGE = Preference.Info.enum("key.VOICE_SEARCH_LANGUAGE", "Voice search language", VoiceSearchLanguage.entries)

            fun getAll(): Array<Preference.Info> = mutableListOf<Preference.Info>().apply {
                add(VIBRATE_ON_KEYPRESS)
                add(AUTO_LAUNCH_IF_SINGLE_APP_FOUND)
                add(VOICE_SEARCH_LANGUAGE)
            }.toTypedArray()
        }
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    protected open fun getAllPreferenceDefinitions(): Array<Preference.Info> {
        return AllPrefsInfo.getAll()
    }

    fun getAllPreferences(): List<Preference.Entity> {
        val sharedPrefs = prefs.all
        return getAllPreferenceDefinitions().map { info ->
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

    val vibrateOnKeypress: Boolean
        get() = prefs.getString(AllPrefsInfo.VIBRATE_ON_KEYPRESS.key,"")?.toBooleanStrictOrNull() ?: true

    val autoLaunchOnSingleAppFound: Boolean
        get() = prefs.getString(AllPrefsInfo.AUTO_LAUNCH_IF_SINGLE_APP_FOUND.key,"")?.toBooleanStrictOrNull() ?: true

    val voiceSearchLanguage: VoiceSearchLanguage?
        get() = prefs.getString(AllPrefsInfo.VOICE_SEARCH_LANGUAGE.key, null)?.let(VoiceSearchLanguage.Companion::getById)
}