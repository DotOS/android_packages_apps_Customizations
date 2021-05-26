package com.dot.applock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.dot.applock.adapter.AppLockFragmentAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AppLockFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_applock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pagerAdapter = AppLockFragmentAdapter(requireActivity() as AppLockActivity)
        val pager: ViewPager2 = view.findViewById(R.id.appLockContainer)
        pager.adapter = pagerAdapter
        val tabs: TabLayout = view.findViewById(R.id.appLockTabs)
        TabLayoutMediator(tabs, pager) { tab, position ->
            when (position) {
                1 -> tab.text = "Scheduled"
                0 -> tab.text = "All Apps"
            }
        }.attach()
    }
}