package dev.coletz.voidlauncher.keyboard

const val deviceWithPhysicalKeyboard = true

fun provideKeyboardMapper(): KeyboardMapper = BlackBerryKeyboardMapper()

fun provideCustomKeyManager(): CustomKeyManager = BlackBerryCustomKeyManager()