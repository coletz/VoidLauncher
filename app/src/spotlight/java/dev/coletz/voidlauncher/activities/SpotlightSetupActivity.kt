package dev.coletz.voidlauncher.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import dev.coletz.voidlauncher.R
import dev.coletz.voidlauncher.appoptions.PreferenceManagerDialog
import dev.coletz.voidlauncher.mvvm.SpotlightPreferencesViewModel
import dev.coletz.voidlauncher.utils.KeyForwarderAccessibility

class SpotlightSetupActivity : AppCompatActivity() {

    private lateinit var overlayButton: Button
    private lateinit var accessibilityButton: Button
    private lateinit var appSettingsButton: Button

    private val prefsViewModel: SpotlightPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spotlight_setup)

        overlayButton = findViewById(R.id.overlay_permission_btn)
        accessibilityButton = findViewById(R.id.accessibility_permission_btn)
        appSettingsButton = findViewById(R.id.app_settings_btn)

        overlayButton.setOnClickListener {
            requestOverlayPermission()
        }

        accessibilityButton.setOnClickListener {
            KeyForwarderAccessibility.requestAccessibilityEnabled(this)
        }

        appSettingsButton.setOnClickListener {
            openAppSettings()
        }
    }

    private fun openAppSettings() {
        PreferenceManagerDialog(this).apply {
            val updateData = { loadPreferences(prefsViewModel.getAllPreferences()) }

            setOnPreferenceUpdatedListener {
                prefsViewModel.updatePreference(it)
                updateData()
            }

            updateData()

            show()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        overlayButton.isVisible = !Settings.canDrawOverlays(this)
        accessibilityButton.isVisible = !isAccessibilityEnabled()
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:$packageName".toUri()
        )
        startActivity(intent)
    }

    private fun isAccessibilityEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val expectedComponentName = "$packageName/${KeyForwarderAccessibility::class.java.name}"
        return enabledServices.contains(expectedComponentName)
    }
}
