
package com.android.settings.dotextras.custom.sections.batterystyles

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.settings.dotextras.custom.sections.batterystyles.fragments.*

class BatteryStylesAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = arrayListOf(
        DefaultBatteryStyle(),
        CircleBatteryStyle(),
        CircleDottedBatteryStyle(),
        FilledBatteryStyle()
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}