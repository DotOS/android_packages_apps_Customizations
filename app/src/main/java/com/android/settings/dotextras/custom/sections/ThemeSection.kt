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
import android.content.om.IOverlayManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.ServiceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.custom.utils.SettingsConstants
import com.android.settings.dotextras.custom.views.AccentColorController
import com.android.settings.dotextras.custom.views.ColorSheet
import com.android.settings.dotextras.custom.views.TwoToneAccentView
import com.android.settings.dotextras.system.OverlayController
import com.google.android.material.switchmaterial.SwitchMaterial

class ThemeSection : GenericSection() {

    private lateinit var twoToneAccentView: TwoToneAccentView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_themes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as BaseActivity).setTitle(getString(R.string.section_themes_title))
        twoToneAccentView = view.findViewById(R.id.twoTone)
        twoToneAccentView.setSharedPref(
            requireActivity().getSharedPreferences(
                SettingsConstants.SETTINGS_PREF,
                Context.MODE_PRIVATE
            )
        )
        twoToneAccentView.bindWhiteColor(
            ResourceHelper.getAccent(
                requireContext(),
                Configuration.UI_MODE_NIGHT_NO
            )
        )
        twoToneAccentView.bindDarkColor(
            ResourceHelper.getAccent(
                requireContext(),
                Configuration.UI_MODE_NIGHT_YES
            )
        )
        val accentController: AccentColorController = view.findViewById(R.id.accentController)
        accentController.updateVisibility(
            resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
        )
        twoToneAccentView.setOnTwoTonePressed {
            val colorSheet = ColorSheet()
            colorSheet.colorPicker(resources.getIntArray(R.array.materialColors),
                onResetListener = {
                    if (it == TwoToneAccentView.Shade.LIGHT)
                        featureManager.AccentManager().resetLight()
                    if (it == TwoToneAccentView.Shade.DARK)
                        featureManager.AccentManager().resetDark()
                },
                listener = { color ->
                    if (it == TwoToneAccentView.Shade.LIGHT)
                        twoToneAccentView.bindWhiteColor(color)
                    if (it == TwoToneAccentView.Shade.DARK)
                        twoToneAccentView.bindDarkColor(color)
                    accentController.updateVisibility(
                        resources.configuration.uiMode and
                                Configuration.UI_MODE_NIGHT_MASK
                    )
                }).show(requireActivity().supportFragmentManager)
        }
        setupStyles(
            view.findViewById(R.id.notifOpacitySwitch),
            OverlayController.Categories.NOTIFICATION_CATEGORY,
            OverlayController.Packages.NOTIFICATION_OPAQUE
        )
        setupStyles(
            view.findViewById(R.id.settingsStyleSwitch),
            OverlayController.Categories.OVERLAY_CATEGORY_STYLES_SETTINGS,
            OverlayController.Packages.STYLES_SETTINGS
        )
        setupStyles(
            view.findViewById(R.id.qsOpacitySwitch),
            OverlayController.Categories.OVERLAY_CATEGORY_STYLES_SYSUI,
            OverlayController.Packages.QS_OPAQUE
        )
    }

    private fun setupStyles(switch: SwitchMaterial, category: String, pckg: String) {
        val styles: OverlayController.Styles = OverlayController(
            category,
            requireActivity().packageManager,
            IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
        ).Styles()
        switch.isChecked = styles.isEnabled(pckg)
        switch.setOnCheckedChangeListener { _, isChecked -> styles.toggleStyle(pckg, isChecked) }
    }

}