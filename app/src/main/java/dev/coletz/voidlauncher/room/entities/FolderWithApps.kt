package dev.coletz.voidlauncher.room.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import dev.coletz.voidlauncher.models.AppEntity
import dev.coletz.voidlauncher.models.FoldersAppsCrossRef
import dev.coletz.voidlauncher.models.FolderEntity

data class FolderWithApps(
    @Embedded val folder: FolderEntity,
    @Relation(
        parentColumn = "folder_id",
        entityColumn = "package_name",
        associateBy = Junction(FoldersAppsCrossRef::class)
    )
    val apps: List<AppEntity>
)