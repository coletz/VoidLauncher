package dev.coletz.voidlauncher.keyboard

class BlackBerryKeyboardMapper: KeyboardMapper {
    override fun mapKeyCode(keyCode: Int): Int {
        return when (keyCode) {
            7 -> Keyboard.KEYCODE_CUSTOM_VOICE_RECORD
            11 -> Keyboard.KEYCODE_CUSTOM_CURRENCY
            in 29..54 -> keyCode + 36
            57 -> Keyboard.KEYCODE_ALT
            59 -> Keyboard.KEYCODE_SHIFT_LEFT
            60 -> Keyboard.KEYCODE_SHIFT_RIGHT
            62 -> Keyboard.KEYCODE_SPACE
            63 -> Keyboard.KEYCODE_MODE_CHANGE
            66 -> Keyboard.KEYCODE_DONE
            67 -> Keyboard.KEYCODE_DELETE
            else -> 0
        }
    }
}