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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter
import com.android.settings.dotextras.custom.sections.fod.FodColorAdapter
import com.android.settings.dotextras.custom.sections.fod.FodResource
import com.android.settings.dotextras.custom.utils.GridSpacingItemDecoration
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.google.android.material.snackbar.Snackbar

open class FODOptSection : GenericSection() {

    private var fodColors: ArrayList<FodResource> = ArrayList()
    private var fodoptList: ArrayList<ContextCards> = ArrayList()
    private val PRESSED_COLOR = arrayListOf(
        "fod_icon_pressed",
        "fod_icon_pressed_cyan",
        "fod_icon_pressed_green",
        "fod_icon_pressed_yellow"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_fod_opt, container, false)
    }

    override fun isAvailable(context: Context): Boolean = ResourceHelper.hasFodSupport(context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fodoptList.clear()
        fodColors.clear()
        buildSwitch(
            fodoptList,
            iconID = R.drawable.ic_night_mode,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.fod_nightlight),
            accentColor = R.color.light_green_500,
            feature = featureManager.System().FOD_NIGHT_LIGHT,
            featureType = ContextCardsAdapter.Type.SYSTEM,
            summary = getString(R.string.fod_nightlight_summary),
            enabled = ResourceHelper.shouldDisableNightLight(requireContext())
        )
        if (ResourceHelper.hasAmbient(requireContext())) {
            buildSwitch(
                fodoptList,
                iconID = R.drawable.ic_lock,
                title = getString(R.string.disabled),
                subtitle = getString(R.string.fod_screenoff_title),
                accentColor = R.color.cyan_800,
                feature = featureManager.System().FOD_GESTURE,
                featureType = ContextCardsAdapter.Type.SYSTEM,
                summary = getString(R.string.fod_screenoff_summary)
            )
            { value ->
                featureManager.Secure().enableDozeIfNeeded(requireContext())
                when (value) {
                    0 -> {
                        Snackbar.make(view, getString(R.string.enable_aod), Snackbar.LENGTH_LONG)
                            .setAction(R.string.enable) {
                                featureManager.Secure().enableAOD()
                            }.show()
                    }
                    1 -> {
                        featureManager.Secure().disableAOD()
                    }
                }

            }
        }
        setupLayout(fodoptList, R.id.fodOptSection, GRID_OPT_COLUMNS)
        for (i in PRESSED_COLOR.indices) {
            fodColors.add(FodResource(PRESSED_COLOR[i], i))
        }
        val recyclerfodColorView: RecyclerView = view.findViewById(R.id.fodRecRecycler)
        val adapterfodColor = FodColorAdapter(featureManager, fodColors)
        recyclerfodColorView.adapter = adapterfodColor
        recyclerfodColorView.setHasFixedSize(true)
        recyclerfodColorView.addItemDecoration(
            GridSpacingItemDecoration(
                GRID_FOD_COLUMNS,
                SPACER,
                true
            )
        )
        recyclerfodColorView.layoutManager = GridLayoutManager(requireContext(), GRID_FOD_COLUMNS)
    }
}