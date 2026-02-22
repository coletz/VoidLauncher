package dev.coletz.voidlauncher.mvvm

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import dev.coletz.voidlauncher.models.AppEntity
import dev.coletz.voidlauncher.models.FolderEntity
import dev.coletz.voidlauncher.models.TagEntity
import dev.coletz.voidlauncher.room.*
import dev.coletz.voidlauncher.room.dao.FolderEntityDao
import dev.coletz.voidlauncher.room.dao.TagEntityDao
import dev.coletz.voidlauncher.room.entities.AppWithTagAndFolder
import dev.coletz.voidlauncher.room.entities.FolderWithApps
import dev.coletz.voidlauncher.views.AppUiItem
import dev.coletz.voidlauncher.views.FolderUiItem
import dev.coletz.voidlauncher.views.MainListUiItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application): AndroidViewModel(application){

    private val database: VoidDatabase = VoidDatabase.getDatabase(application)
    private val databaseAppDao = database.appEntityDao()
    private val databaseTagDao: TagEntityDao = database.tagEntityDao()
    private val databaseFolderDao: FolderEntityDao = database.folderEntityDao()

    private val packageManagerDao = PackageManagerDao.get(application)
    private val appRepo: AppRepository = AppRepository(packageManagerDao, databaseAppDao)
    private val tagRepo: TagRepository = TagRepository(databaseTagDao)
    private val folderRepo: FolderRepository = FolderRepository(databaseFolderDao)

    private val allApps = appRepo.getVisibleAppsWithTagsAndFolder()
    private val expandedFolderIds: MutableLiveData<List<Long>> = MutableLiveData(listOf())
    val filter: MutableLiveData<String> = MutableLiveData()
    var showAllOnBlankFilter: Boolean = true
    val apps = MediatorLiveData<List<MainListUiItem>>().apply {
        addSource(allApps) { value = it.mapFilterAndSort(filter.value, expandedFolderIds.value) }
        addSource(filter) { value = allApps.value.mapFilterAndSort(it, expandedFolderIds.value) }
        addSource(expandedFolderIds) { value = allApps.value.mapFilterAndSort(filter.value, it) }
    }
    val singleApp: LiveData<AppUiItem?> = apps.map {
        it
            ?.takeIf { it.size == 1 }
            ?.filterIsInstance<AppUiItem>()
            ?.first()
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

    fun getFoldersWithApps(): LiveData<List<FolderWithApps>> =
        folderRepo.getFoldersWithApps()

    fun addAppInFolder(app: AppEntity, folder: FolderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            folderRepo.addAppInFolder(app, folder)
            appRepo.updateApps()
        }
    }

    fun removeAppFromFolder(app: AppEntity, folder: FolderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            folderRepo.removeAppFromFolder(app, folder)
            appRepo.updateApps()
        }
    }

    private fun List<AppWithTagAndFolder>?.mapFilterAndSort(filter: String?, expandedFolderIds: List<Long>? = null): List<MainListUiItem> {
        this ?: return emptyList()
        val trimmedFilter = filter?.trim() ?: ""
        val filterPredicate: (MainListUiItem) -> Boolean = { item ->
            if (trimmedFilter.isBlank()) {
                showAllOnBlankFilter
            } else {
                item.tags.plus(item.uiName).any { name -> name.split(" ").any { word -> word.startsWith(trimmedFilter, ignoreCase = true) } }
            }
        }

        return toUiItem(expandedFolderIds ?: listOf()).filter(filterPredicate).distinctBy { it.identifier }
    }

    private fun List<AppWithTagAndFolder>.toUiItem(expandedFolderIds: List<Long>): List<MainListUiItem> =
        groupBy { it.folder }.flatMap { (folder, apps) ->
            val appsWithTag = apps.groupBy { it.app }.map { (app, apps) ->
                AppUiItem(
                    uiIdentifier = app.packageName,
                    uiName = app.uiName,
                    isFavorite = app.isFavorite,
                    isHidden = app.isHidden,
                    tags = apps.mapNotNull { it.tagName }
                )
            }
            if (folder != null) {
                val folderUiItem = FolderUiItem(
                    folderId = folder.folderId,
                    uiName = folder.name,
                    appsWithTag,
                    folder.folderId in expandedFolderIds
                )
                mutableListOf<MainListUiItem>(folderUiItem).apply { if (folderUiItem.isExpanded) { addAll(folderUiItem.apps) } }.sortedDescending()
            } else {
                appsWithTag.sortedDescending()
            }
        }

    fun guessApp(queryStrings: List<String>, onResult: (AppUiItem?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val validQueries = queryStrings.filter { it.isNotBlank() }
            if (validQueries.isEmpty()) { return@launch }
            validQueries.forEach { queryString ->
                allApps.value
                    ?.mapFilterAndSort(queryString)
                    ?.filterIsInstance<AppUiItem>()
                    ?.takeIf { it.size == 1 }
                    ?.let {
                        withContext(Dispatchers.Main) { onResult(it.first()) }
                        return@launch
                    }
            }
            withContext(Dispatchers.Main) { onResult(null) }
        }
    }

    fun getAppByPackageName(packageName: String, onSuccess: (AppEntity) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = appRepo.getAppByPackageName(packageName)
            withContext(Dispatchers.Main) { onSuccess(app) }
        }
    }

    fun launch(packageName: String) {
        getAppByPackageName(packageName) { app ->
            app.launch(getApplication(), onError = {
                Log.e("AppViewModel", "Error launching app", it)
                Toast.makeText(getApplication(), "Error launching app", Toast.LENGTH_LONG).show()
                updateApps()
            })
        }
    }

    fun toggleFolder(folder: FolderUiItem) {
        viewModelScope.launch(Dispatchers.IO) {
            if (folder.isExpanded) {
                Log.e("AppViewModel", "Collapse")
                expandedFolderIds.postValue(expandedFolderIds.value?.minus(folder.folderId))
            } else {
                Log.e("AppViewModel", "Expand")
                expandedFolderIds.postValue(expandedFolderIds.value?.plus(folder.folderId))
            }
        }
    }
}