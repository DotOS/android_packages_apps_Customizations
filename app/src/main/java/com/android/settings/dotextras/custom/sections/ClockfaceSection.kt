/*
 * Copyright (C) 2021 The dotOS Project
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
package com.android.settings.dotextras.custom.sections

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.FeatureActivityBase
import com.android.settings.dotextras.custom.sections.clock.*
import com.android.settings.dotextras.custom.utils.GridSpacingItemDecoration

class ClockfaceSection : GenericSection() {

    private val EXTRA_CLOCK_FACE_NAME = "clock_face_name"
    private lateinit var mClockManager: BaseClockManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_clockface_v2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as FeatureActivityBase).setTitle(getString(R.string.section_clockface2))
        val recyclerView = view.findViewById<RecyclerView>(R.id.clockfaceOptionsRecycler)
        recyclerView.isNestedScrollingEnabled = false
        val contentProviderClockProvider = ContentProviderClockProvider(requireActivity())
        mClockManager = object : BaseClockManager(ContentProviderClockProvider(requireActivity())) {
            override fun handleApply(option: Clockface?, callback: onHandleCallback) {
                callback?.invoke(true)
            }

            override fun lookUpCurrentClock(): String = requireActivity().intent.getStringExtra(EXTRA_CLOCK_FACE_NAME).toString()
        }
        val clockPreview: ImageView = view.requireViewById(R.id.defaultClockPreview)
        val callback = object: ClockfaceRecyclerAdapter.ClockfaceCallback {
            override fun onApply(clockfaceCompat: ClockfaceCompat) {
                clockPreview.setImageDrawable(null)
                clockfaceCompat.clockface.bindPreviewTile2(clockPreview)
            }
        }
        mClockManager.fetchOptions({ options ->
            run {
                if (options != null) {
                    val cm = ClockManager(
                        requireContext().contentResolver,
                        contentProviderClockProvider
                    )
                    val optionsCompat = ArrayList<ClockfaceCompat>()
                    for (option in options) {
                        optionsCompat.add(ClockfaceCompat(option))
                    }
                    recyclerView.adapter = ClockfaceRecyclerAdapter(cm, callback, optionsCompat)
                    recyclerView.layoutManager =
                        GridLayoutManager(context, 3)
                    recyclerView.addItemDecoration(
                        GridSpacingItemDecoration(3, resources.getDimensionPixelSize(R.dimen.recyclerSpacer), false)
                    )
                    for (i in 0 until optionsCompat.size) {
                        if (optionsCompat[i].clockface.isActive(cm))
                            recyclerView.scrollToPosition(i)
                    }
                }
            }
        }, false)
    }

    override fun isAvailable(context: Context): Boolean {
        val clockfaceManager = object : BaseClockManager(ContentProviderClockProvider(context)) {
            override fun handleApply(option: Clockface?, callback: onHandleCallback) {}

            override fun lookUpCurrentClock(): String = requireActivity().intent.getStringExtra(EXTRA_CLOCK_FACE_NAME).toString()
        }
        return clockfaceManager.isAvailable
    }

}