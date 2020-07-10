package com.coletz.voidlauncher.mvvm

import android.app.Application
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.coletz.voidlauncher.R
import com.coletz.voidlauncher.models.NotificationObject
import com.coletz.voidlauncher.room.VoidDatabase
import com.coletz.voidlauncher.utils.NotificationListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationViewModel(application: Application): AndroidViewModel(application){
    private val repo: NotificationRepository

    val notifications: LiveData<List<NotificationObject>>

    init {
        val dao = VoidDatabase.getDatabase(application).notificationDao()
        repo = NotificationRepository(dao)

        notifications = repo.notifications
    }

    fun add(notification: NotificationObject) = viewModelScope.launch(Dispatchers.IO) {
        repo.add(notification)
    }

    fun remove(notification: NotificationObject) = viewModelScope.launch(Dispatchers.IO) {
        repo.remove(notification)
    }

    fun resolveIcon(notification: NotificationObject, onLoaded: (Drawable?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val app = getApplication<Application>()
        val icon = notification.icon

        val icDrawable = if(icon == null){
            ResourcesCompat.getDrawable(app.resources, R.drawable.ic_broken_icon, null)
        } else {
            try {
                val pkgResources = app.packageManager.getResourcesForApplication(icon.resPackage)
                val resId = pkgResources.getIdentifier(icon.resId, "drawable", icon.resPackage)
                ResourcesCompat.getDrawable(pkgResources, resId, null)
            } catch (e: Exception){
                ResourcesCompat.getDrawable(app.resources, R.drawable.ic_broken_icon, null)
            }
        }
        withContext(Dispatchers.Main){
            onLoaded(icDrawable)
        }
    }

    fun checkNotificationAccess(onMissingAccess: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val app = getApplication<Application>()
        val pkgName = app.packageName
        val serviceName = NotificationListener::class.java.name
        val isEnabled = Settings.Secure.getString(app.contentResolver, "enabled_notification_listeners")
            .split(":")
            .map { it.split("/") }
            .any { it[0] == pkgName && it[1] == serviceName }

        if (!isEnabled) {
            withContext(Dispatchers.Main) {
                onMissingAccess()
            }
        }
    }
}