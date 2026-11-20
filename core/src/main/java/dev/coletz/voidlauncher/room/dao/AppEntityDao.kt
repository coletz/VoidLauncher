package dev.coletz.voidlauncher.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import dev.coletz.voidlauncher.models.AppEntity
import dev.coletz.voidlauncher.room.entities.AppWithTagAndFolder

@Dao
interface AppEntityDao {
    @Transaction
    @Query("""
        WITH app_with_folder AS (SELECT * FROM (
            SELECT f.folder_id as folder_id,
                   f.name      as folder_name,
                   a.*
            FROM folder_entity f
            LEFT JOIN folders_apps_cross_ref ref USING (folder_id)
            JOIN app_entity a USING (package_name)
        
            UNION ALL
        
            SELECT NULL AS folder_id,
                   NULL AS folder_name,
                   a.*
            FROM app_entity a
            LEFT JOIN folders_apps_cross_ref ref USING (package_name)
            WHERE ref.folder_id IS NULL
        ))
        SELECT * FROM (
            SELECT t.tag_name, a.* FROM app_with_folder a JOIN tag_entity t USING(package_name)
            UNION
            SELECT null, a.* FROM app_with_folder a
        ) WHERE is_hidden = 0;
    """)
    fun getVisibleAppsWithTagsAndFolderLive(): LiveData<List<AppWithTagAndFolder>>

    @Query("SELECT * FROM app_entity WHERE package_name = :packageName")
    suspend fun getAppByPackageName(packageName: String): AppEntity

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOnlyNewApps(apps: List<AppEntity>): LongArray

    @Query("UPDATE app_entity SET is_hidden = 0 WHERE package_name = :packageName")
    suspend fun show(packageName: String): Int

    @Query("UPDATE app_entity SET is_hidden = 1 WHERE package_name = :packageName")
    suspend fun hide(packageName: String): Int

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