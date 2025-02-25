package dev.coletz.voidlauncher.appoptions

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import dev.coletz.voidlauncher.R
import dev.coletz.voidlauncher.models.AppEntity
import dev.coletz.voidlauncher.mvvm.AppViewModel
import dev.coletz.voidlauncher.views.InputTextDialog
import dev.coletz.voidlauncher.views.multiActionDialog


class AppOptionMenu internal constructor(
    uniqueId: String,
    registry: ActivityResultRegistry,
    private val appViewModel: AppViewModel
) {

    interface Provider {
        val appOptionMenu: AppOptionMenu
    }

    private val appUninstallLauncher = registry.register(uniqueId, ActivityResultContracts.StartActivityForResult()) {
        appViewModel.updateApps()
    }

    fun open(fragment: Fragment, app: AppEntity) {
        val context: Context = fragment.requireContext()
        val lifecycleOwner: LifecycleOwner = fragment.viewLifecycleOwner

        val onUninstallClicked: () -> Unit = {
            val packageUri = "package:${app.packageName}".toUri()
            val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
            appUninstallLauncher.launch(uninstallIntent)
        }

        val openAppRenameDialog: () -> Unit = {
            InputTextDialog(context)
                .setTitle("Real name: ${app.officialName}\nPackage: ${app.packageName}")
                .customizeInput { it.setText(app.editedName) }
                .setOnConfirmClicked { appViewModel.updateEditableName(app.packageName, editedName = it) }
                .show()
        }

        val openTagManagerDialog: () -> Unit = {
            val tagsLiveData = appViewModel.getAppTags(app)

            val dialog = TagManagerDialog(context)
            dialog
                .setApp(app)
                .setOnTagCreatedListener { appViewModel.insertTag(it) }
                .setOnTagDeletedListener { appViewModel.deleteTag(it) }
                .setOnDialogShown { tagsLiveData.observe(lifecycleOwner) { dialog.loadTags(it) } }
                .setOnDialogDismissed { tagsLiveData.removeObservers(lifecycleOwner) }
                .show()
        }

        val openFolderManagerDialog: () -> Unit = {
            val foldersLiveData = appViewModel.getFoldersWithApps()

            val dialog = FolderManagerDialog(context)
            dialog
                .setApp(app)
                .setOnFolderCreatedListener { app, folder -> appViewModel.addAppInFolder(app, folder) }
                .setOnFolderDeletedListener { app, folder -> appViewModel.removeAppFromFolder(app, folder) }
                .setOnDialogShown { foldersLiveData.observe(lifecycleOwner) { dialog.loadFolders(it) } }
                .setOnDialogDismissed { foldersLiveData.removeObservers(lifecycleOwner) }
                .show()
        }

        context.multiActionDialog {
            title = "${app.uiName} (${app.packageName})"
            add(R.string.rename_option_label) { openAppRenameDialog() }
            add(R.string.folder_manager_option_label) { openFolderManagerDialog() }
            if (app.isFavorite) {
                add(R.string.unmark_as_favorite_option_label) { appViewModel.setFavorite(app, false) }
            } else {
                add(R.string.mark_as_favorite_option_label) { appViewModel.setFavorite(app, true) }
            }
            add(R.string.tag_manager_option_label) { openTagManagerDialog() }
            add(R.string.uninstall_option_label) { onUninstallClicked() }
            add(R.string.hide_option_label) { appViewModel.hide(app) }
        }
    }
}

fun ComponentActivity.createAppOptionMenu(appViewModel: AppViewModel) = AppOptionMenu(
    this::class.java.toString(),
    activityResultRegistry,
    appViewModel
)