package dev.coletz.voidlauncher.activities

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.OnBackPressedCallback
import dev.coletz.voidlauncher.R
import dev.coletz.voidlauncher.keyboard.KeyboardView
import dev.coletz.voidlauncher.keyboard.KeyboardMapper
import dev.coletz.voidlauncher.keyboard.provideKeyboardMapper

class MainActivity : BaseMainActivity() {

    private val keyboardMapper: KeyboardMapper = provideKeyboardMapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!appViewModel.filter.value.isNullOrBlank()) {
                    appViewModel.filter.postValue(null)
                }
            }
        })
    }

    // For physical keyboard; mapping is needed to get the keyCode equal
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val mappedKeyCode = keyboardMapper.mapKeyCode(keyCode)
        supportFragmentManager
            .fragments
            .filterIsInstance<KeyboardView.OnKeyboardActionListener>()
            .forEach { it.onKey(mappedKeyCode, intArrayOf()) }
        return super.onKeyDown(keyCode, event)
    }
}
