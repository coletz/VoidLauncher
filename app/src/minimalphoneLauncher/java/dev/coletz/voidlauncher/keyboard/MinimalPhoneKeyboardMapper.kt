package dev.coletz.voidlauncher.keyboard

class MinimalPhoneKeyboardMapper: KeyboardMapper {
    override fun mapKeyCode(keyCode: Int): Int {
        return when (keyCode) {
            56 -> Keyboard.KEYCODE_CUSTOM_VOICE_RECORD
            57 -> Keyboard.KEYCODE_ALT
            59 -> Keyboard.KEYCODE_SHIFT_LEFT
            60 -> Keyboard.KEYCODE_SHIFT_RIGHT
            62 -> Keyboard.KEYCODE_SPACE
            66 -> Keyboard.KEYCODE_DONE
            67 -> Keyboard.KEYCODE_DELETE
            else -> 0
        }
    }
}