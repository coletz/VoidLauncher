package dev.coletz.voidlauncher.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import dev.coletz.voidlauncher.models.FolderEntity
import dev.coletz.voidlauncher.models.FoldersAppsCrossRef
import dev.coletz.voidlauncher.room.entities.FolderWithApps

@Dao
interface FolderEntityDao {

    @Transaction
    @Query("SELECT * FROM folder_entity")
    fun getFoldersWithApps(): LiveData<List<FolderWithApps>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolderAppsCrossRef(foldersAppsCrossRef: FoldersAppsCrossRef): Long

    @Delete
    suspend fun deleteFolderAppsCrossRef(foldersAppsCrossRef: FoldersAppsCrossRef)

    @Delete
    suspend fun delete(folder: FolderEntity)
}