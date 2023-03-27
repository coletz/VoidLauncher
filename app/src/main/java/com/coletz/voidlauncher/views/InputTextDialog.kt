package com.coletz.voidlauncher.views

import android.content.Context
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import com.coletz.voidlauncher.databinding.InputTextDialogBinding

class InputTextDialog(context: Context) {

    private val binding = InputTextDialogBinding.inflate(LayoutInflater.from(context))
    private val builder: AlertDialog.Builder
    private var onConfirm: (String) -> Unit = {}

    init {
        builder = AlertDialog.Builder(context).apply {
            setView(binding.root)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ ->
                context.getSystemService<InputMethodManager>()
                    ?.hideSoftInputFromWindow(binding.itemLabel.windowToken, 0)

                val text = binding.itemLabel.text.toString()
                if (text.isNotBlank()) { onConfirm(text) }
            }
        }
    }

    fun setTitle(title: String?) = apply {
        builder.setTitle(title)
    }

    fun customizeInput(block: (EditText) -> Unit) = apply {
        block(binding.itemLabel)
    }

    fun setOnConfirmClicked(onConfirm: (String) -> Unit) = apply {
        this.onConfirm = onConfirm
    }

    fun show() {
        builder.show()
    }
}