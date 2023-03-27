package com.coletz.voidlauncher.appoptions

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.*
import com.coletz.voidlauncher.R
import com.coletz.voidlauncher.databinding.PreferenceManagerDialogBinding
import com.coletz.voidlauncher.databinding.PreferenceManagerItemBinding
import com.coletz.voidlauncher.models.Preference
import com.coletz.voidlauncher.views.InputTextDialog

class PreferenceManagerDialog(context: Context) {

    private val prefsListAdapter = PreferenceListAdapter()
    private val builder: AlertDialog.Builder

    private var onPreferenceUpdatedListener: (Preference.Entity) -> Unit = {}

    init {
        val binding = PreferenceManagerDialogBinding.inflate(LayoutInflater.from(context))
        binding.recyclerView.adapter = prefsListAdapter

        prefsListAdapter.onClickListener = { pref ->
            val inputTextDialog = InputTextDialog(context)
                .setTitle(pref.key.name)
                .setOnConfirmClicked { onPreferenceUpdatedListener(pref.copy(value = it)) }

            when (pref.key.type){
                Preference.Type.INTEGER -> {
                    inputTextDialog
                        .customizeInput {
                            it.setText(pref.value?.toString())
                            it.inputType = InputType.TYPE_CLASS_NUMBER
                        }
                }
            }

            inputTextDialog.show()
        }

        builder = AlertDialog.Builder(context).apply {
            setTitle("Preference Manager")
            setView(binding.root)
            setPositiveButton(R.string.close_label, null)
        }
    }

    fun loadPreferences(preferences: List<Preference.Entity>) = apply {
        prefsListAdapter.submitList(preferences)
    }

    fun setOnPreferenceUpdatedListener(onPreferenceUpdatedListener: (Preference.Entity) -> Unit) = apply {
        this.onPreferenceUpdatedListener = onPreferenceUpdatedListener
    }

    fun show() = apply {
        builder.show()
    }

    private class PreferenceListAdapter : ListAdapter<Preference.Entity, PreferenceListAdapter.Holder>(Differ) {

        var onClickListener: (Preference.Entity) -> Unit = {}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
            Holder(PreferenceManagerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class Holder(private val binding: PreferenceManagerItemBinding): RecyclerView.ViewHolder(binding.root),
            View.OnClickListener {
            private lateinit var item: Preference.Entity

            fun bind(item: Preference.Entity) {
                this.item = item
                binding.itemLabel.text = item.key.name
            }

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                onClickListener(item)
            }
        }

        private object Differ: DiffUtil.ItemCallback<Preference.Entity>() {

            override fun areItemsTheSame(oldItem: Preference.Entity, newItem: Preference.Entity): Boolean {
                return oldItem.key.id == newItem.key.id
            }

            override fun areContentsTheSame(oldItem: Preference.Entity, newItem: Preference.Entity): Boolean {
                return oldItem == newItem
            }
        }
    }
}