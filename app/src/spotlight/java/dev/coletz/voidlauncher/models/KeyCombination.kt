package dev.coletz.voidlauncher.models

import android.view.KeyEvent

data class KeyCombination(
    val keyCode: Int,
    val requireCtrl: Boolean = false,
    val requireAlt: Boolean = false,
    val requireShift: Boolean = false,
    val requireMeta: Boolean = false
) {
    fun matches(event: KeyEvent): Boolean {
        return event.keyCode == keyCode &&
                event.isCtrlPressed == requireCtrl &&
                event.isAltPressed == requireAlt &&
                event.isShiftPressed == requireShift &&
                event.isMetaPressed == requireMeta
    }

    fun toDisplayString(): String {
        val parts = mutableListOf<String>()
        if (requireCtrl) parts.add("Ctrl")
        if (requireAlt) parts.add("Alt")
        if (requireShift) parts.add("Shift")
        if (requireMeta) parts.add("Meta")
        parts.add(KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_"))
        return parts.joinToString("+")
    }

    fun serialize(): String {
        return "$keyCode|$requireCtrl|$requireAlt|$requireShift|$requireMeta"
    }

    companion object {
        val DEFAULT = KeyCombination(
            keyCode = KeyEvent.KEYCODE_SPACE,
            requireCtrl = true
        )

        fun deserialize(value: String): KeyCombination? {
            val parts = value.split("|")
            if (parts.size != 5) return null
            return try {
                KeyCombination(
                    keyCode = parts[0].toInt(),
                    requireCtrl = parts[1].toBooleanStrict(),
                    requireAlt = parts[2].toBooleanStrict(),
                    requireShift = parts[3].toBooleanStrict(),
                    requireMeta = parts[4].toBooleanStrict()
                )
            } catch (e: Exception) {
                null
            }
        }

        fun fromKeyEvent(event: KeyEvent): KeyCombination {
            return KeyCombination(
                keyCode = event.keyCode,
                requireCtrl = event.isCtrlPressed,
                requireAlt = event.isAltPressed,
                requireShift = event.isShiftPressed,
                requireMeta = event.isMetaPressed
            )
        }
    }
}
