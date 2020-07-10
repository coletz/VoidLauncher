package com.coletz.voidlauncher.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coletz.voidlauncher.models.NotificationObject

@Dao
interface NotificationDao {

    @Query("SELECT * from notification_table ORDER BY postTime")
    fun getAll(): LiveData<List<NotificationObject>>

    @Query("SELECT * from notification_table WHERE id = :id")
    suspend fun get(id: Int): NotificationObject

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(notification: NotificationObject)

    @Query("DELETE FROM notification_table WHERE id = :id")
    suspend fun remove(id: Int)
}