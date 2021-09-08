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
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.stats.Constants
import com.dot.ui.utils.SettingsConstants
import com.dot.ui.DotMaterialPreference
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_sheet_settings.*

class SettingsFragmentSheet : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_sheet_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedprefStats =
            requireActivity().getSharedPreferences("dotStatsPrefs", Context.MODE_PRIVATE)
        pref_stats.setChecked(sharedprefStats.getBoolean(Constants.ALLOW_STATS, true))
        pref_stats.setOnCheckListener { _, isChecked ->
            run {
                val editor: SharedPreferences.Editor = sharedprefStats.edit()
                editor.putBoolean(Constants.ALLOW_STATS, isChecked)
                editor.apply()
                if (isChecked && sharedprefStats.getBoolean(Constants.IS_FIRST_LAUNCH, true))
                    Snackbar.make(view, "Stats will be pushed on the next launch!", Snackbar.LENGTH_SHORT).show()
            }
        }
        buildSetting(
            requireActivity().getSharedPreferences(SettingsConstants.SETTINGS_PREF, Context.MODE_PRIVATE),
            view.findViewById(R.id.pref_balloons),
            SettingsConstants.SHOW_BALLOONS,
            true
        )
    }

    private fun buildSetting(
        preferences: SharedPreferences,
        view: DotMaterialPreference,
        setting: String,
        default: Boolean
    ) {
        view.setChecked(preferences.getBoolean(setting, default))
        view.setOnCheckListener { _, isChecked ->
            run {
                val editor: SharedPreferences.Editor = preferences.edit()
                editor.putBoolean(setting, isChecked)
                editor.apply()
            }
        }
    }
}