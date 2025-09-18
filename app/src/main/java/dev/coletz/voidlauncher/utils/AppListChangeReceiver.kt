package dev.coletz.voidlauncher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import dev.coletz.voidlauncher.mvvm.AppRepository
import dev.coletz.voidlauncher.mvvm.PackageManagerDao
import dev.coletz.voidlauncher.room.VoidDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppListChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        when (action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            "com.android.launcher.action.INSTALL_SHORTCUT",
            "com.android.launcher.action.UNINSTALL_SHORTCUT" -> {
                context?.applicationContext?.onAppListChanged()
            }
        }
    }

    private fun Context.onAppListChanged() {
        val database = VoidDatabase.getDatabase(this)
        val databaseAppDao = database.appEntityDao()
        val packageManagerDao = PackageManagerDao.get(this)
        val appRepo = AppRepository(packageManagerDao, databaseAppDao)

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            appRepo.updateApps()
        }
    }

    fun register(context: ContextWrapper) {
        val packagesFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }

        val shortcutsFilter = IntentFilter().apply {
            "com.android.launcher.action.INSTALL_SHORTCUT"
            "com.android.launcher.action.UNINSTALL_SHORTCUT"
        }

        context.registerReceiver(this, packagesFilter)
        context.registerReceiver(this, shortcutsFilter)
    }
}