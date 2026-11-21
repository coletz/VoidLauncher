package dev.coletz.voidlauncher.keyboard

class MinimalPhoneCustomKeyManager: CustomKeyManager() {
    override fun getCustomKeys(): List<CustomKey> = listOf(
        CustomKey(0, Keyboard.KEYCODE_ALT, "ALT Key"),
        CustomKey(1, Keyboard.KEYCODE_SHIFT_LEFT, "Left Shift Key"),
        CustomKey(2, Keyboard.KEYCODE_SHIFT_RIGHT, "Right Shift Key"),
        CustomKey(3, Keyboard.KEYCODE_CUSTOM_VOICE_RECORD, "Microphone Key")
    )
}