package com.coletz.voidlauncher.mvvm

import androidx.lifecycle.LiveData
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.room.AppEntityDao

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

    suspend fun update(app: AppEntity) {
        databaseAppDao.update(app)
    }

    fun getNotHidden(): LiveData<List<AppEntity>> = databaseAppDao.getNotHiddenLive()

    suspend fun hide(app: AppEntity) {
        databaseAppDao.hide(app.packageName)
    }
}