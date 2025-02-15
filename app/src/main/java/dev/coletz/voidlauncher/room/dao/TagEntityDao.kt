package dev.coletz.voidlauncher.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import dev.coletz.voidlauncher.models.TagEntity

@Dao
interface TagEntityDao {

    @Query("SELECT * FROM tag_entity WHERE package_name = :packageName")
    fun getTagsByPackageName(packageName: String): LiveData<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg tag: TagEntity): LongArray

    @Delete
    suspend fun delete(tag: TagEntity)
}