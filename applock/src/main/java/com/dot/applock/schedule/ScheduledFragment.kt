package com.dot.applock.schedule

import android.app.AppLockManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dot.applock.R
import com.dot.applock.ResourceHelper
import com.dot.applock.adapter.AppLockAdapter
import com.dot.applock.model.AppModel
import com.dot.applock.task.ScheduledLocker
import com.dot.applock.task.ScheduledUnlocker
import com.google.android.material.button.MaterialButton
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class ScheduledFragment : Fragment() {

    private lateinit var appLockManager: AppLockManager
    private lateinit var adapter: AppLockAdapter

    override fun onResume() {
        super.onResume()
        view?.requestLayout()
    }

    private lateinit var workManager: WorkManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_schedapps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appLockManager = ResourceHelper.getAppLockManager(requireContext())
        workManager = WorkManager.getInstance(requireContext())
        val apps: ArrayList<AppModel> = ArrayList()
        val create: MaterialButton = view.findViewById(R.id.createSched)
        create.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
                .replace(R.id.appLockFragmentContainer, SchedWizardFragment(), "schedWizard")
                .addToBackStack("schedWizard")
                .commit()
        }
    }

    private fun createScheduledUnlocking(apps: ArrayList<AppModel>) {
        val lockSched = PeriodicWorkRequestBuilder<ScheduledUnlocker>(15, TimeUnit.MINUTES).setInputData(passSchedPackages(apps)).build()
        workManager.enqueue(lockSched)
    }

    private fun createScheduledLocking(apps: ArrayList<AppModel>) {
        val lockSched = PeriodicWorkRequestBuilder<ScheduledLocker>(15, TimeUnit.MINUTES).setInputData(passSchedPackages(apps)).build()
        workManager.enqueue(lockSched)
    }

    private fun createOnceScheduledLocking(apps: ArrayList<AppModel>, delayMilis: Long) {
        val lockSched = OneTimeWorkRequestBuilder<ScheduledLocker>().setInitialDelay(delayMilis, TimeUnit.MILLISECONDS).setInputData(passSchedPackages(apps)).build()
        workManager.enqueue(lockSched)
    }

    private fun passSchedPackages(apps: ArrayList<AppModel>): Data {
        val builder = Data.Builder()
        val array = ArrayList<String>()
        for (app in apps) {
            array.add(app.mPackageName.toString())
        }
        builder.putStringArray("PACKAGES", array.toTypedArray())
        return builder.build()
    }
}