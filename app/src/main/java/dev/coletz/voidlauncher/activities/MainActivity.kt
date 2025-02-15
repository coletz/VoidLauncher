package dev.coletz.voidlauncher.activities

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.OnBackPressedCallback
import dev.coletz.voidlauncher.R
import dev.coletz.voidlauncher.keyboard.Keyboard.*
import dev.coletz.voidlauncher.keyboard.KeyboardView

class MainActivity : BaseMainActivity() {

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
        val mappedKeyCode = mapPhysicalKeyCode(keyCode)
        supportFragmentManager
            .fragments
            .filterIsInstance<KeyboardView.OnKeyboardActionListener>()
            .forEach { it.onKey(mappedKeyCode, intArrayOf()) }
        return super.onKeyDown(keyCode, event)
    }

    private fun mapPhysicalKeyCode(keyCode: Int): Int {
        return when (keyCode) {
            7 -> KEYCODE_CUSTOM_VOICE_RECORD
            11 -> KEYCODE_CUSTOM_CURRENCY
            in 29..54 -> keyCode + 36
            57 -> KEYCODE_ALT
            59 -> KEYCODE_SHIFT_LEFT
            60 -> KEYCODE_SHIFT_RIGHT
            62 -> KEYCODE_SPACE
            63 -> KEYCODE_MODE_CHANGE
            66 -> KEYCODE_DONE
            67 -> KEYCODE_DELETE
            else -> 0
        }
    }
}
