package com.dot.applock.schedule

import android.app.AppLockManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.dot.applock.AppLockActivity
import com.dot.ui.utils.ObjectSerializer
import com.dot.applock.R
import com.dot.applock.ui.CustomTextView
import com.dot.ui.utils.ResourceHelper
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton

class SchedWizardFragment : Fragment() {

    private lateinit var appLockManager: AppLockManager
    lateinit var mStatusText: CustomTextView
    lateinit var mNext: MaterialButton
    lateinit var mSchedWizardTitle: CollapsingToolbarLayout

    override fun onResume() {
        super.onResume()
        view?.requestLayout()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_schedapps_wizard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appLockManager = ResourceHelper.getAppLockManager(requireContext())
        mSchedWizardTitle = view.findViewById(R.id.schedWizardTitle)
        mStatusText = view.findViewById(R.id.schedSelectedText)
        mNext = view.findViewById(R.id.schedNext)
        val SCHED_TEMP_TAG = "scheduledapps_temp"
        val tempPref: SharedPreferences =
            requireContext().getSharedPreferences(SCHED_TEMP_TAG, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = tempPref.edit()
        editor.putString("apps", ObjectSerializer.serialize(ArrayList<String>()))
        editor.apply()
        val pager: ViewPager2 = view.findViewById(R.id.appSchedContainer)
        val pagerAdapter = SchedWizardFragmentAdapter(requireActivity() as AppLockActivity, mSchedWizardTitle, mStatusText, mNext, pager)
        pager.adapter = pagerAdapter
        pager.isUserInputEnabled = false
    }

}