package com.coletz.voidlauncher.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.coletz.voidlauncher.models.FolderEntity

@Dao
interface FolderEntityDao {

    @Transaction
    @Query("SELECT * FROM folder")
    fun getFoldersWithApps(): LiveData<List<FolderWithApps>>

    @Transaction
    @Query("SELECT * FROM app_entity WHERE package_name = :packageName")
    fun getFoldersByPackageName(packageName: String): LiveData<List<AppWithFolders>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolderAppsCrossRef(folderAppsCrossRef: FolderAppsCrossRef): Long

    @Delete
    suspend fun deleteFolderAppsCrossRef(folderAppsCrossRef: FolderAppsCrossRef)

    @Delete
    suspend fun delete(folder: FolderEntity)
}