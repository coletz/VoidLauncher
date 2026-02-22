package dev.coletz.voidlauncher.keyboard

import android.content.Context
import android.content.res.Configuration

object KeyboardUtils {
    fun hasPhysicalKeyboard(context: Context): Boolean {
        return HAS_PHYSICAL_KEYBOARD || context.resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY
    }
}