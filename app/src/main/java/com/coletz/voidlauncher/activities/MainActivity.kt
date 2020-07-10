package com.coletz.voidlauncher.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.provider.Settings.Secure
import android.view.View.*
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.coletz.voidlauncher.R
import com.coletz.voidlauncher.models.NotificationObject
import com.coletz.voidlauncher.mvvm.AppViewModel
import com.coletz.voidlauncher.mvvm.NotificationViewModel
import com.coletz.voidlauncher.utils.NotificationListener
import com.coletz.voidlauncher.views.MainViewPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {

    private val pagerAdapter by lazy { MainViewPagerAdapter(this) }

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val timeHandler = Handler()

    private val timeRunnable: Runnable by lazy { Runnable {
        time_view.text = dateFormat.format(Date())
        timeHandler.postDelayed(timeRunnable, 1000)
    } }

    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var appViewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setContentView(R.layout.activity_main)

        time_view.setOnClickListener { startActivity(Intent(Settings.ACTION_SETTINGS)) }

        view_pager.adapter = pagerAdapter
        view_pager.currentItem = 1

        notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        notificationViewModel.notifications.observe(this, notificationsObserver)
        appViewModel = ViewModelProvider(this).get(AppViewModel::class.java)
        appViewModel.loadApps()

        notification_bar.rightPadding = 96
    }

    override fun onResume() {
        super.onResume()
        timeHandler.post(timeRunnable)
        view_pager.setCurrentItem(1, false)
        requestNotificationAccess()
    }

    override fun onPause() {
        timeHandler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    private fun requestNotificationAccess() {
        notificationViewModel.checkNotificationAccess {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun addNotification(notification: NotificationObject){
        notificationViewModel.resolveIcon(notification) {
            notification_bar.addNotification(notification, it)
        }
    }

    private var notificationsObserver = Observer<List<NotificationObject>> { notifications ->
        notifications.forEach(::addNotification)
        notification_bar.removeDiff(notifications)
    }
}
