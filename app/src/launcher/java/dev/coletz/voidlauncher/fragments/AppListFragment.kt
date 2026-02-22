package dev.coletz.voidlauncher.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.fragment.app.Fragment
import dev.coletz.voidlauncher.BuildConfig
import dev.coletz.voidlauncher.R
import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import dev.coletz.voidlauncher.appoptions.AppOptionMenu
import dev.coletz.voidlauncher.appoptions.PreferenceManagerDialog
import dev.coletz.voidlauncher.keyboard.Keyboard
import dev.coletz.voidlauncher.keyboard.KeyboardView
import dev.coletz.voidlauncher.keyboard.HAS_PHYSICAL_KEYBOARD
import dev.coletz.voidlauncher.keyboard.KeyboardUtils
import dev.coletz.voidlauncher.keyboard.SOFTWARE_KEYBOARD_LAYOUT
import dev.coletz.voidlauncher.models.support.CustomAction
import dev.coletz.voidlauncher.mvvm.AppViewModel
import dev.coletz.voidlauncher.mvvm.PreferencesViewModel
import dev.coletz.voidlauncher.utils.*
import dev.coletz.voidlauncher.views.AppUiItem
import dev.coletz.voidlauncher.views.AppsAdapter
import dev.coletz.voidlauncher.views.multiActionDialog
import java.util.*

class AppListFragment: Fragment(R.layout.fragment_app_list), KeyboardView.OnKeyboardActionListener {
    
    private lateinit var microphoneBtn: ImageView
    private lateinit var appsList: RecyclerView
    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboardSeparator: View
    private lateinit var settingsBtn: View
    private lateinit var filterView: TextView

    private val appViewModel: AppViewModel by activityViewModels()
    private val prefsViewModel: PreferencesViewModel by activityViewModels()

    private val vibrator by lazy { requireNotNull(requireContext().getSystemService<Vibrator>()) }

    private val appsAdapter = AppsAdapter()

    private var spacePressedAt = 0L

