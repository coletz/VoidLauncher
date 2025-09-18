package dev.coletz.voidlauncher.keyboard

const val deviceWithPhysicalKeyboard = true

fun provideKeyboardMapper(): KeyboardMapper = MinimalPhoneKeyboardMapper()

fun provideCustomKeyManager(): CustomKeyManager = MinimalPhoneCustomKeyManager()