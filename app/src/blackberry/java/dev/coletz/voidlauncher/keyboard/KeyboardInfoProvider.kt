package dev.coletz.voidlauncher.keyboard

const val HAS_PHYSICAL_KEYBOARD = true

const val SOFTWARE_KEYBOARD_LAYOUT: Int = -1

fun provideKeyboardMapper(): KeyboardMapper = BlackBerryKeyboardMapper()

fun provideCustomKeyManager(): CustomKeyManager = BlackBerryCustomKeyManager()