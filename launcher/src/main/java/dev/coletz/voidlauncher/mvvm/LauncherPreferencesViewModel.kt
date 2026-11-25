package dev.coletz.voidlauncher.mvvm

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import dev.coletz.voidlauncher.keyboard.deviceHasPhysicalKeyboard
import dev.coletz.voidlauncher.keyboard.provideCustomKeyManager
import dev.coletz.voidlauncher.models.Preference
import dev.coletz.voidlauncher.models.support.CustomAction

class LauncherPreferencesViewModel(private val application: Application): PreferencesViewModel(application){

    companion object {

        private val customKeyManager = provideCustomKeyManager()

        object LauncherPrefsInfo {
            internal const val CUSTOM_ACTION_BASE_KEY = "key.CUSTOM_ACTION_"

            val KEYBOARD_BOTTOM_MARGIN = Preference.Info.int("key.KBD_BOTTOM_MARGIN", "Bottom kb margin (px)", defaultValue = 26)
            val CUSTOM_ACTIONS = customKeyManager.getCustomKeys().map { Preference.Info.enum("$CUSTOM_ACTION_BASE_KEY${it.id}", it.label, CustomAction.entries, defaultValue = CustomAction.NONE) }
            val DISTRACTION_FREE_MODE = Preference.Info.bool("key.DISTRACTION_FREE_MODE", "Distraction free mode", false)

            fun getAll(context: Context): Array<Preference.Info> = mutableListOf<Preference.Info>().apply {
                add(DISTRACTION_FREE_MODE)
                if (!deviceHasPhysicalKeyboard(context)) { add(KEYBOARD_BOTTOM_MARGIN) }
                addAll(CUSTOM_ACTIONS)
            }.toTypedArray()
        }
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    override fun getAllPreferenceDefinitions(): Array<Preference.Info> {
        return super.getAllPreferenceDefinitions() + LauncherPrefsInfo.getAll(application)
    }

    val keyboardBottomMargin: Int
        get() = readInt(LauncherPrefsInfo.KEYBOARD_BOTTOM_MARGIN)

    val distractionFreeMode: Boolean
        get() = prefs.getString(LauncherPrefsInfo.DISTRACTION_FREE_MODE.key, "")?.toBooleanStrictOrNull() ?: false

    fun getCustomActionByKey(keyPrimaryCode: Int): CustomAction {
        val actionId = customKeyManager.getCustomKeyByCode(keyPrimaryCode).id
        return prefs.getString("${LauncherPrefsInfo.CUSTOM_ACTION_BASE_KEY}$actionId", "").let(CustomAction.Companion::getById)
    }
}