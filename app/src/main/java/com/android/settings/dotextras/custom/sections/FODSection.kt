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

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter
import com.android.settings.dotextras.custom.sections.clock.*
import com.android.settings.dotextras.custom.sections.fod.FodAnimationAdapter
import com.android.settings.dotextras.custom.sections.fod.FodColorAdapter
import com.android.settings.dotextras.custom.sections.fod.FodIconAdapter
import com.android.settings.dotextras.custom.sections.fod.FodResource
import com.android.settings.dotextras.custom.utils.GridSpacingItemDecoration
import com.android.settings.dotextras.databinding.SectionFodBinding
import com.dot.ui.utils.ResourceHelper
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi

open class FODSection : GenericSection(), TabLayout.OnTabSelectedListener {

    private var _binding: SectionFodBinding? = null
    private val binding get() = _binding!!

    private lateinit var mClockManager: BaseClockManager
    private val EXTRA_CLOCK_FACE_NAME = "clock_face_name"
    private val FOD_ICON_ANIMATION_SETTING = "fod_icon_animation"

    private var fodIcons: ArrayList<FodResource> = ArrayList()
    private var fodAnims: ArrayList<FodResource> = ArrayList()
    private var fodColors: ArrayList<FodResource> = ArrayList()
    private var fodoptList: ArrayList<ContextCards> = ArrayList()
    private val PRESSED_COLOR = arrayListOf(
        "fod_icon_pressed",
        "fod_icon_pressed_cyan",
        "fod_icon_pressed_green",
        "fod_icon_pressed_yellow"
    )
    private val ICON_STYLES = arrayListOf(
        "fod_icon_default",
        "fod_icon_default_0",
        "fod_icon_default_2",
        "fod_icon_default_3",
        "fod_icon_default_4",
        "fod_icon_default_5",
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

    private var latestIcon: Drawable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = SectionFodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun isAvailable(context: Context): Boolean = ResourceHelper.hasFodSupport(context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            showPreview()
            setupFODIconsSelector()
            setupFODAnimationSelector()
            setupFODOptions()
        }
    }

    private fun setupFODIconsSelector() {
        with(binding) {
            val staticIconTab = iconSelector.getTabAt(0)
            val animatedIconTab = iconSelector.getTabAt(1)
            fodIconContainer.setExpanded(iconSelector.selectedTabPosition == 0, true)
            iconSelector.selectTab(if (isAnimatedIconEnabled()) animatedIconTab else staticIconTab)
            fodIconContainer.setExpanded(!isAnimatedIconEnabled(), false)
            iconSelector.addOnTabSelectedListener(this@FODSection)
            task {
                fodIcons.clear()
                for (i in ICON_STYLES.indices) {
                    val fodIcon = FodResource(ICON_STYLES[i], i)
                    fodIcon.listener = { drawable ->
                        latestIcon = drawable
                        updateIconPreview(drawable)
                    }
                    fodIcons.add(fodIcon)
                }
                FodIconAdapter(featureManager, fodIcons)
            } successUi {
                requireActivity().runOnUiThread {
                    fodIconRecycler.adapter = it
                    fodIconRecycler.addItemDecoration(
                        GridSpacingItemDecoration(GRID_FOD_COLUMNS, SPACER, true)
                    )
                    fodIconRecycler.layoutManager =
                        GridLayoutManager(requireContext(), GRID_FOD_COLUMNS)
                }
            }
        }
    }

    private fun setupFODAnimationSelector() {
        with(binding) {
            task {
                for (i in ANIM_STYLES.indices) {
                    val fodAnim = FodResource(ANIM_STYLES[i], i)
                    fodAnim.listenerAnim = { drawable ->
                        requireActivity().runOnUiThread {
                            setPreviewAnimation(drawable!!, false)
                            fodAnimStart.setOnClickListener {
                                fodAnimStart.text =
                                    if (!isAnimating()) getString(R.string.fod_anim_stop) else getString(
                                        R.string.fod_anim_start
                                    )
                                setAnimationState(running = !isAnimating())
                            }
                        }
                    }
                    fodAnims.add(fodAnim)
                }
                FodAnimationAdapter(featureManager, fodAnims)
            } successUi {
                requireActivity().runOnUiThread {
                    fodAnimRecycler.adapter = it
                    fodAnimRecycler.setHasFixedSize(true)
                    fodAnimRecycler.addItemDecoration(
                        GridSpacingItemDecoration(
                            GRID_FOD_COLUMNS,
                            SPACER,
                            true
                        )
                    )

                    fodAnimRecycler.layoutManager =
                        GridLayoutManager(requireContext(), GRID_FOD_COLUMNS)
                }
            }
            fodAnimSwitch.isChecked = featureManager.System()
                .getInt(featureManager.System().FOD_RECOGNIZING_ANIMATION, 0) == 1
            fodAnimLayout.setExpanded(fodAnimSwitch.isChecked, false)
            fodAnimStart.visibility = if (fodAnimSwitch.isChecked) View.VISIBLE else View.GONE
            fodAnimSwitch.setOnCheckedChangeListener { _, isChecked ->
                fodAnimStart.visibility = if (isChecked) View.VISIBLE else View.GONE
                fodAnimLayout.setExpanded(expand = isChecked, animate = true)
                featureManager.System()
                    .setInt(
                        featureManager.System().FOD_RECOGNIZING_ANIMATION,
                        if (isChecked) 1 else 0
                    )
            }
        }
    }

