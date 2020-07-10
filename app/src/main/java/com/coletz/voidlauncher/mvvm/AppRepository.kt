package com.coletz.voidlauncher.mvvm

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.coletz.voidlauncher.models.AppObject

class AppRepository(private val dao: AppDao) {
    val apps: LiveData<List<AppObject>> = dao.apps

    @WorkerThread
    suspend fun loadApps() = dao.loadApps()
}