package com.coletz.voidlauncher.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "folders_apps_cross_ref", primaryKeys = ["folder_id", "package_name"], indices = [Index("package_name")])
data class FoldersAppsCrossRef(
    @ColumnInfo(name = "folder_id") val folderId: Long,
    @ColumnInfo(name = "package_name") val packageName: String
)