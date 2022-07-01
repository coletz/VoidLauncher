package com.coletz.voidlauncher.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.coletz.voidlauncher.*
import com.coletz.voidlauncher.utils.SpaceItemDecoration
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.coletz.voidlauncher.appoptions.AppOptionMenu
import com.coletz.voidlauncher.databinding.FragmentAppListBinding
import com.coletz.voidlauncher.keyboard.Keyboard
import com.coletz.voidlauncher.keyboard.KeyboardView
import com.coletz.voidlauncher.mvvm.AppViewModel
import com.coletz.voidlauncher.utils.Accessible
import com.coletz.voidlauncher.utils.wip
import com.coletz.voidlauncher.views.AppsAdapter
import com.coletz.voidlauncher.views.multiActionDialog
import java.util.*

class AppListFragment: Fragment(), KeyboardView.OnKeyboardActionListener {

    private var _binding: FragmentAppListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val appViewModel: AppViewModel by activityViewModels()

    private val appsAdapter = AppsAdapter()

    private var spacePressedAt = 0L

    private var filter: String
        get() = appViewModel.filter.value ?: ""
        set(value) {
            appViewModel.filter.postValue(value)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentAppListBinding.inflate(inflater, container, false)
            .also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appsList.addItemDecoration(SpaceItemDecoration(28))

        binding.appsList.itemAnimator = null

        appsAdapter.onAppClicked = { app ->
            filter = ""
            app.launch(context, onError = {
                Log.e("Error", "Error launching app", it)
                Toast.makeText(context, "Error launching app", Toast.LENGTH_LONG).show()
                appViewModel.updateApps()
            })
        }

        appsAdapter.onAppLongClicked = {
            (activity as? AppOptionMenu.Provider)
                ?.appOptionMenu
                ?.open(requireContext(), it)
                ?.let { true }
                ?: false
        }

        appsAdapter.onVisibleAppsLoaded = {
            binding.appsList.scrollToPosition(appsAdapter.itemCount - 1)
        }

        binding.appsList.adapter = appsAdapter

        binding.keyboardView.apply {
            val fragment = this@AppListFragment
            keyboard = Keyboard(fragment.context, R.xml.keyboard_layout)
            setOnKeyboardActionListener(fragment)
        }

        binding.settingsBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }

        binding.settingsBtn.setOnLongClickListener {
            multiActionDialog {
                closeOnSelection = true
                add(getString(R.string.open_app_settings_option_label, getString(R.string.app_name))) { requireContext().wip() }
                add(R.string.reload_apps_label) { appViewModel.updateApps() }
                add(R.string.power_menu_label) { Accessible.openPowerDialog(context) }
            }
            true
        }

        appViewModel.apps.observe(viewLifecycleOwner, appsObserver)
        appViewModel.filter.observe(viewLifecycleOwner, filterObserver)
    }

    override fun onPause() {
        filter = ""
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> {
                val filterLength = filter.count()
                if(filterLength > 0) {
                    filter = filter.substring(0, filterLength - 1)
                }
            }
            Keyboard.KEYCODE_CUSTOM_RECENT -> {
                Accessible.pressRecents(context)
            }
            Keyboard.KEYCODE_CUSTOM_NOTIFICATION -> {
                Accessible.openNotification(context)
            }
            else -> {
                val tmpFilter = filter + primaryCode.toChar().toString()
                if (primaryCode == 32) {
                    val now = Date().time
                    if (tmpFilter.endsWith(" ") && now < spacePressedAt + 500) {
                        spacePressedAt = 0
                        filter = ""
                        Accessible.screenOff(context)
                    } else {
                        filter = tmpFilter
                        spacePressedAt = now
                    }
                } else {
                    filter = tmpFilter.trim()
                }
            }
        }
    }

    override fun onPress(code: Int) {}

    override fun onRelease(code: Int) {}

    override fun onText(charSequence: CharSequence) {}

    override fun swipeLeft() {}

    override fun swipeRight() {}

    override fun swipeDown() {
        Accessible.openNotification(context)
    }

    override fun swipeUp() {}

    private var appsObserver = Observer(appsAdapter::updateApps)

    private var filterObserver = Observer<String> {
        binding.filterView.text = it
    }
}