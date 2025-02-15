package dev.coletz.voidlauncher.appoptions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.*
import dev.coletz.voidlauncher.R
import dev.coletz.voidlauncher.models.AppEntity
import dev.coletz.voidlauncher.models.TagEntity
import dev.coletz.voidlauncher.views.InputTextDialog


class TagManagerDialog(context: Context) {

    private lateinit var app: AppEntity

    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var createTagBtn: View

    private val tagListAdapter = TagListAdapter()
    private val builder: AlertDialog.Builder

    private var onTagCreatedListener: (TagEntity) -> Unit = {}
    private var onTagDeletedListener: (TagEntity) -> Unit = {}
    private var onDialogShown: () -> Unit = {}
    private var onDialogDismissed: () -> Unit = {}

    init {
        context.bindViews()
        recyclerView.adapter = tagListAdapter

        tagListAdapter.onLongClickListener = { onTagDeletedListener(it) }

        createTagBtn.setOnClickListener {
            InputTextDialog(context)
                .setTitle("New tag:")
                .setOnConfirmClicked { onTagCreatedListener(TagEntity(app.packageName, it)) }
                .show()
        }

        builder = AlertDialog.Builder(context).apply {
            setTitle("Tag Manager")
            setView(rootView)
            setPositiveButton(R.string.close_label, null)
        }
    }

    private fun Context.bindViews() {
        rootView = LayoutInflater.from(this).inflate(R.layout.tag_manager_dialog, null)
        with(rootView) {
            recyclerView = findViewById(R.id.recycler_view)
            createTagBtn = findViewById(R.id.create_tag_btn)
        }
    }

    fun setApp(app: AppEntity) = apply {
        this.app = app
    }

    fun loadTags(tags: List<TagEntity>) = apply {
        tagListAdapter.submitList(tags)
    }

    fun setOnTagCreatedListener(onTagCreatedListener: (TagEntity) -> Unit) = apply {
        this.onTagCreatedListener = onTagCreatedListener
    }

    fun setOnTagDeletedListener(onTagDeletedListener: (TagEntity) -> Unit) = apply {
        this.onTagDeletedListener = onTagDeletedListener
    }

    fun setOnDialogShown(onDialogShown: () -> Unit) = apply {
        this.onDialogShown = onDialogShown
    }

    fun setOnDialogDismissed(onDialogDismissed: () -> Unit) = apply {
        this.onDialogDismissed = onDialogDismissed
    }

    fun show() = apply {
        builder.create()
            .also {
                it.setOnShowListener { onDialogShown() }
                it.setOnDismissListener { onDialogDismissed() }
                it.setOnCancelListener { onDialogDismissed() }
            }.show()
    }

    private class TagListAdapter : ListAdapter<TagEntity, TagListAdapter.Holder>(Differ) {

        var onLongClickListener: (TagEntity) -> Unit = {}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
            Holder(LayoutInflater.from(parent.context).inflate(R.layout.tag_manager_item, parent, false))

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class Holder(view: View): RecyclerView.ViewHolder(view), View.OnLongClickListener {
            private lateinit var item: TagEntity

            private val itemLabel: TextView = itemView.findViewById(R.id.item_label)

            init {
                itemView.setOnLongClickListener(this)
            }

            fun bind(item: TagEntity) {
                this.item = item
                itemLabel.text = item.tagName
            }

            override fun onLongClick(v: View?): Boolean {
                onLongClickListener(item)
                return true
            }
        }

        private object Differ: DiffUtil.ItemCallback<TagEntity>() {

            override fun areContentsTheSame(oldItem: TagEntity, newItem: TagEntity): Boolean {
                return oldItem.tagName == newItem.tagName
            }

            override fun areItemsTheSame(oldItem: TagEntity, newItem: TagEntity): Boolean {
                return oldItem.tagName == newItem.tagName && oldItem.packageName == newItem.packageName
            }
        }
    }
}