package com.coletz.voidlauncher.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "tag",
    primaryKeys = ["package_name", "tag_name"],
    foreignKeys = [ForeignKey(
        entity = AppEntity::class,
        parentColumns = ["package_name"],
        childColumns = ["package_name"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TagEntity(
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "tag_name") val tagName: String = "",
)