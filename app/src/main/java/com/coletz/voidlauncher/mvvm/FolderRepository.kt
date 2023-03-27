package com.coletz.voidlauncher.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.models.FolderEntity
import com.coletz.voidlauncher.room.AppWithFolders
import com.coletz.voidlauncher.room.FolderAppsCrossRef
import com.coletz.voidlauncher.room.FolderEntityDao
import com.coletz.voidlauncher.room.FolderWithApps
import com.coletz.voidlauncher.utils.debug

class FolderRepository(
    private val databaseFolderDao: FolderEntityDao,
) {

    fun getFoldersWithApps(): LiveData<List<FolderWithApps>> =
        databaseFolderDao.getFoldersWithApps()

    fun getAppWithFolders(appEntity: AppEntity): LiveData<List<FolderEntity>> =
        databaseFolderDao.getFoldersByPackageName(appEntity.packageName).map {
            it.flatMap(AppWithFolders::folders)
        }

    suspend fun addAppInFolder(app: AppEntity, folder: FolderEntity) {
        val folderId = databaseFolderDao.insert(folder)
        folderId.debug("HERE IS THE NEW FOLDER ID")
        databaseFolderDao.insertFolderAppsCrossRef(FolderAppsCrossRef(folderId, app.packageName))
    }

    suspend fun removeAppFromFolder(app: AppEntity, folder: FolderEntity) {
        databaseFolderDao.deleteFolderAppsCrossRef(FolderAppsCrossRef(folder.folderId, app.packageName))
    }
}