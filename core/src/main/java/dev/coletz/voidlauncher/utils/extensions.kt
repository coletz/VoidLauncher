package dev.coletz.voidlauncher.utils

import android.util.Log

fun <T> T.debug(tag: Any? = null): T = apply {
    Log.e(tag?.toString() ?: "[DEBUG]", this?.toString() ?: "[NULL]")
}

fun <T, C : Iterable<T>> C.debugEach(tag: Any? = null): C {
    return apply { for (element in this) element.debug(tag) }
}