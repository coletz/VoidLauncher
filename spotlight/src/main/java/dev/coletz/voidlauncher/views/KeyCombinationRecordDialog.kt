package dev.coletz.voidlauncher.views

import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import dev.coletz.voidlauncher.R
import dev.coletz.voidlauncher.models.KeyCombination

class KeyCombinationRecordDialog(context: Context) {

    private val rootView = LayoutInflater.from(context).inflate(R.layout.key_combination_record_dialog, null)
    private val keyCombinationText: TextView = rootView.findViewById(R.id.key_combination_text)
    private val builder: AlertDialog.Builder

    private var currentCombination: KeyCombination = KeyCombination.DEFAULT
    private var onConfirm: (KeyCombination) -> Unit = {}

    init {
        builder = AlertDialog.Builder(context).apply {
            setView(rootView)
            setNegativeButton(android.R.string.cancel, null)
            setNeutralButton("Reset to Default") { _, _ ->
                onConfirm(KeyCombination.DEFAULT)
            }
            setPositiveButton(android.R.string.ok) { _, _ ->
                onConfirm(currentCombination)
            }
        }
    }

    fun setTitle(title: String?) = apply {
        builder.setTitle(title)
    }

    fun setCurrentCombination(combination: KeyCombination) = apply {
        currentCombination = combination
        updateDisplay()
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
        keyCombinationText.text = currentCombination.toDisplayString()
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
