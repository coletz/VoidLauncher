package dev.coletz.voidlauncher.views

import android.content.Context
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.content.getSystemService
import dev.coletz.voidlauncher.core.R

class InputTextDialog(context: Context) {

    private val rootView = LayoutInflater.from(context).inflate(R.layout.input_text_dialog, null)
    private val itemLabel: AppCompatAutoCompleteTextView = rootView.findViewById(R.id.item_label)
    private val builder: AlertDialog.Builder
    private var onConfirm: (String) -> Unit = {}
    private var allowBlank: Boolean = false

    init {
        builder = AlertDialog.Builder(context).apply {
            setView(rootView)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ ->
                context.getSystemService<InputMethodManager>()
                    ?.hideSoftInputFromWindow(itemLabel.windowToken, 0)

                val text = itemLabel.text.toString()
                if (allowBlank || text.isNotBlank()) { onConfirm(text) }
            }
        }
    }

    fun setTitle(title: String?) = apply {
        builder.setTitle(title)
    }

    fun customizeInput(block: (AppCompatAutoCompleteTextView) -> Unit) = apply {
        block(itemLabel)
    }

    fun setOnConfirmClicked(onConfirm: (String) -> Unit) = apply {
        this.onConfirm = onConfirm
    }

    /** By default a blank input is ignored. Set to true to forward it to the confirm callback. */
    fun setAllowBlank(allowBlank: Boolean) = apply {
        this.allowBlank = allowBlank
    }

    fun show() {
        builder.show().setWidthPercentage(.9f)
    }
}