    private var filter: String
        get() = appViewModel.filter.value ?: ""
        set(value) {
            if (prefsViewModel.vibrateOnKeypress) {
                vibrator.vibrate(VibrationEffect.createOneShot(75, 75))
            }
            appViewModel.filter.postValue(value)
        }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            SpeechRecognizerManager.getOrCreate(this, prefsViewModel.voiceSearchLanguage).toggleMic()
        } else {
            SpeechRecognizerManager.errorMissingPermission(requireContext())
        }
    }

    private val speechResultListener: (List<String>) -> Boolean = { queryStrings ->
        context?.let { ctx ->
            microphoneBtn.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.default_text_color))
        }
        appViewModel.guessApp(queryStrings) { app ->
            if (app == null) {
                filter = queryStrings.joinToString(" ").uppercase().trim()
            } else {
                appViewModel.launch(app.identifier)
            }
        }
        false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.bindViews()

        appsList.addItemDecoration(SpaceItemDecoration(28))

        appsList.itemAnimator = null

        appsAdapter.onAppClicked = { app ->
            launchApp(app)
        }

        appsAdapter.onFolderClicked = { folder ->
            appViewModel.toggleFolder(folder)
        }

        appsAdapter.onAppLongClicked = {
            appViewModel.getAppByPackageName(it.identifier) { app ->
                (activity as? AppOptionMenu.Provider)
                    ?.appOptionMenu
                    ?.open(this, app)
            }
        }

        appsAdapter.onVisibleAppsLoaded = {
            appsList.scrollToPosition(appsAdapter.itemCount - 1)
        }

        appsList.adapter = appsAdapter

        keyboardView.apply {
            val fragment = this@AppListFragment
            val swKbEnabled = !KeyboardUtils.hasPhysicalKeyboard(context)
            isVisible = swKbEnabled
            keyboardSeparator.isVisible = swKbEnabled
            if (swKbEnabled) {
                keyboard = Keyboard(fragment.context, SOFTWARE_KEYBOARD_LAYOUT)
                setOnKeyboardActionListener(fragment)
            }
        }

        refreshUi()

        settingsBtn.setOnClickListener {
            multiActionDialog {
                closeOnSelection = true
                add(R.string.android_settings) { startActivity(Intent(Settings.ACTION_SETTINGS)) }
                add(R.string.open_app_settings_option_label) { openAppSettings() }
                add(R.string.reload_apps_label) { appViewModel.updateApps() }
                add(R.string.power_menu_label) { Accessible.openPowerDialog(context) }
            }
        }

        microphoneBtn.setOnClickListener {
            startVoiceRecorder()
        }

        appViewModel.apps.observe(viewLifecycleOwner, appsObserver)
        appViewModel.singleApp.observe(viewLifecycleOwner, singleAppLauncher)
        appViewModel.filter.observe(viewLifecycleOwner, filterObserver)
    }

    private fun View.bindViews() {
        microphoneBtn = findViewById(R.id.microphone_btn)
        appsList = findViewById(R.id.apps_list)
        keyboardView = findViewById(R.id.keyboard_view)
        keyboardSeparator = findViewById(R.id.keyboard_separator)
        settingsBtn = findViewById(R.id.settings_btn)
        filterView = findViewById(R.id.filter_view)
    }

    private fun launchApp(app: AppUiItem) {
        filter = ""
        appViewModel.launch(app.identifier)
    }

    private fun refreshUi() {
        keyboardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            bottomMargin = prefsViewModel.keyboardBottomMargin
        }
    }

    private fun startVoiceRecorder() {
        vibrator.vibrate(VibrationEffect.createOneShot(75, 75))
        context?.let { ctx ->
            microphoneBtn.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.mic_active_color))
        }
        SpeechRecognizerManager.getOrCreate(this, prefsViewModel.voiceSearchLanguage)
            .setSpeechResultListener(speechResultListener)
            .toggleMic(requestPermissionLauncher)
    }

    override fun onPause() {
        filter = ""
        super.onPause()
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        when (primaryCode) {
            0 -> { /* ignore, on MinimalPhone it is used for volume and refresh keys */ }
            Keyboard.KEYCODE_DELETE -> {
                val filterLength = filter.count()
                if(filterLength > 0) {
                    filter = filter.substring(0, filterLength - 1)
                }
            }
            Keyboard.KEYCODE_DONE -> {
                appsAdapter.getLastApp()?.let(::launchApp)
            }
            Keyboard.KEYCODE_CUSTOM_RECENT -> Accessible.pressRecent(context)
            Keyboard.KEYCODE_CUSTOM_NOTIFICATION -> Accessible.openNotification(context)
            Keyboard.KEYCODE_CUSTOM_VOICE_RECORD,
            Keyboard.KEYCODE_SHIFT_LEFT,
            Keyboard.KEYCODE_SHIFT_RIGHT,
            Keyboard.KEYCODE_CUSTOM_CURRENCY,
            Keyboard.KEYCODE_MODE_CHANGE,
            Keyboard.KEYCODE_ALT -> {
                when (val customAction = prefsViewModel.getCustomActionByKey(primaryCode)) {
                    CustomAction.VOICE_SEARCH -> startVoiceRecorder()
                    else -> Accessible.runCustomAction(customAction, requireActivity())
                }
            }
            else -> {
                val tmpFilter = filter + primaryCode.toChar().toString()
                if (primaryCode == Keyboard.KEYCODE_SPACE) {
                    val now = Date().time
                    if (tmpFilter.endsWith(" ") && now < spacePressedAt + 500) {
                        spacePressedAt = 0
                        filter = ""
                        Accessible.screenOff(activity)
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

    override fun onText(charSequence: CharSequence) {}

    override fun swipeDown() {
        Accessible.openNotification(context)
    }

    private fun openAppSettings() {
        PreferenceManagerDialog(requireContext()).apply {
            val updateData = { loadPreferences(prefsViewModel.getAllPreferences()) }

            setOnPreferenceUpdatedListener {
                prefsViewModel.updatePreference(it)
                updateData()
                refreshUi()
            }

            updateData()

            show()
        }

    }

    private var appsObserver = Observer(appsAdapter::updateApps)

    private var singleAppLauncher: Observer<AppUiItem?> = Observer({ app ->
        app ?: return@Observer
        if (prefsViewModel.autoLaunchOnSingleAppFound) {
            launchApp(app)
        }
    })

    private var filterObserver = Observer<String?> {
        filterView.text = it ?: ""
    }
}