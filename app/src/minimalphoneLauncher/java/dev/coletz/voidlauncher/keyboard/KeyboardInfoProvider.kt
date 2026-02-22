package dev.coletz.voidlauncher.keyboard

import android.content.Context

fun deviceHasPhysicalKeyboard(context: Context): Boolean = true

const val SOFTWARE_KEYBOARD_LAYOUT: Int = -1

fun provideKeyboardMapper(): KeyboardMapper = MinimalPhoneKeyboardMapper()

fun provideCustomKeyManager(): CustomKeyManager = MinimalPhoneCustomKeyManager()