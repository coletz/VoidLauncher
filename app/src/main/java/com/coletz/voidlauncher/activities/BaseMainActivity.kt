package com.coletz.voidlauncher.activities

import android.os.Bundle
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.coletz.voidlauncher.appoptions.AppOptionMenu
import com.coletz.voidlauncher.appoptions.createAppOptionMenu
import com.coletz.voidlauncher.mvvm.AppViewModel

open class BaseMainActivity : AppCompatActivity(), AppOptionMenu.Provider {

    protected val appViewModel: AppViewModel by viewModels()

    override val appOptionMenu: AppOptionMenu by lazy { createAppOptionMenu(appViewModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        appViewModel.updateApps()
    }
}
