/*
 * Copyright (C) 2020 The dotOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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