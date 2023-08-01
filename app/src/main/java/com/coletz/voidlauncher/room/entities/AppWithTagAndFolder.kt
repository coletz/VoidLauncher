package com.coletz.voidlauncher.room.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Ignore
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.models.FolderEntity
import com.coletz.voidlauncher.models.TagEntity

data class AppWithTagAndFolder(
    @ColumnInfo("folder_id") private val folderId: Long?,
    @ColumnInfo("folder_name") private val folderName: String?,
    @Embedded val app: AppEntity,
    @ColumnInfo("tag_name") val tagName: String?
) {
    @Ignore
    val folder: FolderEntity? = if (folderId != null && folderName != null) FolderEntity(folderId, folderName) else null
    @Ignore
    val tag: TagEntity? = if (tagName != null) TagEntity(app.packageName, tagName) else null
}