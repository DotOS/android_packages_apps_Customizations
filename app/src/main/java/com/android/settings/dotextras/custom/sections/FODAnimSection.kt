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
import com.android.settings.dotextras.custom.sections.fod.FodAnimationAdapter
import com.android.settings.dotextras.custom.sections.fod.FodResource
import com.android.settings.dotextras.custom.utils.GridSpacingItemDecoration
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.custom.views.ExpandableLayout
import com.android.settings.dotextras.custom.views.FodPreview
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

open class FODAnimSection : GenericSection() {

    private var fodAnims: ArrayList<FodResource> = ArrayList()
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
    private val ANIM_STYLES = arrayListOf(
        "gxzw_normal_recognizing_anim_15",
        "gxzw_aod_recognizing_anim_15",
        "gxzw_aurora_recognizing_anim_20",
        "gxzw_aurora_cas_recognizing_anim_20",
        "gxzw_light_recognizing_anim_15",
        "gxzw_pop_recognizing_anim_7",
        "gxzw_pulse_recognizing_anim_14",
        "gxzw_pulse_recognizing_anim_white_14",
        "gxzw_rhythm_recognizing_anim_11",
        "gxzw_star_cas_recognizing_anim_20",
        "fod_op_cosmos_anim_110",
        "fod_op_energy_anim_25",
        "fod_op_mclaren_anim_50",
        "fod_op_ripple_anim_22",
        "fod_op_scanning_anim_14",
        "fod_op_stripe_anim_23",
        "fod_op_wave_anim_20",
        "fod_pureview_dna_anim_28",
        "fod_pureview_future_anim_26",
        "fod_pureview_halo_ring_anim_15",
        "fod_pureview_molecular_anim_15",
        "asus_fod_anim_1_08",
        "asus_fod_anim_2_23",
        "asus_fod_anim_3_16"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_fod_anim, container, false)
    }

    override fun isAvailable(context: Context): Boolean = ResourceHelper.hasFodSupport(context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fodAnims.clear()
        val fodAnimSwitch: SwitchMaterial = view.findViewById(R.id.fodAnimSwitch)
        val fodAnimLayout: ExpandableLayout = view.findViewById(R.id.fodAnimLayout)
        val fodAnimSupport = ResourceHelper.isPackageInstalled(
            requireContext(),
            ResourceHelper.getFodAnimationPackage(requireContext())
        )
        fodAnimSwitch.isEnabled = fodAnimSupport
        fodAnimLayout.visibility = if (fodAnimSupport) View.VISIBLE else View.GONE
        val fodPreview: FodPreview = view.findViewById(R.id.fodAnimPreview)
        for (j in ICON_STYLES.indices) {
            if (j == featureManager.System().getInt(featureManager.System().FOD_ICON, 0)) {
                fodPreview.setPreview(ResourceHelper.getDrawable(requireContext(),
                    getString(R.string.systemui_package), ICON_STYLES[j]))
            }
        }
        if (fodAnimSupport) {
            val fodAnimStart: MaterialButton = view.findViewById(R.id.fodAnimStart)
            for (i in ANIM_STYLES.indices) {
                val fodAnim = FodResource(ANIM_STYLES[i], i)
                fodAnim.listenerAnim = { drawable ->
                    fodPreview.setPreviewAnimation(drawable!!, false)
                    fodAnimStart.setOnClickListener {
                        fodAnimStart.text =
                            if (!fodPreview.isAnimating()) "Stop Animation" else "Start Animation"
                        fodPreview.setAnimationState(running = !fodPreview.isAnimating())
                    }
                }
                fodAnims.add(fodAnim)
            }
            val recyclerViewAnim: RecyclerView = requireView().findViewById(R.id.fodAnimRecycler)
            recyclerViewAnim.setHasFixedSize(true)
            val adapterAnim =
                FodAnimationAdapter(featureManager, fodAnims)
            recyclerViewAnim.adapter = adapterAnim
            recyclerViewAnim.addItemDecoration(
                GridSpacingItemDecoration(
                    GRID_FOD_COLUMNS,
                    SPACER,
                    true
                )
            )
            fodAnimSwitch.isChecked = featureManager.System()
                .getInt(featureManager.System().FOD_RECOGNIZING_ANIMATION, 0) == 1
            fodAnimLayout.setExpanded(fodAnimSwitch.isChecked, false)
            fodAnimSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    fodAnimLayout.setExpanded(expand = true, animate = true)
                    featureManager.System()
                        .setInt(featureManager.System().FOD_RECOGNIZING_ANIMATION, 1)
                } else {
                    fodAnimLayout.setExpanded(expand = false, animate = true)
                    featureManager.System()
                        .setInt(featureManager.System().FOD_RECOGNIZING_ANIMATION, 0)
                }
            }
            recyclerViewAnim.layoutManager = GridLayoutManager(requireContext(), GRID_FOD_COLUMNS)
        }
    }

}