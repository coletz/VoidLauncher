package com.coletz.voidlauncher.views

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coletz.voidlauncher.fragments.AppListFragment
import com.coletz.voidlauncher.fragments.MainFragment

class MainViewPagerAdapter(activity: AppCompatActivity): FragmentStateAdapter(activity){

    companion object {
        const val INITIAL_PAGE = 0
    }

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> AppListFragment()
            1 -> MainFragment()
            else -> throw Exception("Trying to get fragment with index $position, while only 0-2 are allowed")
        }
    }

}