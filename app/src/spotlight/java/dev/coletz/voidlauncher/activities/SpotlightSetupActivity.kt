package dev.coletz.voidlauncher.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dev.coletz.voidlauncher.R
import dev.coletz.voidlauncher.utils.KeyForwarderAccessibility
import androidx.core.net.toUri

class SpotlightSetupActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var overlayButton: Button
    private lateinit var accessibilityButton: Button
    private lateinit var instructionsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spotlight_setup)

        statusText = findViewById(R.id.status_text)
        overlayButton = findViewById(R.id.overlay_permission_btn)
        accessibilityButton = findViewById(R.id.accessibility_permission_btn)
        instructionsText = findViewById(R.id.instructions_text)

        overlayButton.setOnClickListener {
            requestOverlayPermission()
        }

        accessibilityButton.setOnClickListener {
            KeyForwarderAccessibility.requestAccessibilityEnabled(this)
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        val hasOverlayPermission = Settings.canDrawOverlays(this)
        val hasAccessibilityPermission = isAccessibilityEnabled()

        overlayButton.isEnabled = !hasOverlayPermission
        overlayButton.text = if (hasOverlayPermission) "Overlay Permission Granted" else "Grant Overlay Permission"

        accessibilityButton.isEnabled = !hasAccessibilityPermission
        accessibilityButton.text = if (hasAccessibilityPermission) "Accessibility Enabled" else "Enable Accessibility Service"

        if (hasOverlayPermission && hasAccessibilityPermission) {
            statusText.text = "Setup Complete!"
            instructionsText.text = "Press Ctrl+Space anywhere to open Void Launcher Spotlight.\n\nYou can now close this app."
        } else {
            statusText.text = "Setup Required"
            val missing = mutableListOf<String>()
            if (!hasOverlayPermission) missing.add("Overlay permission")
            if (!hasAccessibilityPermission) missing.add("Accessibility service")
            instructionsText.text = "Please grant the following permissions:\n${missing.joinToString("\n") { "- $it" }}"
        }
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
