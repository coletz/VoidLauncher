package com.coletz.voidlauncher.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.coletz.voidlauncher.R

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
}
