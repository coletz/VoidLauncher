package dev.coletz.voidlauncher.appoptions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.*
import dev.coletz.voidlauncher.core.R
import dev.coletz.voidlauncher.models.KeyCombination
import dev.coletz.voidlauncher.models.Preference
import dev.coletz.voidlauncher.models.support.PersistableEnum
import dev.coletz.voidlauncher.views.InputTextDialog
import dev.coletz.voidlauncher.views.IntInputDialog
import dev.coletz.voidlauncher.views.KeyCombinationRecordDialog

class PreferenceManagerDialog(context: Context) {

    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView

    private val prefsListAdapter = PreferenceListAdapter()
    private val builder: AlertDialog.Builder

    private var onPreferenceUpdatedListener: (Preference.Entity) -> Unit = {}

    init {
        context.bindViews()

        recyclerView.adapter = prefsListAdapter

        prefsListAdapter.onClickListener = { pref ->
            onPreferenceClicked(pref)
        }

        builder = AlertDialog.Builder(context).apply {
            setTitle("Preference Manager")
            setView(rootView)
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

    private fun Context.bindViews() {
        rootView = LayoutInflater.from(this).inflate(R.layout.preference_manager_dialog, null)
        with(rootView) {
            recyclerView = findViewById(R.id.recycler_view)
        }
    }

    private fun onPreferenceClicked(preferenceEntity: Preference.Entity) {
        val onValueUpdated: (String) -> Unit = { onPreferenceUpdatedListener(preferenceEntity.copy(rawValue = it)) }

        when (preferenceEntity.info.type) {
            Boolean::class -> {
                val current = preferenceEntity.effectiveValue.toBoolean()
                onValueUpdated((!current).toString())
            }
            PersistableEnum::class -> showEnumPicker(preferenceEntity, onValueUpdated)
            Int::class -> showIntInput(preferenceEntity, onValueUpdated)
            KeyCombination::class -> showKeyCombinationRecorder(preferenceEntity, onValueUpdated)
            else -> showTextInput(preferenceEntity, onValueUpdated)
        }
    }

    private fun showEnumPicker(preferenceEntity: Preference.Entity, onValueUpdated: (String) -> Unit) {
        val options = preferenceEntity.info.enumOptions
        val checkedIndex = options.indexOfFirst { it.id == preferenceEntity.effectiveValue }

        AlertDialog.Builder(rootView.context)
            .setTitle(preferenceEntity.info.name)
            .setSingleChoiceItems(options.map { it.label }.toTypedArray(), checkedIndex) { dialog, which ->
                onValueUpdated(options[which].id)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showIntInput(preferenceEntity: Preference.Entity, onValueUpdated: (String) -> Unit) {
        val info = preferenceEntity.info
        IntInputDialog(rootView.context)
            .setTitle(info.name)
            .setRange(info.intRange)
            .setValue(preferenceEntity.effectiveValue?.toIntOrNull())
            .setOnConfirmClicked { onValueUpdated(it.toString()) }
            .show()
    }

    private fun showKeyCombinationRecorder(preferenceEntity: Preference.Entity, onValueUpdated: (String) -> Unit) {
        val info = preferenceEntity.info
        KeyCombinationRecordDialog(rootView.context)
            .setTitle(info.name)
            .setCurrentCombination(preferenceEntity.effectiveValue?.let(KeyCombination::deserialize))
            .setDefaultCombination(info.defaultValue?.let(KeyCombination::deserialize))
            .setOnConfirmClicked { onValueUpdated(it.serialize()) }
            .show()
    }

    private fun showTextInput(preferenceEntity: Preference.Entity, onValueUpdated: (String) -> Unit) {
        InputTextDialog(rootView.context)
            .setTitle(preferenceEntity.info.name)
            .setOnConfirmClicked(onValueUpdated)
            .customizeInput { it.setText(preferenceEntity.rawValue) }
            .show()
    }

    private class PreferenceListAdapter : ListAdapter<Preference.Entity, PreferenceListAdapter.Holder>(Differ) {

        companion object {
            private const val VIEW_TYPE_TEXT = 0
            private const val VIEW_TYPE_BOOLEAN = 1
            private const val VIEW_TYPE_VALUE = 2
        }

        var onClickListener: (Preference.Entity) -> Unit = {}

        override fun getItemViewType(position: Int): Int = when (getItem(position).info.type) {
            Boolean::class -> VIEW_TYPE_BOOLEAN
            PersistableEnum::class, Int::class, KeyCombination::class -> VIEW_TYPE_VALUE
            else -> VIEW_TYPE_TEXT
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                VIEW_TYPE_BOOLEAN -> BooleanHolder(inflater.inflate(R.layout.preference_manager_item_boolean, parent, false))
                VIEW_TYPE_VALUE -> ValueHolder(inflater.inflate(R.layout.preference_manager_item_value, parent, false))
                else -> TextHolder(inflater.inflate(R.layout.preference_manager_item, parent, false))
            }
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position))
        }

        abstract inner class Holder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
            protected lateinit var item: Preference.Entity

            private val itemLabel: TextView = itemView.findViewById(R.id.item_label)

            init {
                itemView.setOnClickListener(this)
            }

            open fun bind(item: Preference.Entity) {
                this.item = item
                itemLabel.text = item.info.name
            }

            override fun onClick(v: View?) {
                onClickListener(item)
            }
        }

        inner class TextHolder(view: View): Holder(view)

        inner class BooleanHolder(view: View): Holder(view) {
            private val itemSwitch: SwitchCompat = itemView.findViewById(R.id.item_switch)

            override fun bind(item: Preference.Entity) {
                super.bind(item)
                itemSwitch.isChecked = item.effectiveValue.toBoolean()
            }
        }

        /** Label with the current value underneath. */
        inner class ValueHolder(view: View): Holder(view) {
            private val itemValue: TextView = itemView.findViewById(R.id.item_value)

            override fun bind(item: Preference.Entity) {
                super.bind(item)
                itemValue.text = item.displayValue() ?: itemView.context.getString(R.string.preference_not_set)
            }

            private fun Preference.Entity.displayValue(): String? {
                val value = effectiveValue
                return when (info.type) {
                    PersistableEnum::class -> info.enumOptions.firstOrNull { it.id == value }?.label
                    Int::class -> value?.toIntOrNull()?.let { int -> info.intRange?.let(int::coerceIn) ?: int }?.toString()
                    KeyCombination::class -> value?.let(KeyCombination::deserialize)?.toDisplayString()
                    else -> value
                }
            }
        }

        private object Differ: DiffUtil.ItemCallback<Preference.Entity>() {

            override fun areItemsTheSame(oldItem: Preference.Entity, newItem: Preference.Entity): Boolean {
                return oldItem.info.key == newItem.info.key
            }

            override fun areContentsTheSame(oldItem: Preference.Entity, newItem: Preference.Entity): Boolean {
                return oldItem == newItem
            }
        }
    }
}