package com.coletz.voidlauncher.mvvm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.coletz.voidlauncher.models.AppObject
import com.coletz.voidlauncher.models.NotificationObject
import com.coletz.voidlauncher.room.VoidDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppViewModel(application: Application): AndroidViewModel(application){
    private val repo: AppRepository

    val apps: LiveData<List<AppObject>>

    init {
        val dao = AppDao.get(application)
        repo = AppRepository(dao)

        apps = repo.apps
    }

    fun loadApps() = viewModelScope.launch(Dispatchers.IO) {
        repo.loadApps()
    }
}