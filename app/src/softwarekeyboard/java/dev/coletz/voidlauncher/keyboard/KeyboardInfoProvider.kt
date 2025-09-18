package dev.coletz.voidlauncher.keyboard

const val deviceWithPhysicalKeyboard = false

fun provideKeyboardMapper(): KeyboardMapper = SoftwareKeyboardMapper()

fun provideCustomKeyManager(): CustomKeyManager = SoftwareKeyboardCustomKeyManager()