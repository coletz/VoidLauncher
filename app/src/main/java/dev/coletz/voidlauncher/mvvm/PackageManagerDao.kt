package dev.coletz.voidlauncher.mvvm

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.core.content.getSystemService
import dev.coletz.voidlauncher.models.AppEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PackageManagerDao private constructor(context: Context) {
    private val ownPackageName = context.applicationContext.packageName
    private val launcher: LauncherApps? by lazy { context.getSystemService<LauncherApps>() }
    private val userManager: UserManager? by lazy { context.getSystemService<UserManager>() }

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
                }
            }
        }
    }
}