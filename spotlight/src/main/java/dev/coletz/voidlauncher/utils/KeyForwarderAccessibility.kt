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
import androidx.preference.PreferenceManager
import dev.coletz.voidlauncher.models.KeyCombination
import dev.coletz.voidlauncher.mvvm.SpotlightPreferencesViewModel.Companion.SpotlightPrefsInfo
import dev.coletz.voidlauncher.services.OverlayService


class KeyForwarderAccessibility : AccessibilityService() {

    private var checkNeeded = true
    private var activationKey: KeyCombination = KeyCombination.DEFAULT

    override fun onCreate() {
        super.onCreate()
        checkNeeded = requestAccessibilityEnabled(this, FLAG_ACTIVITY_NEW_TASK)
        loadActivationKey()
    }

    private fun loadActivationKey() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        activationKey = prefs.getString(SpotlightPrefsInfo.ACTIVATION_KEY.key, null)
            ?.let(KeyCombination::deserialize) ?: KeyCombination.DEFAULT
    }

    override fun onInterrupt() {
        checkNeeded = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            // Activation key toggles overlay
            if (event != null && activationKey.matches(event)) {
                toggleOverlay()
                return true
            }

            // Forward key events to overlay if it's showing
            if (OverlayService.isVisible) {
                forwardKeyToOverlay(event!!)
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
        if (intent?.action == ACTION_REFRESH_PREFERENCES) {
            loadActivationKey()
            return START_NOT_STICKY
        }

        if(checkNeeded) {
            checkNeeded = requestAccessibilityEnabled(this, FLAG_ACTIVITY_NEW_TASK)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        const val ACTION_REFRESH_PREFERENCES = "dev.coletz.voidlauncher.ACTION_REFRESH_PREFERENCES"

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