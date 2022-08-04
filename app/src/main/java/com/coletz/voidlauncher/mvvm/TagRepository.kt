package com.coletz.voidlauncher.mvvm

import androidx.lifecycle.LiveData
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.models.TagEntity
import com.coletz.voidlauncher.room.TagEntityDao

class TagRepository(
    private val databaseTagDao: TagEntityDao,
) {

    fun getAppTags(appEntity: AppEntity): LiveData<List<TagEntity>> =
        databaseTagDao.getTagsByPackageNameLive(appEntity.packageName)

    suspend fun insertTag(tag: TagEntity) {
        databaseTagDao.insert(tag)
    }

    suspend fun deleteTag(tag: TagEntity) {
        databaseTagDao.delete(tag)
    }
}