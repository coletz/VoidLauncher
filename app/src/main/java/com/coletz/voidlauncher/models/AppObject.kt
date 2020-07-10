package com.coletz.voidlauncher.models

import android.content.Context
import android.content.Intent

data class AppObject(
    val name: String,
    val packageName: String,
    val isIntent: Boolean = false
): Comparable<AppObject> {
    fun launch(context: Context?) {
        context ?: return
        if(isIntent) {
            context.startActivity(Intent(packageName))
        } else {
            context
                .packageManager
                .getLaunchIntentForPackage(packageName)
                .run { context.startActivity(this) }
        }

    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if(other !is AppObject) return false
        return packageName == other.packageName
    }

    override fun compareTo(other: AppObject): Int {
        if(packageName == other.packageName) return 0
        return name.toLowerCase().compareTo(other.name.toLowerCase())
    }
}