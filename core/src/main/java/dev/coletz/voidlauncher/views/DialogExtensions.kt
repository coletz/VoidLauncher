package dev.coletz.voidlauncher.views

import android.app.Dialog
import android.view.ViewGroup

fun Dialog.setWidthPercentage(percentage: Float) {
    val width = (context.resources.displayMetrics.widthPixels * percentage).toInt()
    window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
}
