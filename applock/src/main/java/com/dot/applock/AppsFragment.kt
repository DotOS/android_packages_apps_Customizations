package com.dot.applock

import android.app.AppLockManager
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dot.applock.adapter.AppLockAdapter
import com.dot.applock.databinding.FragmentAppsBinding
import com.dot.applock.model.AppModel
import com.dot.ui.utils.ObjectToolsAnimator
import com.dot.ui.utils.ResourceHelper
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi

class AppsFragment : Fragment() {

    private lateinit var appLockManager: AppLockManager
    private lateinit var adapter: AppLockAdapter

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private val mComparator: Comparator<AppModel> =
        Comparator { a, b -> a.mLabel.toString().compareTo(b.mLabel.toString()) }

    override fun onResume() {
        super.onResume()
        view?.requestLayout()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                appLockManager = ResourceHelper.getAppLockManager(requireContext())
                applockRecycler.layoutManager = LinearLayoutManager(requireContext())
                val apps: ArrayList<AppModel> = ArrayList()
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
                        apps.sortWith(mComparator)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } successUi {
                    requireActivity().runOnUiThread {
                        adapter = AppLockAdapter(apps, appLockManager, requireActivity())
                        applockRecycler.adapter = adapter
                        applockRecycler.setHasFixedSize(true)
                        applockRecycler.postDelayed({
                            ObjectToolsAnimator.gone(appLockLoading, 500)
                        }, 100)
                    }
                }
            }
        }
    }
}