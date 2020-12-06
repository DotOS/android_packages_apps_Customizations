package com.android.settings.dotextras.custom.sections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.fod.FodAnimation
import com.android.settings.dotextras.custom.sections.fod.FodAnimationAdapter
import com.android.settings.dotextras.custom.sections.fod.FodIcon
import com.android.settings.dotextras.custom.sections.fod.FodIconAdapter
import com.android.settings.dotextras.custom.utils.GridSpacingItemDecoration
import com.android.settings.dotextras.custom.views.ExpandableLayout
import com.android.settings.dotextras.system.FeatureManager
import com.google.android.material.switchmaterial.SwitchMaterial

open class FODSection : Fragment() {

    private val GRID_COLUMNS = 3
    private var fodIcons: ArrayList<FodIcon> = ArrayList()
    private val ICON_STYLES = intArrayOf(
        R.drawable.fod_icon_default,
        R.drawable.fod_icon_default_0,
        R.drawable.fod_icon_default_1,
        R.drawable.fod_icon_default_2,
        R.drawable.fod_icon_default_3,
        R.drawable.fod_icon_default_4,
        R.drawable.fod_icon_default_5,
        R.drawable.fod_icon_arc_reactor,
        R.drawable.fod_icon_cpt_america_flat,
        R.drawable.fod_icon_cpt_america_flat_gray,
        R.drawable.fod_icon_dragon_black_flat,
        R.drawable.fod_icon_evo1,
        R.drawable.fod_icon_glow_circle,
        R.drawable.fod_icon_neon_arc,
        R.drawable.fod_icon_neon_arc_gray,
        R.drawable.fod_icon_neon_circle_pink,
        R.drawable.fod_icon_neon_triangle,
        R.drawable.fod_icon_paint_splash_circle,
        R.drawable.fod_icon_rainbow_horn,
        R.drawable.fod_icon_shooky,
        R.drawable.fod_icon_spiral_blue,
        R.drawable.fod_icon_sun_metro,
        R.drawable.fod_icon_scratch_pink_blue,
        R.drawable.fod_icon_scratch_red_blue,
        R.drawable.fod_icon_fire_ice_ouroboros,
        R.drawable.fod_icon_transparent
    )
    private var fodAnims: ArrayList<FodAnimation> = ArrayList()
    private val ANIM_STYLES = intArrayOf(
        R.drawable.gxzw_normal_recognizing_anim_15,
        R.drawable.gxzw_aod_recognizing_anim_15,
        R.drawable.gxzw_light_recognizing_anim_15,
        R.drawable.gxzw_pop_recognizing_anim_7,
        R.drawable.gxzw_pulse_recognizing_anim_14,
        R.drawable.gxzw_pulse_recognizing_anim_white_14,
        R.drawable.gxzw_rhythm_recognizing_anim_11,
        R.drawable.fod_op_cosmos_anim_110,
        R.drawable.fod_op_mclaren_anim_50,
        R.drawable.fod_op_stripe_anim_23,
        R.drawable.fod_op_wave_anim_20,
        R.drawable.fod_pureview_dna_anim_28,
        R.drawable.fod_pureview_future_anim_26,
        R.drawable.fod_pureview_halo_ring_anim_15,
        R.drawable.fod_pureview_molecular_anim_15
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.section_fod, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val featureManager = FeatureManager(requireActivity().contentResolver)
        fodAnims.clear()
        fodIcons.clear()
        for (i in ANIM_STYLES.indices) {
            fodAnims.add(FodAnimation(ANIM_STYLES[i], i))
        }
        val recyclerViewAnim: RecyclerView = requireView().findViewById(R.id.fodAnimRecycler)
        val adapterAnim =
            FodAnimationAdapter(featureManager, fodAnims)
        recyclerViewAnim.adapter = adapterAnim
        recyclerViewAnim.addItemDecoration(
            GridSpacingItemDecoration(
                GRID_COLUMNS,
                resources.getDimension(R.dimen.recyclerSpacer).toInt(),
                true
            )
        )
        val fodScreenOff: SwitchMaterial = view.findViewById(R.id.fodScreenOff)
        fodScreenOff.isChecked =
            featureManager.System().getInt(featureManager.System().FOD_GESTURE, 0) == 1
        fodScreenOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                featureManager.System().setInt(featureManager.System().FOD_GESTURE, 1)
            } else {
                featureManager.System().setInt(featureManager.System().FOD_GESTURE, 0)
            }
        }
        val fodAnimSwitch: SwitchMaterial = view.findViewById(R.id.fodAnimSwitch)
        val fodAnimLayout: ExpandableLayout = view.findViewById(R.id.fodAnimLayout)
        fodAnimSwitch.isChecked = featureManager.System()
            .getInt(featureManager.System().FOD_RECOGNIZING_ANIMATION, 0) == 1
        fodAnimLayout.setExpanded(fodAnimSwitch.isChecked, false)
        fodAnimSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                fodAnimLayout.setExpanded(expand = true, animate = true)
                featureManager.System().setInt(featureManager.System().FOD_RECOGNIZING_ANIMATION, 1)
            } else {
                fodAnimLayout.setExpanded(expand = false, animate = true)
                featureManager.System().setInt(featureManager.System().FOD_RECOGNIZING_ANIMATION, 0)
            }
        }
        recyclerViewAnim.layoutManager = GridLayoutManager(requireContext(), GRID_COLUMNS)
        for (i in ICON_STYLES.indices) {
            fodIcons.add(FodIcon(ICON_STYLES[i], i))
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.fodIconRecycler)
        val adapter =
            FodIconAdapter(featureManager, fodIcons)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                GRID_COLUMNS,
                resources.getDimension(R.dimen.recyclerSpacer).toInt(),
                true
            )
        )
        recyclerView.layoutManager = GridLayoutManager(requireContext(), GRID_COLUMNS)
    }

}