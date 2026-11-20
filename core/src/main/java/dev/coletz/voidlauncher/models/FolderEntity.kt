package dev.coletz.voidlauncher.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folder_entity")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "folder_id") val folderId: Long = 0,
    @ColumnInfo(name = "name") val name: String
)