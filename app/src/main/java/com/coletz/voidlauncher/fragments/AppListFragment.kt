package com.coletz.voidlauncher.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.coletz.voidlauncher.*
import com.coletz.voidlauncher.utils.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_app_list.*
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.coletz.voidlauncher.activities.MainActivity
import com.coletz.voidlauncher.models.AppObject
import com.coletz.voidlauncher.mvvm.AppViewModel
import com.coletz.voidlauncher.views.AppsAdapter
import kotlinx.android.synthetic.main.fragment_app_list.apps_list

class AppListFragment: Fragment(R.layout.fragment_app_list), KeyboardView.OnKeyboardActionListener {

    private lateinit var appViewModel: AppViewModel

    private val appsAdapter by lazy { AppsAdapter(recyclerView = apps_list) }

    private var filter: String = ""
        set(value) {
            field = value
            appsAdapter.filter = value
            filter_view.text = value
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apps_list.addItemDecoration(SpaceItemDecoration(28))

        apps_list.itemAnimator = null

        appsAdapter.onAppClicked = {
            filter = ""
            it.launch(context)
        }
        apps_list.adapter = appsAdapter

        keyboard_view.apply {
            val fragment = this@AppListFragment
            keyboard = Keyboard(fragment.context, R.xml.keyboard_layout)
            setOnKeyboardActionListener(fragment)
        }

        settings_btn.setOnClickListener { startActivity(Intent(Settings.ACTION_SETTINGS)) }

        appViewModel = ViewModelProvider(this).get(AppViewModel::class.java)
        appViewModel.apps.observe(viewLifecycleOwner, appsObserver)
    }

    override fun onPause() {
        filter = ""
        super.onPause()
    }

    override fun onKey(primatyCode: Int, keyCodes: IntArray) {
        if(primatyCode == Keyboard.KEYCODE_DELETE) {
            val filterLength = filter.count()
            if(filterLength > 0) {
                filter = filter.substring(0, filterLength - 1)
            }
        } else {
            filter += primatyCode.toChar().toString()
        }
    }

    override fun onPress(code: Int) {
        if(code <= 30) {
            keyboard_view.isPreviewEnabled = false
        }
    }

    override fun onRelease(code: Int) {
        keyboard_view.isPreviewEnabled = true
    }

    override fun onText(charSequence: CharSequence) {}

    override fun swipeLeft() {}

    override fun swipeRight() {}

    override fun swipeDown() {}

    override fun swipeUp() {}

    private var appsObserver = Observer<List<AppObject>> { apps ->
        appsAdapter.updateApps(apps)
    }
}