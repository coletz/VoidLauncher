package com.coletz.voidlauncher.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.content.ComponentName
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.text.TextUtils
import android.provider.Settings.Secure
import android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.provider.Settings


class Accessible : AccessibilityService() {

    private var checkNeeded = true

    override fun onCreate() {
        super.onCreate()
        checkNeeded = requestAccessibilityEnabled(this, FLAG_ACTIVITY_NEW_TASK)
    }

    override fun onInterrupt() {
        checkNeeded = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(checkNeeded) {
            checkNeeded = requestAccessibilityEnabled(this, FLAG_ACTIVITY_NEW_TASK)
        }
        val bundle = intent?.extras ?: return super.onStartCommand(intent, flags, startId)

        if(intent.hasExtra(EXTRA_GLOBAL_ACTION)) {
            bundle.getInt(EXTRA_GLOBAL_ACTION)
                .let(::performGlobalAction)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        private const val EXTRA_GLOBAL_ACTION = "GLOB_ACT"

        private fun run(context: Context?, globalAction: Int){
            context ?: return
            Intent(context, Accessible::class.java)
                .putExtra(EXTRA_GLOBAL_ACTION, globalAction)
                .run { context.startService(this) }
        }

        fun pressBack(context: Context?) = run(context, GLOBAL_ACTION_BACK)

        fun pressHome(context: Context?) = run(context, GLOBAL_ACTION_HOME)
        fun pressRecents(context: Context?) = run(context, GLOBAL_ACTION_RECENTS)
        fun openNotification(context: Context?) = run(context, GLOBAL_ACTION_NOTIFICATIONS)
        fun openQuickSettings(context: Context?) = run(context, GLOBAL_ACTION_QUICK_SETTINGS)
        fun openPowerDialog(context: Context?) = run(context, GLOBAL_ACTION_POWER_DIALOG)
        fun toggleSplitScreen(context: Context?) = run(context, GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        fun screenOff(context: Context?) = run(context, GLOBAL_ACTION_LOCK_SCREEN)
        fun screenshot(context: Context?) = run(context, GLOBAL_ACTION_TAKE_SCREENSHOT)

        fun requestAccessibilityEnabled(context: Context, customFlags: Int? = null): Boolean {
            val expectedComponentName = ComponentName(context, Accessible::class.java)

            val enabledServicesSetting = Secure.getString(context.contentResolver, ENABLED_ACCESSIBILITY_SERVICES)
                ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                val enabledService = ComponentName.unflattenFromString(componentNameString)

                if (enabledService != null && enabledService == expectedComponentName)
                    return true
            }

            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .apply { customFlags?.let(::addFlags) }
                .also { context.startActivity(it) }
            return false
        }
    }
}