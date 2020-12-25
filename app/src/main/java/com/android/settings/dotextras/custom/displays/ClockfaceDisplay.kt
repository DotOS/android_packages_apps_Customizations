package com.android.settings.dotextras.custom.displays

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.clock.BaseClockManager
import com.android.settings.dotextras.custom.sections.clock.Clockface
import com.android.settings.dotextras.custom.sections.clock.ContentProviderClockProvider
import com.android.settings.dotextras.custom.sections.clock.onHandleCallback

class ClockfaceDisplay : Fragment() {

    private lateinit var mSelectedOption: Clockface
    private val EXTRA_CLOCK_FACE_NAME = "clock_face_name"
    private var shouldShow = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.display_clockface, container, false)
    }

    fun isAvailable(): Boolean = shouldShow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mClockManager = object : BaseClockManager(
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
                    mSelectedOption = options!![0]
                    mSelectedOption.bindThumbnailTile(view)
                }
            }, false)
        }
    }

}