package com.coletz.voidlauncher.room

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.coletz.voidlauncher.models.AppWithTagEntity
import com.coletz.voidlauncher.models.FolderEntity

data class AppWithFolders(
    @Embedded val app: AppWithTagEntity,
    @Relation(
        parentColumn = "package_name",
        entityColumn = "folder_id",
        associateBy = Junction(FolderAppsCrossRef::class)
    )
    val folders: List<FolderEntity>
)