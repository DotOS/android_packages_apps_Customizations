package com.dot.applock.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dot.applock.AppsFragment
import com.dot.applock.schedule.ScheduledFragment

class AppLockFragmentAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    private val fragmentList = arrayListOf(
        AppsFragment(),
        //ScheduledFragment(),
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}