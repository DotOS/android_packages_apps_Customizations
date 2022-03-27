/*
 * Copyright (C) 2022 The DotOS Project
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
package com.dot.customizations.picker.extras

import android.app.Application
import android.content.Context
import android.provider.Settings
import android.view.View
import androidx.lifecycle.AndroidViewModel
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.picker.AppbarFragment
import com.dot.customizations.picker.grid.GridFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.*
import de.Maxr1998.modernpreferences.preferences.SeekBarPreference
import de.Maxr1998.modernpreferences.preferences.choice.SelectionItem

/**
 * ViewModel class that controls 'Extras' preferences
 *
 * [systemSettingsSingleChoice] - Single Choice Dialog Preference
 *
 * [systemSettingsSeekBar] - SeekBar Preference
 *
 * [systemSettingsSwitch] - Switch Preference
 *
 * [fragmentPreference] - Launch a fragment from preference
 */
class ExtrasViewModel(app: Application) : AndroidViewModel(app) {

    init {
        Preference.Config.dialogBuilderFactory = { context ->
            MaterialAlertDialogBuilder(
                context,
                com.android.settingslib.R.style.Theme_AlertDialog_SettingsLib
            )
        }
    }

    var navigationController:
            CustomizationSectionController.CustomizationSectionNavigationController? = null

    val preferencesAdapter = PreferencesAdapter(createScreen(getApplication()))

    /**
     * Root method to add subScreens of extras
     *
     * [Required] For every Screen and subScreen set [Preference.title]
     *
     * @author IacobIonut01
     */
    private fun createScreen(context: Context) = screen(context) {
        title = context.getString(R.string.extras_title)

        sectionQuickSettings(context)
        //fragmentPreference(GridFragment.newInstance(context.getString(R.string.grid_title))).apply {
        //    title = "Test title"
        //}

    }

    /**
     * [subScreen] Quick Settings
     */
    private fun PreferenceScreen.Builder.sectionQuickSettings(context: Context): PreferenceScreen {
        return subScreen("qs") {
            title = context.getString(R.string.section_qs_title)
            summary = "Control your control panel"

            systemSettingsSwitch(context,
                mTitle = "Animate tile state change",
                mSummary = "Tiles will keep their shape on all states",
                mSummaryOn = "Tiles will change their shapes :\n[Active] - Circle\n[Inactive/Disabled] - Rounded Rectangle",
                setting = "QS_TILE_MORPH_ANIM"
            )

            systemSettingsSingleChoice(context,
                mTitle = "Set brightness slider location",
                mSelections = arrayListOf(
                    SelectionItem("0", "Top"),
                    SelectionItem("1", "Bottom"),
                    SelectionItem("2", "Disabled")
                ),
                mDefault = 1,
                setting = "QS_BRIGHTNESS_LOCATION"
            )

            categoryHeader("qsrows") {
                title = "Quick Settings Rows"
            }

            systemSettingsSeekBar(context,
                mTitle = "Portrait",
                mMin = 1,
                mMax = 6,
                mDefault = 3,
                setting = "QS_PANEL_ROWS_PORTRAIT"
            )

            systemSettingsSeekBar(context,
                mTitle = "Landscape",
                mMin = 1,
                mMax = 3,
                mDefault = 1,
                setting = "QS_PANEL_ROWS_LANDSCAPE"
            )

            systemSettingsSeekBar(context,
                mTitle = "Landscape (Media Player active)",
                mMin = 1,
                mMax = 3,
                mDefault = 2,
                setting = "QS_PANEL_ROWS_LANDSCAPE_MEDIA"
            )

            categoryHeader("qscolumns") {
                title = "Quick Settings Columns"
            }

            systemSettingsSeekBar(context,
                mTitle = "Portrait",
                mMin = 3,
                mMax = 6,
                mDefault = 4,
                setting = "QS_PANEL_COLUMNS_PORTRAIT"
            )

            systemSettingsSeekBar(context,
                mTitle = "Landscape",
                mMin = 2,
                mMax = 6,
                mDefault = 4,
                setting = "QS_PANEL_COLUMNS_LANDSCAPE"
            )

            systemSettingsSeekBar(context,
                mTitle = "Landscape (Media Player active)",
                mMin = 2,
                mMax = 6,
                mDefault = 6,
                setting = "QS_PANEL_COLUMNS_LANDSCAPE_MEDIA"
            )
        }
    }

    private fun PreferenceScreen.Builder.fragmentPreference(fragment: AppbarFragment): Preference {
        return pref(fragment::class.java.name) {
            onClickView { navigationController?.navigateTo(fragment) }
        }
    }

    /**
     * [Note] : Selection keys must match Settings Provider's values
     */
    private fun PreferenceScreen.Builder.systemSettingsSingleChoice(
        context: Context,
        mTitle: CharSequence,
        mSelections: ArrayList<SelectionItem>,
        mDefault: Int,
        setting: String
    ) : Preference {
        return singleChoice(setting, mSelections) {
            title = mTitle
            initialSelection = Settings.System.getInt(context.contentResolver, setting, mDefault).toString()
            onSelectionChange {
                Settings.System.putInt(context.contentResolver, setting, it.toInt())
            }
        }
    }

    private fun PreferenceScreen.Builder.systemSettingsSeekBar(
        context: Context,
        mTitle: CharSequence,
        mSummary: CharSequence? = null,
        mMin: Int = 0,
        mMax: Int = 5,
        mDefault: Int = 3,
        setting: String
    ): Preference {
        return seekBar(setting) {
            title = mTitle
            summary = mSummary
            min = mMin
            max = mMax
            default = Settings.System.getInt(context.contentResolver, setting, mDefault)
            seekListener = SeekBarPreference.OnSeekListener { _, _, value ->
                Settings.System.putInt(context.contentResolver, setting, value)
            }
        }
    }

    private fun PreferenceScreen.Builder.systemSettingsSwitch(
        context: Context,
        mTitle: CharSequence,
        mSummary: CharSequence? = null,
        mSummaryOn: CharSequence? = null,
        mDefault: Int = 0,
        setting: String
    ): Preference {
        return switch(setting) {
            title = mTitle
            summary = mSummary
            summaryOn = mSummaryOn
            onCheckedChange {
                Settings.System.putInt(context.contentResolver, setting, if (it) 1 else 0)
            }
            defaultValue = Settings.System.getInt(context.contentResolver, setting, mDefault) == 1
        }
    }

    private fun PreferenceScreen.Builder.secureSettingsSwitch(
        context: Context,
        mTitle: CharSequence,
        mSummary: CharSequence? = null,
        mSummaryOn: CharSequence? = null,
        mDefault: Int = 0,
        setting: String
    ): Preference {
        return switch(setting) {
            title = mTitle
            summary = mSummary
            summaryOn = mSummaryOn
            onCheckedChange {
                Settings.Secure.putInt(context.contentResolver, setting, if (it) 1 else 0)
            }
            defaultValue = Settings.Secure.getInt(context.contentResolver, setting, mDefault) == 1
        }
    }

}