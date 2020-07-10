package com.coletz.voidlauncher.mvvm

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.annotation.WorkerThread
import androidx.core.content.getSystemService
import androidx.lifecycle.MutableLiveData
import com.coletz.voidlauncher.models.AppObject
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.suspendCoroutine

class AppDao private constructor(context: Context) {
    private val launcher = context.getSystemService<LauncherApps>()
    private val userManager = context.getSystemService<UserManager>()

    val apps: MutableLiveData<List<AppObject>> = MutableLiveData()

    private var retry = 0

    suspend fun loadApps() = coroutineScope {
        userManager ?: run { INSTANCE = null; return@coroutineScope }
        launcher ?: run { INSTANCE = null; return@coroutineScope }
        retry = 0


        userManager
            .userProfiles
            .flatMap { launcher.getActivityList(null, it) }
            .map { AppObject(it.label.toString(), it.applicationInfo.packageName, false) }
            .sortedDescending()
            .run { apps.postValue(this) }
    }


    companion object {
        @Volatile
        private var INSTANCE: AppDao? = null

        fun get(context: Context): AppDao {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                return AppDao(context.applicationContext).apply {
                    INSTANCE = this
                }
            }
        }
    }
}