    private fun setupFODOptions() {
        with(binding) {
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
                            Snackbar.make(
                                requireView(),
                                getString(R.string.enable_aod),
                                Snackbar.LENGTH_LONG
                            )
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
            for (i in PRESSED_COLOR.indices) fodColors.add(FodResource(PRESSED_COLOR[i], i))
            val adapterfodColor = FodColorAdapter(featureManager, fodColors)
            fodRecRecycler.adapter = adapterfodColor
            fodRecRecycler.setHasFixedSize(true)
            fodRecRecycler.addItemDecoration(
                GridSpacingItemDecoration(
                    GRID_FOD_COLUMNS,
                    SPACER,
                    true
                )
            )
            fodRecRecycler.layoutManager = GridLayoutManager(requireContext(), GRID_FOD_COLUMNS)
        }
    }


    private fun getAnimatedIcon(): Drawable {
        return ResourceHelper.getDrawable(
            requireContext(),
            getString(R.string.systemui_package),
            "fod_icon_anim_0"
        )!!
    }

    private fun showPreview() {
        mClockManager = object : BaseClockManager(ContentProviderClockProvider(requireActivity())) {
            override fun handleApply(option: Clockface?, callback: onHandleCallback) {
                callback?.invoke(true)
            }

            override fun lookUpCurrentClock(): String =
                requireActivity().intent.getStringExtra(EXTRA_CLOCK_FACE_NAME).toString()
        }
        mClockManager.fetchOptions({ options ->
            run {
                if (options != null) {
                    val cm = ClockManager(
                        requireActivity().contentResolver,
                        ContentProviderClockProvider(requireActivity())
                    )
                    val optionsCompat = ArrayList<ClockfaceCompat>()
                    for (option in options) {
                        val opt = ClockfaceCompat(option)
                        opt.selected = opt.clockface.isActive(cm)
                        optionsCompat.add(opt)
                        if (opt.selected) {
                            opt.clockface.bindPreviewTile(
                                requireActivity(),
                                binding.previewSurfaceLockscreen
                            )
                        }
                    }
                }
            }
        }, false)

        val wallpaperManager = WallpaperManager.getInstance(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK)
            if (pfd != null)
                binding.previewImageLockscreen.load(BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor))
            else
                binding.previewImageLockscreen.load(wallpaperManager.drawable)
        }

        for (j in ICON_STYLES.indices) {
            if (j == featureManager.System().getInt(featureManager.System().FOD_ICON, 0)) {
                updateIconPreview(
                    ResourceHelper.getDrawable(
                        requireContext(),
                        getString(R.string.systemui_package), ICON_STYLES[j]
                    )
                )
            }
        }
    }

    private fun updateIconPreview(drawable: Drawable?) {
        with(binding) {
            fodPreviewSrc.post {
                if (isAnimatedIconEnabled()) {
                    setPreview(getAnimatedIcon())
                    (fodPreviewSrc.drawable!! as AnimationDrawable).start()
                } else {
                    setPreview(drawable)
                }
            }
        }
    }

    private fun isAnimatedIconEnabled(): Boolean =
        featureManager.System().getInt(FOD_ICON_ANIMATION_SETTING, 0) != 0

    private fun setPreview(drawable: Drawable?) {
        binding.fodPreviewSrc.setImageDrawable(drawable)
    }

    private fun setPreviewAnimation(anim: AnimationDrawable, start: Boolean) {
        with(binding) {
            if (fodAnimPreviewSrc.drawable != null && (fodAnimPreviewSrc.drawable as AnimationDrawable).isRunning)
                (fodAnimPreviewSrc.drawable as AnimationDrawable).stop()
            fodAnimPreviewSrc.setImageDrawable(anim)
            if (start) {
                anim.start()
            }
        }
    }

    private fun isAnimating(): Boolean =
        (binding.fodAnimPreviewSrc.drawable!! as AnimationDrawable).isRunning

    private fun setAnimationState(running: Boolean) {
        with(binding) {
            if (running)
                (fodAnimPreviewSrc.drawable!! as AnimationDrawable).start()
            else {
                (fodAnimPreviewSrc.drawable!! as AnimationDrawable).stop()
                val drw = fodAnimPreviewSrc.drawable as AnimationDrawable
                fodAnimPreviewSrc.setImageDrawable(null)
                fodAnimPreviewSrc.setImageDrawable(drw)
            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        with(binding) {
            featureManager.System().setInt(FOD_ICON_ANIMATION_SETTING, tab.position)
            updateIconPreview(latestIcon)
            fodIconContainer.setExpanded(tab.position == 0, true)
            if (tab.position == 1) {
                fodPreviewSrc.post {
                    if (fodPreviewSrc.drawable!! is AnimationDrawable)
                        (fodPreviewSrc.drawable!! as AnimationDrawable).start()
                }
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabReselected(tab: TabLayout.Tab) {}

}