package com.coletz.voidlauncher.appoptions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.*
import com.coletz.voidlauncher.R
import com.coletz.voidlauncher.databinding.TagManagerDialogBinding
import com.coletz.voidlauncher.databinding.TagManagerItemBinding
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.models.TagEntity
import com.coletz.voidlauncher.views.InputTextDialog


class TagManagerDialog(context: Context) {

    private lateinit var app: AppEntity

    private val tagListAdapter = TagListAdapter()
    private val builder: AlertDialog.Builder

    private var onTagCreatedListener: (TagEntity) -> Unit = {}
    private var onTagDeletedListener: (TagEntity) -> Unit = {}
    private var onDialogShown: () -> Unit = {}
    private var onDialogDismissed: () -> Unit = {}

    init {
        val binding = TagManagerDialogBinding.inflate(LayoutInflater.from(context))
        binding.recyclerView.adapter = tagListAdapter

        tagListAdapter.onLongClickListener = { onTagDeletedListener(it) }

        binding.createTagBtn.setOnClickListener {
            InputTextDialog(context)
                .setTitle("New tag:")
                .setOnConfirmClicked { onTagCreatedListener(TagEntity(app.packageName, it)) }
                .show()
        }

        builder = AlertDialog.Builder(context).apply {
            setTitle("Tag Manager")
            setView(binding.root)
            setPositiveButton(R.string.close_label, null)
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
            Holder(TagManagerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class Holder(private val binding: TagManagerItemBinding): RecyclerView.ViewHolder(binding.root),
            View.OnLongClickListener {
            private lateinit var item: TagEntity

            fun bind(item: TagEntity) {
                this.item = item
                binding.itemLabel.text = item.tagName
            }

            init {
                itemView.setOnLongClickListener(this)
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