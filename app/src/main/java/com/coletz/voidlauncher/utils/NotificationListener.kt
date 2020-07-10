package com.coletz.voidlauncher.utils

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.coletz.voidlauncher.room.NotificationIcon
import com.coletz.voidlauncher.models.NotificationObject
import com.coletz.voidlauncher.mvvm.NotificationViewModel

class NotificationListener: NotificationListenerService(){

    private var notificationViewModel: NotificationViewModel? = null

    override fun onCreate() {
        super.onCreate()
        notificationViewModel = NotificationViewModel(application)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        if(sbn.isOngoing) return


        val notificationIcon = with(sbn.notification.smallIcon) { NotificationIcon(resId.toString(), resPackage) }
        val notificationObj = NotificationObject(
            sbn.id,
            sbn.groupKey,
            sbn.postTime,
            notificationIcon
        )
        notificationViewModel?.add(notificationObj)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn ?: return

        notificationViewModel?.remove(
            NotificationObject(
                sbn.id,
                sbn.groupKey
            )
        )
    }
}