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
import com.android.settings.dotextras.custom.sections.fod.FodIconAdapter
import com.android.settings.dotextras.custom.sections.fod.FodResource
import com.android.settings.dotextras.custom.utils.GridSpacingItemDecoration
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.custom.views.FodPreview
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

open class FODIconSection : GenericSection() {

    private var fodIcons: ArrayList<FodResource> = ArrayList()
    private val ICON_STYLES = arrayListOf(
        "fod_icon_default",
        "fod_icon_default_0",
        "fod_icon_default_2",
        "fod_icon_default_3",
        "fod_icon_default_4",
        "fod_icon_default_5",
        "fod_icon_default_aosp",
        "fod_icon_arc_reactor",
        "fod_icon_cpt_america_flat",
        "fod_icon_cpt_america_flat_gray",
        "fod_icon_dragon_black_flat",
        "fod_icon_glow_circle",
        "fod_icon_neon_arc",
        "fod_icon_neon_arc_gray",
        "fod_icon_neon_circle_pink",
        "fod_icon_neon_triangle",
        "fod_icon_paint_splash_circle",
        "fod_icon_rainbow_horn",
        "fod_icon_shooky",
        "fod_icon_spiral_blue",
        "fod_icon_sun_metro",
        "fod_icon_scratch_pink_blue",
        "fod_icon_scratch_red_blue",
        "fod_icon_fire_ice_ouroboros",
        "fod_icon_transparent"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_fod_icon, container, false)
    }

    override fun isAvailable(context: Context): Boolean = ResourceHelper.hasFodSupport(context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fodIcons.clear()
        val fodPreview: FodPreview = view.findViewById(R.id.fodIconPreview)
        for (i in ICON_STYLES.indices) {
            val fodIcon = FodResource(ICON_STYLES[i], i)
            fodIcon.listener = { drawable ->
                doAsync {
                    uiThread { fodPreview.setPreview(drawable) }
                }
            }
            fodIcons.add(fodIcon)
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.fodIconRecycler)
        val adapter = FodIconAdapter(featureManager, fodIcons)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                GRID_FOD_COLUMNS,
                SPACER,
                true
            )
        )
        recyclerView.layoutManager = GridLayoutManager(requireContext(), GRID_FOD_COLUMNS)
    }

}