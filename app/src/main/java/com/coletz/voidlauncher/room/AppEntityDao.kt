package com.coletz.voidlauncher.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.models.AppWithTagEntity
import com.coletz.voidlauncher.models.TagEntity

@Dao
interface AppEntityDao {
    @Query("SELECT * FROM app_entity")
    fun getAllLive(): LiveData<List<AppEntity>>

    @Query("SELECT * FROM app_entity WHERE is_hidden = 0")
    fun getVisibleAppsLive(): LiveData<List<AppEntity>>

    @Query("""SELECT * FROM (
                SELECT a.*, t.tag_name FROM app_entity a JOIN tag t ON a.package_name = t.package_name
                UNION
                SELECT a.*, a.edited_name FROM app_entity a
              ) WHERE is_hidden = 0""")
    fun getVisibleAppsWithTagsLive(): LiveData<List<AppWithTagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOnlyNewApps(apps: List<AppEntity>): LongArray

    @Query("UPDATE app_entity SET is_hidden = 0 WHERE package_name = :packageName")
    suspend fun show(packageName: String)

    @Query("UPDATE app_entity SET is_hidden = 1 WHERE package_name = :packageName")
    suspend fun hide(packageName: String)

    @Query("UPDATE app_entity SET is_favorite = :isFavorite WHERE package_name = :packageName")
    suspend fun setFavorite(packageName: String, isFavorite: Boolean)

    @Query("UPDATE app_entity SET official_name = :officialName WHERE package_name = :packageName")
    suspend fun updateOfficialName(packageName: String, officialName: String): Int

    @Transaction
    suspend fun updateOfficialNames(apps: List<AppEntity>): Int {
        return apps.sumOf { updateOfficialName(it.packageName, it.officialName) }
    }

    @Query("UPDATE app_entity SET edited_name = :editedName WHERE package_name = :packageName")
    suspend fun updateEditableName(packageName: String, editedName: String)

    @Delete
    suspend fun delete(app: AppEntity)

    @Query("DELETE FROM app_entity WHERE package_name NOT IN (:packageNames)")
    suspend fun deleteMissing(packageNames: List<String>): Int
}