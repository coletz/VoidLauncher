package dev.coletz.voidlauncher.appoptions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.*
import dev.coletz.voidlauncher.R
import dev.coletz.voidlauncher.models.AppEntity
import dev.coletz.voidlauncher.models.FolderEntity
import dev.coletz.voidlauncher.room.entities.FolderWithApps
import dev.coletz.voidlauncher.views.InputTextDialog


class FolderManagerDialog(context: Context) {

    private lateinit var app: AppEntity

    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var createFolderBtn: View

    private val folderListAdapter = FolderListAdapter()
    private val builder: AlertDialog.Builder

    private var onAppAddedToFolder: (AppEntity, FolderEntity) -> Unit = { _, _ -> }
    private var onAppRemovedFromFolderListener: (AppEntity, FolderEntity) -> Unit = { _, _ -> }
    private var onDialogShown: () -> Unit = {}
    private var onDialogDismissed: () -> Unit = {}

    init {
        context.bindViews()

        recyclerView.adapter = folderListAdapter

        folderListAdapter.onAppChecked = { folder, isChecked ->
            if (isChecked) {
                onAppAddedToFolder(app, folder)
            } else {
                onAppRemovedFromFolderListener(app, folder)
            }
        }

        createFolderBtn.setOnClickListener {
            InputTextDialog(context)
                .setTitle("New folder:")
                .setOnConfirmClicked { onAppAddedToFolder(app, FolderEntity(name = it)) }
                .show()
        }

        builder = AlertDialog.Builder(context).apply {
            setTitle("Folder Manager")
            setView(rootView)
            setPositiveButton(R.string.close_label, null)
        }
    }

    private fun Context.bindViews() {
        rootView = LayoutInflater.from(this).inflate(R.layout.folder_manager_dialog, null)
        with(rootView) {
            recyclerView = findViewById(R.id.recycler_view)
            createFolderBtn = findViewById(R.id.create_folder_btn)
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
            Holder(LayoutInflater.from(parent.context).inflate(R.layout.folder_manager_item, parent, false))

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class Holder(view: View): RecyclerView.ViewHolder(view), CompoundButton.OnCheckedChangeListener {
            private lateinit var item: FolderWithApps

            private val itemLabel: CheckBox = itemView.findViewById(R.id.item_label)

            init {
                itemLabel.setOnCheckedChangeListener(this)
            }

            fun bind(item: FolderWithApps) {
                this.item = item
                itemLabel.text = item.folder.name
                itemLabel.isChecked = app in item.apps
            }

            override fun onCheckedChanged(v: CompoundButton, isChecked: Boolean) {
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