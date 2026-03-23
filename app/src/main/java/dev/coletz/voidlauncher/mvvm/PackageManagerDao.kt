package dev.coletz.voidlauncher.mvvm

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.os.UserManager
import androidx.core.content.getSystemService
import dev.coletz.voidlauncher.models.AppEntity
import dev.coletz.voidlauncher.room.VoidDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PackageManagerDao private constructor(context: Context) {
    private val appContext: Context = context.applicationContext
    private val ownPackageName = appContext.packageName
    private val launcher: LauncherApps? by lazy { appContext.getSystemService<LauncherApps>() }
    private val userManager: UserManager? by lazy { appContext.getSystemService<UserManager>() }

    private val launcherAppsCallback = object : LauncherApps.Callback() {
        override fun onPackageAdded(packageName: String, user: UserHandle) = syncApps()
        override fun onPackageChanged(packageName: String, user: UserHandle) = syncApps()
        override fun onPackageRemoved(packageName: String, user: UserHandle) = syncApps()
        override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = syncApps()
        override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = syncApps()
    }

    fun startListening() {
        launcher?.registerCallback(launcherAppsCallback, Handler(Looper.getMainLooper()))
    }

    private fun syncApps() {
        val database = VoidDatabase.getDatabase(appContext)
        val appRepo = AppRepository(this, database.appEntityDao())
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            appRepo.updateApps()
        }
    }

    suspend fun getInstalledApps(): AppListResult {
        val userManager = userManager ?: run { INSTANCE = null; return AppListResult.MissingContext }
        val launcher = launcher ?: run { INSTANCE = null; return AppListResult.MissingContext }

        return withContext(Dispatchers.IO) {
            userManager
                .userProfiles
                .flatMap { launcher.getActivityList(null, it) }
                .map {
                    AppEntity(
                        it.applicationInfo.packageName,
                        it.label.toString()
                    )
                }
                .filter { it.packageName != ownPackageName }
                .let(AppListResult::Success)
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: PackageManagerDao? = null

        fun get(context: Context): PackageManagerDao {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                return PackageManagerDao(context.applicationContext).apply {
                    INSTANCE = this
                    startListening()
                }
            }
        }
    }
}