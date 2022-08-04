package com.coletz.voidlauncher.room

import androidx.room.*
import com.coletz.voidlauncher.models.TagEntity

@Dao
interface TagEntityDao {

    @Query("SELECT * FROM tag WHERE package_name = :packageName")
    suspend fun getTagsByPackageName(packageName: String): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<TagEntity>): LongArray

    @Delete
    suspend fun delete(app: TagEntity)
}