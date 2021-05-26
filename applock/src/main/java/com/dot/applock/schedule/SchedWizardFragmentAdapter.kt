package com.dot.applock.schedule

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dot.applock.ui.CustomTextView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton

class SchedWizardFragmentAdapter(activity: AppCompatActivity, wizardTitle: CollapsingToolbarLayout, statusText: CustomTextView, nextButton: MaterialButton, pager: ViewPager2) : FragmentStateAdapter(activity) {

    val schedWizard = SchedWizardSelectAppsFragment()
    val schedDate = SchedWizardSelectDateFragment()

    init {
        schedWizard.initViews(wizardTitle, statusText, nextButton, pager)
        schedDate.initViews(wizardTitle, statusText, nextButton, pager)
    }

    private val fragmentList = arrayListOf(
        schedWizard,
        schedDate,
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]

}