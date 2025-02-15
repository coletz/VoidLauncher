package dev.coletz.voidlauncher.mvvm

import androidx.lifecycle.LiveData
import dev.coletz.voidlauncher.models.AppEntity
import dev.coletz.voidlauncher.room.dao.AppEntityDao
import dev.coletz.voidlauncher.room.entities.AppWithTagAndFolder
import dev.coletz.voidlauncher.views.AppUiItem
import dev.coletz.voidlauncher.views.FolderUiItem
import dev.coletz.voidlauncher.views.MainListUiItem

class AppRepository(
    private val packageManagerDao: PackageManagerDao,
    private val databaseAppDao: AppEntityDao,
) {

    suspend fun updateApps() {
        val apps = packageManagerDao.getInstalledApps()
        if (apps is AppListResult.Success) {
            // - rimuovo da db le app che ci sono a db ma non sono installate (sono state disinstallate)
            databaseAppDao.deleteMissing(apps.list.map(AppEntity::packageName))
            // + aggiungo tutte le app che sono installate ma non sono a db
            databaseAppDao.insertOnlyNewApps(apps.list).count()
            // ^ aggiorno le app già presenti (officialName potrebbe cambiare)
            databaseAppDao.updateOfficialNames(apps.list)
        }
    }

    suspend fun updateEditableName(packageName: String, editedName: String) {
        databaseAppDao.updateEditableName(packageName, editedName)
    }

    fun getVisibleAppsWithTagsAndFolder(): LiveData<List<AppWithTagAndFolder>> =
        databaseAppDao.getVisibleAppsWithTagsAndFolderLive()

    suspend fun hide(app: AppEntity) {
        databaseAppDao.hide(app.packageName)
    }

    suspend fun setFavorite(app: AppEntity, isFavorite: Boolean) {
        databaseAppDao.setFavorite(app.packageName, isFavorite)
    }

    suspend fun getAppByPackageName(packageName: String): AppEntity =
        databaseAppDao.getAppByPackageName(packageName)
}
