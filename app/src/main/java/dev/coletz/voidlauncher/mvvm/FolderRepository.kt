package dev.coletz.voidlauncher.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dev.coletz.voidlauncher.models.AppEntity
import dev.coletz.voidlauncher.models.FolderEntity
import dev.coletz.voidlauncher.models.FoldersAppsCrossRef
import dev.coletz.voidlauncher.room.dao.FolderEntityDao
import dev.coletz.voidlauncher.room.entities.FolderWithApps

class FolderRepository(
    private val databaseFolderDao: FolderEntityDao,
) {

    fun getFoldersWithApps(): LiveData<List<FolderWithApps>> =
        databaseFolderDao.getFoldersWithApps()

    suspend fun addAppInFolder(app: AppEntity, folder: FolderEntity) {
        val folderId = databaseFolderDao.insert(folder)
        databaseFolderDao.insertFolderAppsCrossRef(FoldersAppsCrossRef(folderId, app.packageName))
    }

    suspend fun removeAppFromFolder(app: AppEntity, folder: FolderEntity) {
        databaseFolderDao.deleteFolderAppsCrossRef(FoldersAppsCrossRef(folder.folderId, app.packageName))
    }
}