package dev.coletz.voidlauncher.views

import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import dev.coletz.voidlauncher.core.R
import dev.coletz.voidlauncher.models.KeyCombination

class KeyCombinationRecordDialog(context: Context) {

    private val rootView = LayoutInflater.from(context).inflate(R.layout.key_combination_record_dialog, null)
    private val keyCombinationText: TextView = rootView.findViewById(R.id.key_combination_text)
    private val builder: AlertDialog.Builder

    private var currentCombination: KeyCombination? = null
    private var onConfirm: (KeyCombination) -> Unit = {}

    init {
        builder = AlertDialog.Builder(context).apply {
            setView(rootView)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ ->
                currentCombination?.let(onConfirm)
            }
        }
    }

    fun setTitle(title: String?) = apply {
        builder.setTitle(title)
    }

    fun setCurrentCombination(combination: KeyCombination?) = apply {
        currentCombination = combination
        updateDisplay()
    }

    /** Adds a "Reset to default" button that confirms [combination]. No button if null. */
    fun setDefaultCombination(combination: KeyCombination?) = apply {
        combination ?: return@apply
        builder.setNeutralButton(R.string.preference_reset_to_default) { _, _ ->
            onConfirm(combination)
        }
    }

    fun setOnConfirmClicked(onConfirm: (KeyCombination) -> Unit) = apply {
        this.onConfirm = onConfirm
    }

    fun show() {
        val dialog = builder.create()

        dialog.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && !isModifierOnly(keyCode)) {
                currentCombination = KeyCombination.fromKeyEvent(event)
                updateDisplay()
                true
            } else {
                false
            }
        }

        dialog.show()
        updateDisplay()
    }

    private fun updateDisplay() {
        keyCombinationText.text = currentCombination?.toDisplayString()
            ?: keyCombinationText.context.getString(R.string.preference_key_combination_prompt)
    }

    private fun isModifierOnly(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_CTRL_LEFT ||
                keyCode == KeyEvent.KEYCODE_CTRL_RIGHT ||
                keyCode == KeyEvent.KEYCODE_ALT_LEFT ||
                keyCode == KeyEvent.KEYCODE_ALT_RIGHT ||
                keyCode == KeyEvent.KEYCODE_SHIFT_LEFT ||
                keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT ||
                keyCode == KeyEvent.KEYCODE_META_LEFT ||
                keyCode == KeyEvent.KEYCODE_META_RIGHT
    }
}
