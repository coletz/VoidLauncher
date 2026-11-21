package dev.coletz.voidlauncher.mvvm

import androidx.lifecycle.LiveData
import dev.coletz.voidlauncher.models.AppEntity
import dev.coletz.voidlauncher.models.TagEntity
import dev.coletz.voidlauncher.room.dao.TagEntityDao

class TagRepository(
    private val databaseTagDao: TagEntityDao,
) {

    fun getAppTags(appEntity: AppEntity): LiveData<List<TagEntity>> =
        databaseTagDao.getTagsByPackageName(appEntity.packageName)

    suspend fun insertTag(tag: TagEntity) {
        databaseTagDao.insert(tag)
    }

    suspend fun deleteTag(tag: TagEntity) {
        databaseTagDao.delete(tag)
    }
}