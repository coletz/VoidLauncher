package com.coletz.voidlauncher.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

fun <T> T.debug(tag: Any? = null): T = apply {
    Log.e(tag?.toString() ?: "[DEBUG]", this?.toString() ?: "[NULL]")
}

fun <T, C : Iterable<T>> C.debugEach(tag: Any? = null): C {
    return apply { for (element in this) element.debug(tag) }
}

fun Context.wip() {
    Toast.makeText(this, "Feature is still WIP, sorry", Toast.LENGTH_LONG).show()
}