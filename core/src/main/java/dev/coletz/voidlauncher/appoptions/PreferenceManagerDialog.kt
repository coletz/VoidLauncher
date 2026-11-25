package dev.coletz.voidlauncher.appoptions

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.*
import dev.coletz.voidlauncher.core.R
import dev.coletz.voidlauncher.models.Preference
import dev.coletz.voidlauncher.models.support.PersistableEnum
import dev.coletz.voidlauncher.views.InputTextDialog

class PreferenceManagerDialog(context: Context) {

    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView

    private val prefsListAdapter = PreferenceListAdapter()
    private val builder: AlertDialog.Builder

    private var onPreferenceUpdatedListener: (Preference.Entity) -> Unit = {}
    private var customPreferenceHandler: ((Preference.Entity, (String) -> Unit) -> Boolean)? = null

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

    fun setCustomPreferenceHandler(handler: (Preference.Entity, (String) -> Unit) -> Boolean) = apply {
        this.customPreferenceHandler = handler
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

        // Try custom handler first
        val handled = customPreferenceHandler?.invoke(preferenceEntity, onValueUpdated) == true
        if (handled) {
            return
        }

        when (preferenceEntity.info.type) {
            Boolean::class -> {
                val current = preferenceEntity.effectiveValue.toBoolean()
                onValueUpdated((!current).toString())
            }
            PersistableEnum::class -> showEnumPicker(preferenceEntity, onValueUpdated)
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

    private fun showTextInput(preferenceEntity: Preference.Entity, onValueUpdated: (String) -> Unit) {
        InputTextDialog(rootView.context)
            .setTitle(preferenceEntity.info.name)
            .setOnConfirmClicked(onValueUpdated)
            .customizeInput {
                it.setText(preferenceEntity.rawValue)
                if (preferenceEntity.info.type == Int::class) {
                    it.inputType = InputType.TYPE_CLASS_NUMBER
                }
            }
            .show()
    }

    private class PreferenceListAdapter : ListAdapter<Preference.Entity, PreferenceListAdapter.Holder>(Differ) {

        companion object {
            private const val VIEW_TYPE_TEXT = 0
            private const val VIEW_TYPE_BOOLEAN = 1
            private const val VIEW_TYPE_ENUM = 2
        }

        var onClickListener: (Preference.Entity) -> Unit = {}

        override fun getItemViewType(position: Int): Int = when (getItem(position).info.type) {
            Boolean::class -> VIEW_TYPE_BOOLEAN
            PersistableEnum::class -> VIEW_TYPE_ENUM
            else -> VIEW_TYPE_TEXT
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                VIEW_TYPE_BOOLEAN -> BooleanHolder(inflater.inflate(R.layout.preference_manager_item_boolean, parent, false))
                VIEW_TYPE_ENUM -> EnumHolder(inflater.inflate(R.layout.preference_manager_item_enum, parent, false))
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

        inner class EnumHolder(view: View): Holder(view) {
            private val itemValue: TextView = itemView.findViewById(R.id.item_value)

            override fun bind(item: Preference.Entity) {
                super.bind(item)
                val value = item.effectiveValue
                itemValue.text = item.info.enumOptions.firstOrNull { it.id == value }?.label
                    ?: itemView.context.getString(R.string.preference_not_set)
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