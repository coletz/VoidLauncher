package dev.coletz.voidlauncher.services

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.coletz.voidlauncher.R
import dev.coletz.voidlauncher.activities.SpotlightSetupActivity
import dev.coletz.voidlauncher.models.support.SortingDirection
import dev.coletz.voidlauncher.mvvm.AppViewModel
import dev.coletz.voidlauncher.mvvm.SpotlightPreferencesViewModel
import dev.coletz.voidlauncher.utils.SpaceItemDecoration
import dev.coletz.voidlauncher.views.AppUiItem
import dev.coletz.voidlauncher.views.AppsAdapter

class OverlayService : LifecycleService(), LifecycleOwner, ViewModelStoreOwner {

    companion object {
        const val ACTION_SHOW = "dev.coletz.voidlauncher.ACTION_SHOW_OVERLAY"
        const val ACTION_HIDE = "dev.coletz.voidlauncher.ACTION_HIDE_OVERLAY"
        const val ACTION_TOGGLE = "dev.coletz.voidlauncher.ACTION_TOGGLE_OVERLAY"
        const val ACTION_KEY_EVENT = "dev.coletz.voidlauncher.ACTION_KEY_EVENT"
        const val EXTRA_KEY_CODE = "extra_key_code"
        const val EXTRA_UNICODE_CHAR = "extra_unicode_char"

        var isVisible = false
            private set
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val viewModelStoreOwner = ViewModelStore()

    private lateinit var appViewModel: AppViewModel
    private lateinit var prefsViewModel: SpotlightPreferencesViewModel
    private val appsAdapter = AppsAdapter()

    private var filter: String
        get() = appViewModel.filter.value ?: ""
        set(value) {
            appViewModel.filter.postValue(value)
        }

    override val lifecycle: Lifecycle get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore get() = viewModelStoreOwner

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        appViewModel = AppViewModel(application)
        prefsViewModel = SpotlightPreferencesViewModel(application)
        appViewModel.showAllOnBlankFilter = prefsViewModel.showAllOnStart
        appViewModel.sortingDirection = prefsViewModel.sortingDirection
        appViewModel.updateApps()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> hideOverlay()
            ACTION_TOGGLE -> {
                if (isVisible) hideOverlay() else showOverlay()
            }
            ACTION_KEY_EVENT -> {
                val keyCode = intent.getIntExtra(EXTRA_KEY_CODE, 0)
                val unicodeChar = intent.getIntExtra(EXTRA_UNICODE_CHAR, 0)
                handleKeyEvent(keyCode, unicodeChar)
            }
        }
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (isVisible) return

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Overlay permission required. Open Spotlight setup.", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_app_list, null)

        setupOverlayView()

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val params = WindowManager.LayoutParams(
            (screenWidth * prefsViewModel.spotlightWidthPercentage / 100.0).toInt(),
            (screenHeight * 0.6).toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 80.dp
        }

        windowManager.addView(overlayView, params)
        isVisible = true
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        // Reset filter when showing
        filter = ""
    }

    private fun setupOverlayView() {
        val view = overlayView ?: return

        val appsList: RecyclerView = view.findViewById(R.id.overlay_apps_list)
        val filterView: TextView = view.findViewById(R.id.overlay_filter_view)
        val closeButton: View = view.findViewById(R.id.overlay_close_btn)

        appsList.addItemDecoration(SpaceItemDecoration(16))
        appsList.itemAnimator = null
        appsList.layoutManager = LinearLayoutManager(this)
        appsList.adapter = appsAdapter

        appsAdapter.onAppClicked = { app ->
            launchApp(app)
        }

        appsAdapter.onFolderClicked = { folder ->
            appViewModel.toggleFolder(folder)
        }

        appsAdapter.onVisibleAppsLoaded = {
            appsList.scrollToPosition(appsAdapter.itemCount - 1)
        }

        closeButton.setOnClickListener {
            hideOverlay()
        }

        val settingsButton: View = view.findViewById(R.id.settings_btn)
        settingsButton.setOnClickListener {
            hideOverlay()
            val intent = Intent(this, SpotlightSetupActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        // Handle touch outside to dismiss
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                hideOverlay()
                true
            } else {
                false
            }
        }

        // Handle back key
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (filter.isNotEmpty()) {
                    filter = ""
                } else {
                    hideOverlay()
                }
                true
            } else {
                false
            }
        }

        // Observe apps and filter
        appViewModel.apps.observe(this, Observer { apps ->
            appsAdapter.updateApps(apps)
        })

        appViewModel.filter.observe(this, Observer { filterText ->
            filterView.text = filterText ?: ""
        })

        appViewModel.singleApp.observe(this, Observer { app ->
            app ?: return@Observer
            if (prefsViewModel.autoLaunchOnSingleAppFound) {
                launchApp(app)
            }
        })
    }

    private fun launchApp(app: AppUiItem) {
        filter = ""
        appViewModel.launch(app.identifier)
        hideOverlay()
    }

    private fun hideOverlay() {
        if (!isVisible) return

        overlayView?.let {
            windowManager.removeView(it)
        }
        overlayView = null
        isVisible = false
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onDestroy() {
        hideOverlay()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        viewModelStoreOwner.clear()
        super.onDestroy()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    // Handle key events from accessibility service
    private fun handleKeyEvent(keyCode: Int, unicodeChar: Int) {
        if (!isVisible) return

        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> {
                // Backspace
                if (filter.isNotEmpty()) {
                    filter = filter.dropLast(1)
                }
            }
            KeyEvent.KEYCODE_ENTER -> {
                // Launch first visible app based on sorting direction
                val appToLaunch = when (prefsViewModel.sortingDirection) {
                    SortingDirection.ASCENDING -> appsAdapter.getFirstApp()
                    SortingDirection.DESCENDING -> appsAdapter.getLastApp()
                }
                appToLaunch?.let(::launchApp)
            }
            KeyEvent.KEYCODE_ESCAPE -> {
                // Escape closes overlay or clears filter
                if (filter.isNotEmpty()) {
                    filter = ""
                } else {
                    hideOverlay()
                }
            }
            else -> {
                // Handle printable characters
                if (unicodeChar > 0) {
                    val char = unicodeChar.toChar()
                    if (char.isLetterOrDigit() || char.isWhitespace()) {
                        filter = (filter + char.toString()).trim()
                    }
                }
            }
        }
    }
}
