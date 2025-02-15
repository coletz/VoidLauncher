package dev.coletz.voidlauncher.models.support

enum class CustomAction(override val id: String): PersistableEnum {
    NONE("none"),
    PRESS_BACK("press_back"),
    PRESS_HOME("press_home"),
    PRESS_RECENT("press_recent"),
    OPEN_NOTIFICATION("open_notification"),
    OPEN_QUICK_SETTINGS("open_quick_settings"),
    OPEN_POWER_DIALOG("open_power_dialog"),
    TOGGLE_SPLIT_SCREEN("toggle_split_screen"),
    SCREEN_OFF("screen_off"),
    SCREENSHOT("screenshot");

    companion object {
        const val MAX_ACTIONS: Int = 4

        fun getById(rawValue: String?): CustomAction =
            entries.firstOrNull { it.id == rawValue } ?: NONE
    }
}