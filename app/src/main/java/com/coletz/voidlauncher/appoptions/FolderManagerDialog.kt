package com.coletz.voidlauncher.appoptions

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.*
import com.coletz.voidlauncher.R
import com.coletz.voidlauncher.databinding.FolderManagerDialogBinding
import com.coletz.voidlauncher.databinding.FolderManagerItemBinding
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.models.FolderEntity
import com.coletz.voidlauncher.room.FolderWithApps
import com.coletz.voidlauncher.views.InputTextDialog


class FolderManagerDialog(context: Context) {

    private lateinit var app: AppEntity

    private val folderListAdapter = FolderListAdapter()
    private val builder: AlertDialog.Builder

    private var onAppAddedToFolder: (AppEntity, FolderEntity) -> Unit = { _, _ -> }
    private var onAppRemovedFromFolderListener: (AppEntity, FolderEntity) -> Unit = { _, _ -> }
    private var onDialogShown: () -> Unit = {}
    private var onDialogDismissed: () -> Unit = {}

    init {
        val binding = FolderManagerDialogBinding.inflate(LayoutInflater.from(context))
        binding.recyclerView.adapter = folderListAdapter

        folderListAdapter.onAppChecked = { folder, isChecked ->
            if (isChecked) {
                onAppAddedToFolder(app, folder)
            } else {
                onAppRemovedFromFolderListener(app, folder)
            }
        }

        binding.createFolderBtn.setOnClickListener {
            InputTextDialog(context)
                .setTitle("New folder:")
                .setOnConfirmClicked { onAppAddedToFolder(app, FolderEntity(name = it)) }
                .show()
        }

        builder = AlertDialog.Builder(context).apply {
            setTitle("Folder Manager")
            setView(binding.root)
            setPositiveButton(R.string.close_label, null)
        }
    }

    fun setApp(app: AppEntity) = apply {
        this.app = app
    }

    fun loadFolders(folders: List<FolderWithApps>) = apply {
        folderListAdapter.submitList(folders)
    }

    fun setOnFolderCreatedListener(onFolderCreatedListener: (AppEntity, FolderEntity) -> Unit) = apply {
        this.onAppAddedToFolder = onFolderCreatedListener
    }

    fun setOnFolderDeletedListener(onFolderDeletedListener: (AppEntity, FolderEntity) -> Unit) = apply {
        this.onAppRemovedFromFolderListener = onFolderDeletedListener
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

    private inner class FolderListAdapter : ListAdapter<FolderWithApps, FolderListAdapter.Holder>(Differ) {

        var onAppChecked: (FolderEntity, Boolean) -> Unit = {_, _ ->}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
            Holder(FolderManagerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class Holder(private val binding: FolderManagerItemBinding): RecyclerView.ViewHolder(binding.root),
            CompoundButton.OnCheckedChangeListener {
            private lateinit var item: FolderWithApps

            fun bind(item: FolderWithApps) {
                this.item = item
                binding.itemLabel.text = item.folder.name
                binding.itemLabel.isChecked = app in item.apps
            }

            init {
                binding.itemLabel.setOnCheckedChangeListener(this)
            }

            override fun onCheckedChanged(v: CompoundButton?, isChecked: Boolean) {
                onAppChecked(item.folder, isChecked)
            }
        }

    }

    private object Differ: DiffUtil.ItemCallback<FolderWithApps>() {

        override fun areContentsTheSame(oldItem: FolderWithApps, newItem: FolderWithApps): Boolean {
            return oldItem.folder.name == newItem.folder.name &&
                    sameApps(oldItem.apps, newItem.apps)
        }

        override fun areItemsTheSame(oldItem: FolderWithApps, newItem: FolderWithApps): Boolean {
            return oldItem.folder.folderId == newItem.folder.folderId
        }

        private fun sameApps(oldApps: List<AppEntity>, newApps: List<AppEntity>): Boolean {
            if (oldApps.size != newApps.size) {
                return false
            }

            val sortedOld = oldApps.sortedBy { it.packageName }
            val sortedNew = newApps.sortedBy { it.packageName }
            sortedOld.indices.forEach {
                if (sortedOld[it].packageName != sortedNew[it].packageName) {
                    return false
                }
            }

            return true
        }
    }
}