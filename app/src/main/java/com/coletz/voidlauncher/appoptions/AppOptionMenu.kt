package com.coletz.voidlauncher.appoptions

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.coletz.voidlauncher.R
import com.coletz.voidlauncher.databinding.AppRenameDialogBinding
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.mvvm.AppViewModel
import com.coletz.voidlauncher.utils.wip
import com.coletz.voidlauncher.views.multiActionDialog


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

    fun open(context: Context, app: AppEntity) {
        val onUninstallClicked: () -> Unit = {
            val packageUri = "package:${app.packageName}".toUri()
            val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
            appUninstallLauncher.launch(uninstallIntent)
        }

        context.multiActionDialog {
            title = "${app.uiName} (${app.packageName})"
            add(R.string.rename_option_label) { context.openAppRenameDialog(app) }
            add(R.string.add_to_folder_option_label) { context.wip() }
            if (app.isFavorite) {
                add(R.string.unmark_as_favorite_option_label) { appViewModel.setFavorite(app, false) }
            } else {
                add(R.string.mark_as_favorite_option_label) { appViewModel.setFavorite(app, true) }
            }
            add(R.string.uninstall_option_label) { onUninstallClicked() }
            add(R.string.hide_option_label) { appViewModel.hide(app) }
        }
    }

    private fun Context.openAppRenameDialog(app: AppEntity) {
        val binding = AppRenameDialogBinding.inflate(LayoutInflater.from(this))
        binding.itemLabel.setText(app.editedName)
        AlertDialog.Builder(this).apply {
            setTitle("Real name: ${app.officialName}\nPackage: ${app.packageName}")
            setView(binding.root)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ ->
                val newName = binding.itemLabel.text.toString()
                if (newName.isNotBlank()) {
                    appViewModel.update(app.copy(editedName = newName))
                }
            }
        }.show()
    }
}

fun Fragment.createAppOptionMenu(appViewModel: AppViewModel) = AppOptionMenu(
    id.toString(),
    requireActivity().activityResultRegistry,
    appViewModel
)

fun ComponentActivity.createAppOptionMenu(appViewModel: AppViewModel) = AppOptionMenu(
    this::class.java.toString(),
    activityResultRegistry,
    appViewModel
)