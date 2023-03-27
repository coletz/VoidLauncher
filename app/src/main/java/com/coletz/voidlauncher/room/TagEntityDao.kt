package com.coletz.voidlauncher.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.coletz.voidlauncher.models.TagEntity

@Dao
interface TagEntityDao {

    @Query("SELECT * FROM tag WHERE package_name = :packageName")
    fun getTagsByPackageName(packageName: String): LiveData<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg tag: TagEntity): LongArray

    @Delete
    suspend fun delete(tag: TagEntity)
}