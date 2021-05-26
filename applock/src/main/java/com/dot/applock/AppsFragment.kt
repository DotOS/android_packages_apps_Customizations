package com.dot.applock

import android.app.AppLockManager
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SortedList
import com.dot.applock.adapter.AppLockAdapter
import com.dot.applock.model.AppModel
import kotlinx.android.synthetic.main.fragment_apps.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi

class AppsFragment : Fragment() {

    private lateinit var appLockManager: AppLockManager
    private lateinit var adapter: AppLockAdapter

    private val mCallback = object : SortedList.Callback<AppModel>() {
        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyItemRangeRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            adapter.notifyItemRangeChanged(position, count)
        }

        override fun compare(a: AppModel, b: AppModel): Int {
            return mComparator.compare(a, b)
        }

        override fun areContentsTheSame(oldItem: AppModel, newItem: AppModel): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(item1: AppModel, item2: AppModel): Boolean {
            return item1.mPackageName == item2.mPackageName
        }
    }

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
    ): View? {
        return inflater.inflate(R.layout.fragment_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appLockManager = ResourceHelper.getAppLockManager(requireContext())
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
                apps.sortBy { it.mLabel }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } successUi {
            requireActivity().runOnUiThread {
                adapter = AppLockAdapter(apps, appLockManager)
                applockRecycler.adapter = adapter
                applockRecycler.setHasFixedSize(true)
                applockRecycler.layoutManager = LinearLayoutManager(requireContext())
                applockRecycler.postDelayed({
                    ObjectToolsAnimator.gone(appLockLoading, 500)
                }, 100)
            }
        }
    }
}