package com.coletz.voidlauncher.mvvm

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.coletz.voidlauncher.models.NotificationObject
import com.coletz.voidlauncher.room.NotificationDao

class NotificationRepository(private val dao: NotificationDao) {
    val notifications: LiveData<List<NotificationObject>> = dao.getAll()

    @WorkerThread
    suspend fun add(notification: NotificationObject){
        dao.add(notification)
    }

    @WorkerThread
    suspend fun remove(notification: NotificationObject){
        dao.remove(notification.id)
    }
}