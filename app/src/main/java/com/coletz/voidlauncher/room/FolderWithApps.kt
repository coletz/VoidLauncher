package com.coletz.voidlauncher.room

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.models.FolderEntity

data class FolderWithApps(
    @Embedded val folder: FolderEntity,
    @Relation(
        parentColumn = "folder_id",
        entityColumn = "package_name",
        associateBy = Junction(FolderAppsCrossRef::class)
    )
    val apps: List<AppEntity>
)