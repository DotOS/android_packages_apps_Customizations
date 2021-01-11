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
package com.android.settings.dotextras.custom.sections

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SECURE
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SYSTEM
import com.android.settings.dotextras.custom.sections.clock.*
import com.android.settings.dotextras.custom.utils.ItemRecyclerSpacer

class AODLockscreenSection : GenericSection() {

    private val EXTRA_CLOCK_FACE_NAME = "clock_face_name"
    private lateinit var mClockManager: BaseClockManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_aod_lock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as BaseActivity).setTitle(getString(R.string.section_aod_title))
        val recyclerView = view.findViewById<RecyclerView>(R.id.clockfaceOptionsRecycler)
        recyclerView.isNestedScrollingEnabled = true
        val contentProviderClockProvider = ContentProviderClockProvider(requireActivity())
        mClockManager = object : BaseClockManager(
            ContentProviderClockProvider(requireActivity())) {
            override fun handleApply(option: Clockface?, callback: onHandleCallback) {
                callback?.invoke(true)
            }

            override fun lookUpCurrentClock(): String {
                return requireActivity().intent.getStringExtra(EXTRA_CLOCK_FACE_NAME)
            }
        }
        if (!mClockManager.isAvailable) {
            view.findViewById<LinearLayout>(R.id.clockfaceSection).visibility = View.GONE
            Log.e("ClockManager", "Not available")
        } else {
            mClockManager.fetchOptions({ options ->
                run {
                    if (options != null) {
                        val cm = ClockManager(requireContext().contentResolver,
                            contentProviderClockProvider)
                        val optionsCompat = ArrayList<ClockfaceCompat>()
                        for (option in options) {
                            optionsCompat.add(ClockfaceCompat(option))
                        }
                        recyclerView.adapter =
                            ClockfacePreviewRecyclerAdapter(cm, optionsCompat)
                        recyclerView.layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        recyclerView.addItemDecoration(
                            ItemRecyclerSpacer(resources.getDimension(R.dimen.recyclerSpacerBigger),
                                null,
                                false)
                        )
                        val snap = PagerSnapHelper()
                        snap.attachToRecyclerView(recyclerView)
                        for (i in 0 until optionsCompat.size) {
                            if (optionsCompat[i].clockface.isActive(cm))
                                recyclerView.scrollToPosition(i)
                        }

                    }
                }
            }, false)
        }
        val optionsList = ArrayList<ContextCards>()
        buildSwitch(optionsList,
            iconID = R.drawable.ic_aod,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.aod_title),
            accentColor = R.color.orange_500,
            feature = featureManager.Secure().DOZE_ALWAYS_ON,
            featureType = SECURE,
            summary = getString(R.string.aod_summary)
        )
        buildSwitch(optionsList,
            iconID = R.drawable.ic_light_pulse,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.edge_lightning_title),
            accentColor = R.color.blue_900,
            feature = featureManager.System().AMBIENT_NOTIFICATION_LIGHT,
            featureType = SYSTEM,
            summary = getString(R.string.edge_lightning_summary)
        )
        buildSwipeable(optionsList,
            iconID = R.drawable.ic_light_pulse,
            subtitle = getString(R.string.edge_lightning_title),
            accentColor = R.color.teal_500,
            feature = featureManager.System().AMBIENT_NOTIFICATION_LIGHT_MODE,
            featureType = SYSTEM,
            min = 0,
            max = 3,
            default = 1,
            summary = getString(R.string.edge_lightning_summary_colormode),
            extraTitle = getString(R.string.style)
        ) { position, title ->
            run {
                var newTitle = ""
                when (position) {
                    0 -> newTitle = getString(R.string.edge_lightning_mode_default)
                    1 -> newTitle = getString(R.string.edge_lightning_mode_accent)
                    2 -> newTitle = getString(R.string.edge_lightning_mode_custom)
                    3 -> newTitle = getString(R.string.edge_lightning_mode_auto)
                }
                title.text = newTitle
            }
        }
        buildRGB(optionsList,
            iconID = R.drawable.ic_light_pulse,
            subtitle = getString(R.string.edge_lightning_title),
            feature = featureManager.System().AMBIENT_NOTIFICATION_LIGHT_COLOR,
            featureType = SYSTEM,
            summary = getString(R.string.edge_lightning_summary_color),
            defaultColor = resources.getColor(R.color.defaultEdgeLightningColor, null)
        ) {}
        setupLayout(optionsList, R.id.aodlockContextSection)
    }

}