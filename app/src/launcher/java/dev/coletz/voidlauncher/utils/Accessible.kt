package dev.coletz.voidlauncher.utils

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.provider.Settings
import android.provider.Settings.Secure
import android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import dev.coletz.voidlauncher.models.support.CustomAction
import dev.coletz.voidlauncher.models.support.CustomAction.*


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
        fun pressRecent(context: Context?) = run(context, GLOBAL_ACTION_RECENTS)
        fun openNotification(context: Context?) = run(context, GLOBAL_ACTION_NOTIFICATIONS)
        fun openQuickSettings(context: Context?) = run(context, GLOBAL_ACTION_QUICK_SETTINGS)
        fun openPowerDialog(context: Context?) = run(context, GLOBAL_ACTION_POWER_DIALOG)
        fun toggleSplitScreen(context: Context?) = run(context, GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        fun screenOff(activity: Activity?) = screenOffCompat(activity)
        fun screenshot(context: Context?) = run(context, GLOBAL_ACTION_TAKE_SCREENSHOT)
        fun voiceAssistant(context: Context?) {
            context ?: return
            try {
                val intent = Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE)
                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    .putExtra(RecognizerIntent.EXTRA_PROMPT, "How can I help you?")

                // Check if there's an activity that can handle this intent
                if (intent.resolveActivity(context.packageManager) != null) {
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK) // Add this flag if calling from a non-Activity context
                    context.startActivity(intent)
                } else {
                    // Fallback or error handling if no voice assistant is available
                    Toast.makeText(context, "Voice assistant not available", Toast.LENGTH_SHORT).show()
                }
            } catch (_: ActivityNotFoundException) {
                // This specific exception might also indicate no voice assistant
                Toast.makeText(context, "Voice assistant not found", Toast.LENGTH_SHORT).show()
            } catch (e: kotlin.Exception) {
                // General error handling
                Toast.makeText(context, "Could not start voice assistant", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        fun runCustomAction(customAction: CustomAction, activity: Activity) {
            when(customAction) {
                NONE -> {}
                PRESS_BACK -> pressBack(activity)
                PRESS_HOME -> pressHome(activity)
                PRESS_RECENT -> pressRecent(activity)
                OPEN_NOTIFICATION -> openNotification(activity)
                OPEN_QUICK_SETTINGS -> openQuickSettings(activity)
                OPEN_POWER_DIALOG -> openPowerDialog(activity)
                TOGGLE_SPLIT_SCREEN -> toggleSplitScreen(activity)
                SCREEN_OFF -> screenOff(activity)
                SCREENSHOT -> screenshot(activity)
                VOICE_ASSISTANT -> voiceAssistant(activity)
                VOICE_SEARCH -> {/* Do nothing, handled outside */ }
            }
        }

        fun requestAccessibilityEnabled(context: Context, customFlags: Int? = null): Boolean {
            val expectedComponentName = ComponentName(context, Accessible::class.java)
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

        private fun screenOffCompat(activity: Activity?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                screenOffCompat28(activity)
            } else {
                screenOffCompatPre28(activity)
            }
        }

        @RequiresApi(Build.VERSION_CODES.P)
        private fun screenOffCompat28(context: Context?) {
            run(context, GLOBAL_ACTION_LOCK_SCREEN)
        }

        private fun screenOffCompatPre28(activity: Activity?) {
            val devicePolicyManager = activity?.getSystemService<DevicePolicyManager>() ?: return
            val adminComponent = ComponentName(activity, DeviceAdmin::class.java)
            if (devicePolicyManager.isAdminActive(adminComponent)) {
                devicePolicyManager.lockNow();
            } else {
                Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                    .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app needs admin privileges to lock the screen.")
                    .run { activity.startActivityForResult(this, 1) }
            }
        }
    }
}