package dev.coletz.voidlauncher.keyboard

import dev.coletz.voidlauncher.R

const val HAS_PHYSICAL_KEYBOARD = false

val SOFTWARE_KEYBOARD_LAYOUT: Int = R.xml.keyboard_layout

fun provideKeyboardMapper(): KeyboardMapper = SoftwareKeyboardMapper()

fun provideCustomKeyManager(): CustomKeyManager = SoftwareKeyboardCustomKeyManager()