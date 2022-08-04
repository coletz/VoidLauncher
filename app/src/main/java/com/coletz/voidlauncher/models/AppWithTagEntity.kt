package com.coletz.voidlauncher.models

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "app_entity")
class AppWithTagEntity(
    packageName: String,
    officialName: String,
    editedName: String?,
    isIntent: Boolean,
    isHidden: Boolean,
    isFavorite: Boolean,
    @ColumnInfo(name = "tag_name") val tagName: String? = null
): AppEntity(packageName, officialName, editedName, isIntent, isHidden, isFavorite)