package dev.coletz.voidlauncher.models.support

interface PersistableEnum {
    /** Value written to preferences. Never change it for an existing entry. */
    val id: String

    /** Human readable name shown in the UI. Defaults to the enum name, e.g. PRESS_BACK -> "Press back". */
    val label: String
        get() = (this as? Enum<*>)?.name
            ?.lowercase()
            ?.replace('_', ' ')
            ?.replaceFirstChar { it.uppercase() }
            ?: id
}