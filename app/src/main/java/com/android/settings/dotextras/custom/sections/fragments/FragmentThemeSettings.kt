package com.android.settings.dotextras.custom.sections.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.activities.SectionActivity
import com.android.settings.dotextras.custom.sections.GenericSection
import com.android.settings.dotextras.custom.sections.ThemeSettingsSheet
import com.android.settings.dotextras.custom.sections.clock.*
import com.android.settings.dotextras.custom.sections.grid.GridOptionsManager
import com.android.settings.dotextras.custom.sections.grid.LauncherGridOptionsProvider
import com.android.settings.dotextras.databinding.FragmentThemeSettingsBinding
import com.dot.ui.system.OverlayController
import com.dot.ui.utils.overlayController

class FragmentThemeSettings : GenericSection() {

    private var _binding: FragmentThemeSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentThemeSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                clockfaceSettings.setOnClickPreference {
                    launchSheet(0)
                }
                gridSettings.setOnClickPreference {
                    launchSheet(1)
                }
                shapeSettings.setOnClickPreference {
                    launchSheet(2)
                }
                fontSettings.setOnClickPreference {
                    launchSheet(3)
                }
                iconSettings.setOnClickPreference {
                    launchSheet(4)
                }
                moreSettings.setOnClickListener {
                    startActivity(Intent(requireActivity(), SectionActivity::class.java))
                }
                updateSummaries()
            }
        }
        lifecycleScope.launchWhenResumed {
            updateSummaries()
        }
    }

    private fun launchSheet(type: Int) {
        ThemeSettingsSheet.newInstance(type).show(parentFragmentManager, type.toString())
    }

    fun updateSummaries() {
        if (view != null) {
            updateClockSummary()
            updateGridSummary()
            updateShapeSummary()
            updateFontSummary()
            updateIconPackSummary()
        }
    }

    private fun updateClockSummary() {
        val mClockManager =
            object : BaseClockManager(ContentProviderClockProvider(requireContext())) {
                override fun handleApply(option: Clockface?, callback: onHandleCallback) {
                    callback?.invoke(true)
                }

                override fun lookUpCurrentClock(): String =
                    requireActivity().intent.getStringExtra("clock_face_name").toString()
            }
        if (activity != null && context != null)
            mClockManager.fetchOptions({ options ->
                run {
                    if (activity != null && context != null && options != null) {
                        if (requireActivity().lifecycle.currentState != Lifecycle.State.DESTROYED) {
                            val cm = ClockManager(
                                requireContext().contentResolver,
                                ContentProviderClockProvider(requireContext())
                            )
                            for (option in options) {
                                if (option.isActive(cm)) {
                                    binding.clockfaceSettings.summary = option.getTitle()
                                }
                            }
                        }
                    }
                }
            }, false)
    }

    private fun updateGridSummary() {
        val mGridManager = GridOptionsManager(
            LauncherGridOptionsProvider(
                requireContext(),
                getString(R.string.grid_control_metadata_name)
            )
        )
        if (requireActivity().lifecycle.currentState != Lifecycle.State.DESTROYED) {
            mGridManager.fetchOptions({ options ->
                run {
                    if (activity != null && context != null && options != null)
                    if (requireActivity().lifecycle.currentState != Lifecycle.State.DESTROYED) {
                        for (option in options) {
                            if (option.isActive()) {
                                binding.gridSettings.summary = option.title
                            }
                        }
                    }
                }
            }, false)
        }
    }

    private fun updateShapeSummary() {
        val overlayController =
            requireContext().packageManager.overlayController(OverlayController.Categories.ICON_SHAPE_CATEGORY)
        val shapes = overlayController.Shapes().getShapes(requireContext())
        for (shape in shapes) {
            if (shape.selected) binding.shapeSettings.summary = shape.label
        }
    }

    private fun updateFontSummary() {
        val overlayController =
            requireContext().packageManager.overlayController(OverlayController.Categories.FONT_CATEGORY)
        val fonts = overlayController.FontPacks().getFontPacks(requireContext())
        for (font in fonts) {
            if (font.selected) binding.fontSettings.summary = font.label
        }
    }

    private fun updateIconPackSummary() {
        val overlayController =
            requireContext().packageManager.overlayController(OverlayController.Categories.ANDROID_ICON_PACK_CATEGORY)
        val icons = overlayController.IconPacks().getIconPacks(requireContext())
        for (icon in icons) {
            if (icon.selected) binding.iconSettings.summary = icon.label
        }
    }
}