package com.coletz.voidlauncher.room

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["folder_id", "package_name"])
data class FolderAppsCrossRef(
    @ColumnInfo(name = "folder_id") val folderId: Long,
    @ColumnInfo(name = "package_name") val packageName: String
)