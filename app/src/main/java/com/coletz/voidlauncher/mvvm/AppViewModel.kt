package com.coletz.voidlauncher.mvvm

import android.app.Application
import androidx.lifecycle.*
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.models.AppWithTagEntity
import com.coletz.voidlauncher.models.TagEntity
import com.coletz.voidlauncher.room.TagEntityDao
import com.coletz.voidlauncher.room.VoidDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppViewModel(application: Application): AndroidViewModel(application){

    private val database: VoidDatabase = VoidDatabase.getDatabase(application)
    private val databaseAppDao = database.appEntityDao()
    private val databaseTagDao: TagEntityDao = database.tagEntityDao()
    private val packageManagerDao = PackageManagerDao.get(application)
    private val appRepo: AppRepository = AppRepository(packageManagerDao, databaseAppDao)
    private val tagRepo: TagRepository = TagRepository(databaseTagDao)

    private val allApps: LiveData<List<AppWithTagEntity>> = appRepo.getVisibleApps()
    val filter: MutableLiveData<String> = MutableLiveData()
    val apps = MediatorLiveData<List<AppEntity>>().apply {
        addSource(allApps) { value = it.filterAndSort(filter.value) }
        addSource(filter) { value = allApps.value.filterAndSort(it) }
    }

    fun updateApps() {
        viewModelScope.launch(Dispatchers.IO) {
            appRepo.updateApps()
        }
    }

    fun updateEditableName(packageName: String, editedName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepo.updateEditableName(packageName, editedName)
        }
    }

    fun hide(app: AppEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepo.hide(app)
            appRepo.updateApps()
        }
    }

    fun setFavorite(app: AppEntity, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepo.setFavorite(app, isFavorite)
            appRepo.updateApps()
        }
    }

    fun getAppTags(app: AppEntity): LiveData<List<TagEntity>> =
        tagRepo.getAppTags(app)

    fun insertTag(tag: TagEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            tagRepo.insertTag(tag)
            appRepo.updateApps()
        }
    }

    fun deleteTag(tag: TagEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            tagRepo.deleteTag(tag)
            appRepo.updateApps()
        }
    }

    private fun List<AppWithTagEntity>?.filterAndSort(filter: String?): List<AppEntity> {
        this ?: return emptyList()
        val trimmedFilter = filter?.trim() ?: ""
        val filterPredicate: (AppWithTagEntity) -> Boolean = { app ->
            if (trimmedFilter.isBlank()) {
                true
            } else {
                (app.tagName ?: app.uiName).split(" ").any { it.startsWith(trimmedFilter, ignoreCase = true) }
            }
        }

        return filter(filterPredicate).distinctBy { it.packageName }.sortedDescending()
    }
}