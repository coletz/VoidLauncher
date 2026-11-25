package dev.coletz.voidlauncher.mvvm

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import dev.coletz.voidlauncher.models.Preference
import dev.coletz.voidlauncher.models.support.PersistableEnum
import dev.coletz.voidlauncher.models.support.VoiceSearchLanguage

open class PreferencesViewModel(application: Application): AndroidViewModel(application){

    companion object {

        object AllPrefsInfo {
            val VIBRATE_ON_KEYPRESS = Preference.Info.bool("key.VIBRATE_KEYPRESS", "Vibrate on keypress", defaultValue = true)
            val AUTO_LAUNCH_IF_SINGLE_APP_FOUND = Preference.Info.bool("key.AUTO_LAUNCH_IF_SINGLE_APP", "Auto-open if 1 app found", defaultValue = true)
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

    protected fun readBoolean(info: Preference.Info): Boolean =
        prefs.getString(info.key, null)?.toBooleanStrictOrNull() ?: info.defaultValue.toBoolean()

    /** The stored value, or the definition's default, kept inside the definition's range. */
    protected fun readInt(info: Preference.Info): Int {
        val value = prefs.getString(info.key, null)?.toIntOrNull() ?: info.defaultValue?.toIntOrNull() ?: 0
        return info.intRange?.let(value::coerceIn) ?: value
    }

    /** The stored entry, or the definition's default, or null if neither matches one of [entries]. */
    protected fun <T : PersistableEnum> readEnum(info: Preference.Info, entries: List<T>): T? {
        val rawValue = prefs.getString(info.key, null)
        return entries.firstOrNull { it.id == rawValue } ?: entries.firstOrNull { it.id == info.defaultValue }
    }

    val vibrateOnKeypress: Boolean
        get() = readBoolean(AllPrefsInfo.VIBRATE_ON_KEYPRESS)

    val autoLaunchOnSingleAppFound: Boolean
        get() = readBoolean(AllPrefsInfo.AUTO_LAUNCH_IF_SINGLE_APP_FOUND)

    val voiceSearchLanguage: VoiceSearchLanguage?
        get() = readEnum(AllPrefsInfo.VOICE_SEARCH_LANGUAGE, VoiceSearchLanguage.entries)
}