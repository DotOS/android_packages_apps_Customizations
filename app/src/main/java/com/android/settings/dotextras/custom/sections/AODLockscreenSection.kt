package com.android.settings.dotextras.custom.sections

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.SectionInterface
import com.android.settings.dotextras.custom.sections.clock.*
import com.android.settings.dotextras.custom.utils.ItemRecyclerSpacer

class AODLockscreenSection : GenericSection() {

    private val EXTRA_CLOCK_FACE_NAME = "clock_face_name"
    private var shouldShow = true
    private lateinit var mClockManager: BaseClockManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_aod_lock, container, false)
    }

    override fun isAvailable(context: Context): Boolean = shouldShow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.clockfaceOptionsRecycler)
        recyclerView.isNestedScrollingEnabled = true
        val contentProviderClockProvider = ContentProviderClockProvider(requireActivity())
        mClockManager = object : BaseClockManager(
            ContentProviderClockProvider(requireActivity())) {
            override fun handleApply(option: Clockface?, callback: onHandleCallback) {
                val result = Intent()
                result.putExtra(EXTRA_CLOCK_FACE_NAME, option!!.id)
                requireActivity().setResult(RESULT_OK, result)
                callback?.invoke(true)
            }

            override fun lookUpCurrentClock(): String {
                return requireActivity().intent.getStringExtra(EXTRA_CLOCK_FACE_NAME)
            }
        }
        if (!mClockManager.isAvailable) {
            shouldShow = false
            Log.e("ClockManager", "Not available")
        } else {
            shouldShow = true
            mClockManager.fetchOptions({ options ->
                run {
                    if (options!=null) {
                        val optionsCompat = ArrayList<ClockfaceCompat>()
                        for (option in options) {
                            optionsCompat.add(ClockfaceCompat(option))
                        }
                        recyclerView.adapter =
                            ClockfacePreviewRecyclerAdapter(ClockManager(requireContext().contentResolver, contentProviderClockProvider), optionsCompat)
                        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        recyclerView.addItemDecoration(
                            ItemRecyclerSpacer(resources.getDimension(R.dimen.recyclerSpacerBigger), null, false)
                        )
                        val snap = PagerSnapHelper()
                        snap.attachToRecyclerView(recyclerView)
                    }
                }
            }, false)
        }
    }

}