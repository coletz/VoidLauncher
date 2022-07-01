package com.coletz.voidlauncher.activities

import android.os.Bundle
import com.coletz.voidlauncher.databinding.ActivityMainViewPagerBinding
import com.coletz.voidlauncher.views.MainViewPagerAdapter

class MainActivityViewPager : BaseMainActivity() {

    private val pagerAdapter by lazy { MainViewPagerAdapter(this) }

    private lateinit var binding: ActivityMainViewPagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainViewPagerBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.currentItem = MainViewPagerAdapter.INITIAL_PAGE

    }

    override fun onResume() {
        super.onResume()
        binding.viewPager.setCurrentItem(MainViewPagerAdapter.INITIAL_PAGE, false)
    }
}
