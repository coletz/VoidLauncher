package dev.coletz.voidlauncher.views

import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.core.widget.doAfterTextChanged
import dev.coletz.voidlauncher.core.R

/**
 * Asks for an integer. When a range is set, a slider is shown next to the text field and kept in
 * sync with it, and the confirmed value is clamped to the range.
 */
class IntInputDialog(private val context: Context) {

    private val rootView = LayoutInflater.from(context).inflate(R.layout.int_input_dialog, null)
    private val input: EditText = rootView.findViewById(R.id.int_input)
    private val seekBar: SeekBar = rootView.findViewById(R.id.int_seek_bar)
    private val rangeLabel: TextView = rootView.findViewById(R.id.int_range_label)

    private val builder: AlertDialog.Builder
    private var onConfirm: (Int) -> Unit = {}
    private var range: IntRange? = null

    init {
        builder = AlertDialog.Builder(context).apply {
            setView(rootView)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ -> confirm() }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) { showInInput(progress) }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

        input.doAfterTextChanged { text ->
            val value = text?.toString()?.toIntOrNull() ?: return@doAfterTextChanged
            if (range?.contains(value) == true) { seekBar.progress = value }
        }
    }

    fun setTitle(title: String?) = apply {
        builder.setTitle(title)
    }

    fun setRange(range: IntRange?) = apply {
        this.range = range
        val visibility = if (range == null) View.GONE else View.VISIBLE
        seekBar.visibility = visibility
        rangeLabel.visibility = visibility
        if (range != null) {
            // max first: setMin() clamps against the current max (default 100)
            seekBar.max = range.last
            seekBar.min = range.first
            rangeLabel.text = context.getString(R.string.preference_int_range, range.first, range.last)
        }
    }

    fun setValue(value: Int?) = apply {
        value ?: return@apply
        showInInput(value)
        if (range?.contains(value) == true) { seekBar.progress = value }
    }

    fun setOnConfirmClicked(onConfirm: (Int) -> Unit) = apply {
        this.onConfirm = onConfirm
    }

    fun show() {
        val dialog = builder.show()
        // Enter on a physical keyboard (or Done on the soft one) confirms, like the OK button
        input.setOnEditorActionListener { _, actionId, event ->
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (actionId == EditorInfo.IME_ACTION_DONE || isEnter) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                true
            } else {
                false
            }
        }
        input.requestFocus()
    }

    private fun showInInput(value: Int) {
        input.setText(value.toString())
        input.setSelection(input.text.length)
    }

    private fun confirm() {
        context.getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(input.windowToken, 0)
        val value = input.text.toString().toIntOrNull() ?: return
        onConfirm(range?.let(value::coerceIn) ?: value)
    }
}
