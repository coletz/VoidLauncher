package dev.coletz.voidlauncher.activities

import android.os.Bundle
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dev.coletz.voidlauncher.appoptions.AppOptionMenu
import dev.coletz.voidlauncher.appoptions.createAppOptionMenu
import dev.coletz.voidlauncher.mvvm.AppViewModel

open class BaseMainActivity : AppCompatActivity(), AppOptionMenu.Provider {

    protected val appViewModel: AppViewModel by viewModels()

    override val appOptionMenu: AppOptionMenu by lazy { createAppOptionMenu(appViewModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        appViewModel.updateApps()
    }
}
