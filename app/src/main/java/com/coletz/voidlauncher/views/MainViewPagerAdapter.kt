package com.coletz.voidlauncher.views

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coletz.voidlauncher.fragments.AppListFragment
import com.coletz.voidlauncher.fragments.MainFragment
import com.coletz.voidlauncher.fragments.UserNotesFragment

class MainViewPagerAdapter(activity: AppCompatActivity): FragmentStateAdapter(activity){
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> UserNotesFragment()
            1 -> MainFragment()
            2 -> AppListFragment()
            else -> throw Exception("Trying to get fragment with index $position, while only 0-2 are allowed")
        }
    }

}