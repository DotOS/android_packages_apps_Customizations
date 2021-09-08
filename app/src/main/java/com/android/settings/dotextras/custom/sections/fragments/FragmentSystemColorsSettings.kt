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
package com.android.settings.dotextras.custom.sections.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.GenericSection
import com.dot.ui.utils.ResourceHelper
import com.dot.ui.utils.SettingsConstants
import com.android.settings.dotextras.custom.views.ColorSheet
import com.android.settings.dotextras.databinding.FragmentSystemColorsSettingsBinding
import com.dot.ui.TwoToneAccentView

class FragmentSystemColorsSettings : GenericSection() {

    private var _binding: FragmentSystemColorsSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSystemColorsSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            with(binding) {
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
                val themeAccentPicker = (requireActivity() as BaseActivity).binding.themeAccentPicker
                themeAccentPicker.isVisible = !featureManager.AccentManager().isUsingRGBAccent(resources.configuration.uiMode)
                twoToneAccentView.setOnTwoTonePressed {
                    val colorSheet = ColorSheet()
                    colorSheet.colorPicker(resources.getIntArray(R.array.materialColors),
                        onResetListener = {
                            themeAccentPicker.visibility = View.VISIBLE
                            if (it == TwoToneAccentView.Shade.LIGHT)
                                featureManager.AccentManager().resetLight()
                            if (it == TwoToneAccentView.Shade.DARK)
                                featureManager.AccentManager().resetDark()
                        },
                        listener = { color ->
                            themeAccentPicker.visibility = View.GONE
                            if (it == TwoToneAccentView.Shade.LIGHT)
                                twoToneAccentView.bindWhiteColor(color)
                            if (it == TwoToneAccentView.Shade.DARK)
                                twoToneAccentView.bindDarkColor(color)
                        }).show(requireActivity().supportFragmentManager)
                }
            }
        }
    }
}