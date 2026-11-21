package dev.coletz.voidlauncher.keyboard

import android.content.Context
import android.content.res.Configuration
import dev.coletz.voidlauncher.R

fun deviceHasPhysicalKeyboard(context: Context): Boolean {
    return context.resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY
}

val SOFTWARE_KEYBOARD_LAYOUT: Int = R.xml.keyboard_layout

fun provideKeyboardMapper(): KeyboardMapper = SoftwareKeyboardMapper()

fun provideCustomKeyManager(): CustomKeyManager = SoftwareKeyboardCustomKeyManager()