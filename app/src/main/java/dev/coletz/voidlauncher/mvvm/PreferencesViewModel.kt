package dev.coletz.voidlauncher.mvvm

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import dev.coletz.voidlauncher.keyboard.deviceWithPhysicalKeyboard
import dev.coletz.voidlauncher.keyboard.provideCustomKeyManager
import dev.coletz.voidlauncher.models.Preference
import dev.coletz.voidlauncher.models.support.CustomAction
import dev.coletz.voidlauncher.models.support.VoiceSearchLanguage

class PreferencesViewModel(application: Application): AndroidViewModel(application){

    companion object {

        private val customKeyManager = provideCustomKeyManager()

        object AllPrefsInfo {
            internal const val CUSTOM_ACTION_BASE_KEY = "key.CUSTOM_ACTION_"

            val KEYBOARD_BOTTOM_MARGIN = Preference.Info.int("key.KBD_BOTTOM_MARGIN", "Bottom kb margin (px)")
            val VIBRATE_ON_KEYPRESS = Preference.Info.bool("key.VIBRATE_KEYPRESS", "Vibrate on keypress")
            val AUTO_LAUNCH_IF_SINGLE_APP_FOUND = Preference.Info.bool("key.AUTO_LAUNCH_IF_SINGLE_APP", "Auto-open if 1 app found")
            val VOICE_SEARCH_LANGUAGE = Preference.Info.enum("key.VOICE_SEARCH_LANGUAGE", "Voice search language", VoiceSearchLanguage.entries)
            val CUSTOM_ACTIONS = customKeyManager.getCustomKeys().map { Preference.Info.enum("$CUSTOM_ACTION_BASE_KEY${it.id}", it.label, CustomAction.entries) }

            val ALL: Array<Preference.Info> = mutableListOf<Preference.Info>().apply {
                if (!deviceWithPhysicalKeyboard) { add(KEYBOARD_BOTTOM_MARGIN) }
                add(VIBRATE_ON_KEYPRESS)
                add(AUTO_LAUNCH_IF_SINGLE_APP_FOUND)
                add(VOICE_SEARCH_LANGUAGE)
                addAll(CUSTOM_ACTIONS)
            }.toTypedArray()
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

    val vibrateOnKeypress: Boolean
        get() = prefs.getString(AllPrefsInfo.VIBRATE_ON_KEYPRESS.key,"")?.toBooleanStrictOrNull() ?: true

    val autoLaunchOnSingleAppFound: Boolean
        get() = prefs.getString(AllPrefsInfo.AUTO_LAUNCH_IF_SINGLE_APP_FOUND.key,"")?.toBooleanStrictOrNull() ?: true

    val voiceSearchLanguage: VoiceSearchLanguage?
        get() = prefs.getString(AllPrefsInfo.VOICE_SEARCH_LANGUAGE.key, null)?.let(VoiceSearchLanguage.Companion::getById)

    fun getCustomActionByKey(keyPrimaryCode: Int): CustomAction {
        val actionId = customKeyManager.getCustomKeyByCode(keyPrimaryCode).id
        return prefs.getString("${AllPrefsInfo.CUSTOM_ACTION_BASE_KEY}$actionId", "").let(CustomAction.Companion::getById)
    }
}