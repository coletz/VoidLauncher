package dev.coletz.voidlauncher.utils

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.provider.Settings
import android.provider.Settings.Secure
import android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
import android.text.TextUtils
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import dev.coletz.voidlauncher.services.OverlayService


class KeyForwarderAccessibility : AccessibilityService() {

    private var checkNeeded = true

    override fun onCreate() {
        super.onCreate()
        checkNeeded = requestAccessibilityEnabled(this, FLAG_ACTIVITY_NEW_TASK)
    }

    override fun onInterrupt() {
        checkNeeded = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            // Ctrl+Space toggles overlay
            if (event.keyCode == KeyEvent.KEYCODE_SPACE && event.isCtrlPressed) {
                toggleOverlay()
                return true
            }

            // Forward key events to overlay if it's showing
            if (OverlayService.isShowing()) {
                forwardKeyToOverlay(event)
                return true
            }
        }
        return super.onKeyEvent(event)
    }

    private fun toggleOverlay() {
        Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_TOGGLE
        }.also { startService(it) }
    }

    private fun forwardKeyToOverlay(event: KeyEvent) {
        Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_KEY_EVENT
            putExtra(OverlayService.EXTRA_KEY_CODE, event.keyCode)
            putExtra(OverlayService.EXTRA_UNICODE_CHAR, event.unicodeChar)
        }.also { startService(it) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(checkNeeded) {
            checkNeeded = requestAccessibilityEnabled(this, FLAG_ACTIVITY_NEW_TASK)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    companion object {

        fun requestAccessibilityEnabled(context: Context, customFlags: Int? = null): Boolean {
            val expectedComponentName = ComponentName(context, KeyForwarderAccessibility::class.java)
            val enabledServicesSetting = Secure.getString(context.contentResolver, ENABLED_ACCESSIBILITY_SERVICES)
            if (enabledServicesSetting != null) {
                val colonSplitter = TextUtils.SimpleStringSplitter(':')
                colonSplitter.setString(enabledServicesSetting)
                while (colonSplitter.hasNext()) {
                    val componentNameString = colonSplitter.next()
                    val enabledService = ComponentName.unflattenFromString(componentNameString)

                    if (enabledService != null && enabledService == expectedComponentName){
                        return true
                    }
                }
            }

            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .apply { customFlags?.let(::addFlags) }
                .also { context.startActivity(it) }

            return false
        }
    }
}