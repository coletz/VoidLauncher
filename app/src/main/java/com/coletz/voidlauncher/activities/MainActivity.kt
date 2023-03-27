package com.coletz.voidlauncher.activities

import android.os.Bundle
import com.coletz.voidlauncher.R

class MainActivity : BaseMainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        if (!appViewModel.filter.value.isNullOrBlank()) {
            appViewModel.filter.postValue(null)
        }
    }
}
