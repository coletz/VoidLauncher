package com.coletz.voidlauncher.mvvm

import android.app.Application
import androidx.lifecycle.*
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.room.VoidDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppViewModel(application: Application): AndroidViewModel(application){

    private val database: VoidDatabase = VoidDatabase.getDatabase(application)
    private val databaseAppDao = database.appEntityDao()
    private val packageManagerDao = PackageManagerDao.get(application)
    private val repo: AppRepository = AppRepository(packageManagerDao, databaseAppDao)

    private val allApps: LiveData<List<AppEntity>> = repo.getNotHidden()
    val filter: MutableLiveData<String> = MutableLiveData()
    val apps = MediatorLiveData<List<AppEntity>>().apply {
        addSource(allApps) { value = it.filterAndSort(filter.value) }
        addSource(filter) { value = allApps.value.filterAndSort(it) }
    }

    fun updateApps() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateApps()
        }
    }

    fun update(app: AppEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.update(app)
        }
    }

    fun hide(app: AppEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.hide(app)
            repo.updateApps()
        }
    }

    fun setFavorite(app: AppEntity, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.setFavorite(app, isFavorite)
            repo.updateApps()
        }
    }

    private fun List<AppEntity>?.filterAndSort(filter: String?): List<AppEntity> {
        this ?: return emptyList()
        val trimmedFilter = filter?.trim() ?: ""
        val filterPredicate: (AppEntity) -> Boolean = { app ->
            if (trimmedFilter.isBlank()) {
                true
            } else {
                app.uiName.split(" ").any { it.startsWith(trimmedFilter, ignoreCase = true) }
            }
        }

        return filter(filterPredicate).sortedDescending()
    }
}