package com.dot.applock.schedule

import android.annotation.SuppressLint
import android.app.AppLockManager
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.dot.applock.R
import com.dot.applock.ResourceHelper
import com.dot.applock.model.AppModel
import com.dot.applock.ui.CustomTextView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi

class SchedWizardSelectAppsFragment : Fragment() {

    var wizardTitle: CollapsingToolbarLayout? = null
    var statusText: CustomTextView? = null
    var nextButton: MaterialButton? = null
    var pager: ViewPager2? = null

    fun initViews(wizardTitle: CollapsingToolbarLayout, statusText: CustomTextView, nextButton: MaterialButton, pager: ViewPager2) {
        this.wizardTitle = wizardTitle
        this.statusText = statusText
        this.nextButton = nextButton
        this.pager = pager
    }

    private lateinit var appLockManager: AppLockManager
    private lateinit var adapter: SchedWizardAppAdapter

    override fun onResume() {
        super.onResume()
        view?.requestLayout()
        wizardTitle?.title = "Select apps"
        statusText?.text = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_schedapps_pick, container, false)
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appLockManager = ResourceHelper.getAppLockManager(requireContext())
        val apps: ArrayList<AppModel> = ArrayList()
        val recycler: RecyclerView = view.findViewById(R.id.applockRecycler)
        task {
            try {
                val intent = Intent(Intent.ACTION_MAIN, null)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                val resolveInfoList: List<ResolveInfo> =
                    requireActivity().packageManager.queryIntentActivities(intent,
                        0)
                for (resolveInfo in resolveInfoList) apps.add(AppModel(resolveInfo,
                    requireActivity().packageManager,
                    appLockManager))
                apps.sortBy { it.mLabel }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } successUi {
            requireActivity().runOnUiThread {
                if (statusText != null) {
                    adapter = SchedWizardAppAdapter(apps, statusText!!)
                    recycler.adapter = adapter
                    recycler.setHasFixedSize(true)
                    recycler.layoutManager = LinearLayoutManager(requireContext())
                }
            }
        }
        statusText?.addTextChangedListener {
            if (!it.isNullOrEmpty()) {
                nextButton?.isEnabled = true
                nextButton?.setOnClickListener { pager?.currentItem=+1 }
            } else {
                nextButton?.isEnabled = false
                nextButton?.setOnClickListener(null)
            }
        }
        nextButton?.setOnClickListener {
            if (statusText?.text?.isNotEmpty()!!)
                pager?.currentItem=+1
        }
    }
}