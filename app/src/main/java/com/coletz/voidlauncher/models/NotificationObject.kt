package com.coletz.voidlauncher.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.coletz.voidlauncher.room.NotificationIcon
import java.io.Serializable

@Entity(tableName = "notification_table")
data class NotificationObject (
    @PrimaryKey
    val id: Int,
    val group: String,
    val postTime: Long? = null,
    @Embedded
    val icon: NotificationIcon? = null
): Serializable