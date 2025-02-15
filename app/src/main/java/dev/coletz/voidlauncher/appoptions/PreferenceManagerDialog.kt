package dev.coletz.voidlauncher.appoptions

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.*
import dev.coletz.voidlauncher.R
import dev.coletz.voidlauncher.models.Preference
import dev.coletz.voidlauncher.models.support.PersistableEnum
import dev.coletz.voidlauncher.views.InputTextDialog
import kotlin.enums.EnumEntries

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
            val inputTextDialog = InputTextDialog(context)
                .setTitle(pref.info.name)
                .setOnConfirmClicked { onPreferenceUpdatedListener(pref.copy(rawValue = it)) }

            when (pref.info.type){
                Int::class -> {
                    inputTextDialog
                        .customizeInput {
                            it.setText(pref.rawValue)
                            it.inputType = InputType.TYPE_CLASS_NUMBER
                        }
                }
                PersistableEnum::class -> {
                    inputTextDialog
                        .customizeInput {
                            val adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_dropdown_item_1line,
                                pref.info.possibleValue as? List<String> ?: emptyList()
                            )
                            it.setAdapter(adapter)

                            it.setText(pref.rawValue)
                        }
                }
            }

            inputTextDialog.show()
        }

        builder = AlertDialog.Builder(context).apply {
            setTitle("Preference Manager")
            setView(rootView)
            setPositiveButton(R.string.close_label, null)
        }
    }

    private fun Context.bindViews() {
        rootView = LayoutInflater.from(this).inflate(R.layout.preference_manager_dialog, null)
        with(rootView) {
            recyclerView = findViewById(R.id.recycler_view)
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
            Holder(LayoutInflater.from(parent.context).inflate(R.layout.preference_manager_item, parent, false))

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class Holder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
            private lateinit var item: Preference.Entity

            private val itemLabel: TextView = itemView.findViewById(R.id.item_label)

            init {
                itemView.setOnClickListener(this)
            }

            fun bind(item: Preference.Entity) {
                this.item = item
                itemLabel.text = item.info.name
            }

            override fun onClick(v: View?) {
                onClickListener(item)
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