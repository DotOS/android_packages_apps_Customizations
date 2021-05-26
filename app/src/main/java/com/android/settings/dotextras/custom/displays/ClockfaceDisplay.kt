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
import android.view.View
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.clock.*

class ClockfaceDisplay : Fragment(R.layout.display_clockface) {

    private var mClockManager: BaseClockManager? = null
    private val EXTRA_CLOCK_FACE_NAME = "clock_face_name"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mClockManager = object : BaseClockManager(
            ContentProviderClockProvider(requireActivity())
        ) {
            override fun handleApply(option: Clockface?, callback: onHandleCallback) {
                val result = Intent()
                result.putExtra(EXTRA_CLOCK_FACE_NAME, option!!.id)
                requireActivity().setResult(RESULT_OK, result)
                callback?.invoke(true)
            }

            override fun lookUpCurrentClock(): String {
                return requireActivity().intent.getStringExtra(EXTRA_CLOCK_FACE_NAME).toString()
            }
        }
        if (!mClockManager!!.isAvailable) {
            Log.e("ClockManager", "Not available")
        } else {
            mClockManager!!.fetchOptions({ options ->
                run {
                    if (options != null && context != null) {
                        val cm = ClockManager(requireContext().contentResolver, ContentProviderClockProvider(requireActivity()))
                        var active = 0
                        for (option in options) {
                            if (option.isActive(cm)) {
                                active = 1
                                option.bindThumbnailTile(view)
                            }
                        }
                        if (active == 0) options[0].bindThumbnailTile(view)
                    }
                }
            }, false)
        }
    }

    override fun onResume() {
        super.onResume()
        if (context != null) {
            if (!mClockManager!!.isAvailable) {
                Log.e("ClockManager", "Not available")
            } else {
                mClockManager!!.fetchOptions({ options ->
                    run {
                        if (options != null && context != null) {
                            val cm = ClockManager(
                                requireContext().contentResolver,
                                ContentProviderClockProvider(requireActivity())
                            )
                            for (option in options) {
                                if (option.isActive(cm)) {
                                    option.bindThumbnailTile(requireView())
                                }
                            }
                        }
                    }
                }, false)
            }
        }
    }

}