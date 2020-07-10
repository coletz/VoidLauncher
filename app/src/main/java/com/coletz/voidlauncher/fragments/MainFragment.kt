package com.coletz.voidlauncher.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.coletz.voidlauncher.*
import com.coletz.voidlauncher.models.AppObject
import com.coletz.voidlauncher.utils.Accessible
import com.coletz.voidlauncher.utils.SpaceItemDecoration
import com.coletz.voidlauncher.views.AppsAdapter
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment: Fragment(R.layout.fragment_main) {
    private val appsAdapter by lazy { AppsAdapter(recyclerView = apps_list) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        screen_off_btn.setOnClickListener { Accessible.screenOff(context) }
        screen_off_btn.setOnLongClickListener { Accessible.openPowerDialog(context); true }

        open_notification_btn.setOnClickListener { Accessible.openNotification(context) }
        open_notification_btn.setOnLongClickListener { Accessible.openQuickSettings(context); true }

        apps_list.addItemDecoration(SpaceItemDecoration(58))

        appsAdapter.onAppClicked = { it.launch(context) }
        apps_list.adapter = appsAdapter

        setFavouriteApps()
    }

    private fun setFavouriteApps(){
        listOf(
            AppObject("Telefono", Intent.ACTION_DIAL, isIntent = true),
            AppObject("Chrome", "com.android.chrome"),
            AppObject("Telegram", "org.telegram.messenger"),
            AppObject("Twitter", "com.twitter.android")
        ).let(appsAdapter::updateApps)
    }
}