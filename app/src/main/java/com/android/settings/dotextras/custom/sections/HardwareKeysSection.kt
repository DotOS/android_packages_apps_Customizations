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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.custom.views.BacklightBottomSheet
import com.android.settings.dotextras.system.FeatureManager
import kotlin.collections.ArrayList

class HardwareKeysSection : GenericSection() {

    private var hwkeys0List: ArrayList<ContextCards> = ArrayList()
    private var hwkeys1List: ArrayList<ContextCards> = ArrayList()
    private var hwkeys2List: ArrayList<ContextCards> = ArrayList()
    private var hwkeys3List: ArrayList<ContextCards> = ArrayList()
    private var hwkeys4List: ArrayList<ContextCards> = ArrayList()
    private var hwkeys5List: ArrayList<ContextCards> = ArrayList()
    private var hwkeys6List: ArrayList<ContextCards> = ArrayList()
    private var hwkeys7List: ArrayList<ContextCards> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.section_hwkeys, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val actionEntries = resources.getStringArray(R.array.hardware_keys_action_entries)
        val actionEntriesArrayList = ArrayList<String>()
        for (entry in actionEntries) {
            actionEntriesArrayList.add(entry)
        }
        val actionValues = resources.getIntArray(R.array.hardware_keys_action_values)
        val actionValuesArrayList = ArrayList<Int>()
        for (entry in actionValues) {
            actionValuesArrayList.add(entry)
        }
        val actionEntriesGo = resources.getStringArray(R.array.hardware_keys_action_entries_go)
        val actionEntriesArrayListGo = ArrayList<String>()
        for (entry in actionEntriesGo) {
            actionEntriesArrayListGo.add(entry)
        }
        val actionValuesGo = resources.getIntArray(R.array.hardware_keys_action_values_go)
        val actionValuesArrayListGo = ArrayList<Int>()
        for (entry in actionValuesGo) {
            actionValuesArrayListGo.add(entry)
        }
        val actionEntriesVol = resources.getStringArray(R.array.volbtn_cursor_control_entries)
        val actionEntriesVolArrayList = ArrayList<String>()
        for (entry in actionEntriesVol) {
            actionEntriesVolArrayList.add(entry)
        }
        val actionValuesVol = resources.getIntArray(R.array.volbtn_cursor_control_values)
        val actionValuesVolArrayList = ArrayList<Int>()
        for (entry in actionValuesVol) {
            actionValuesVolArrayList.add(entry)
        }
        hwkeys0List.clear()
        hwkeys1List.clear()
        hwkeys2List.clear()
        if (ResourceHelper.hasHardwareKeys(requireContext())) {
            buildSwitch(
                hwkeys0List,
                iconID = R.drawable.ic_menu,
                title = getString(R.string.disabled),
                subtitle = getString(R.string.navkeys_title),
                accentColor = R.color.dot_teal,
                summary = getString(R.string.navkeys_summary),
                default = if (ResourceHelper.isNavbarEnabled(requireContext())) 1 else 0
            ) { value -> ResourceHelper.setNavbarEnabled(requireContext(), value == 1) }
            buildSwitch(
                hwkeys0List,
                iconID = R.drawable.ic_menu,
                title = getString(R.string.control),
                subtitle = getString(R.string.button_backlight_title),
                accentColor = R.color.dot_sky,
                summary = getString(R.string.backlight_summary),
                default = 1,
                disableColor = true
            ) { BacklightBottomSheet().show(parentFragmentManager, "backlight") }
        }
        buildSwitch(
            hwkeys0List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.navbar_invert_layout_title),
            accentColor = R.color.dot_green,
            feature = featureManager.Secure().SYSUI_NAV_BAR_INVERSE,
            featureType = ContextCardsAdapter.Type.SECURE,
            summary = getString(R.string.navbar_invert_layout_summary),
            enabled = false
        )
        setupLayout(hwkeys0List, R.id.hwkeys0)
        if (ResourceHelper.canWakeUsingHomeKey(requireContext()))
        buildSwitch(
            hwkeys1List,
            iconID = R.drawable.ic_homehw,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.button_wake_title),
            accentColor = R.color.red_600,
            feature = "home_wake_screen",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            summary = getString(R.string.home_key_press_summary),
            enabled = true
        )
        buildListSheet(
            hwkeys1List,
            iconID = R.drawable.ic_homehw,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.hardware_keys_long_press_title),
            accentColor = R.color.red_600,
            feature = "key_home_long_press_action",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            default = 0,
            summary = getString(R.string.home_key_press_summary),
            entries = if (ResourceHelper.isAndroidGo()) actionEntriesArrayListGo else actionEntriesArrayList,
            entryValues = if (ResourceHelper.isAndroidGo()) actionValuesArrayListGo else actionValuesArrayList
        )
        buildListSheet(
            hwkeys1List,
            iconID = R.drawable.ic_homehw,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.hardware_keys_double_tap_title),
            accentColor = R.color.red_600,
            feature = "key_home_double_tap_action",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            default = 0,
            summary = getString(R.string.home_key_press_summary),
            entries = if (ResourceHelper.isAndroidGo()) actionEntriesArrayListGo else actionEntriesArrayList,
            entryValues = if (ResourceHelper.isAndroidGo()) actionValuesArrayListGo else actionValuesArrayList
        )
        setupLayoutWithCondition(hwkeys1List, R.id.hwkeys1, ResourceHelper.hasHomeKey(requireContext()))
        if (ResourceHelper.canWakeUsingBackKey(requireContext()))
        buildSwitch(
            hwkeys2List,
            iconID = R.drawable.ic_arrow_back,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.button_wake_title),
            accentColor = R.color.orange_500,
            feature = "back_wake_screen",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            summary = getString(R.string.back_key_press_summary),
            enabled = false
        )
        setupLayoutWithCondition(hwkeys2List, R.id.hwkeys2, ResourceHelper.hasBackKey(requireContext()))
        if (ResourceHelper.canWakeUsingMenuKey(requireContext()))
        buildSwitch(
            hwkeys3List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.button_wake_title),
            accentColor = R.color.green_500,
            feature = "menu_wake_screen",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            summary = getString(R.string.menu_key_press_summary),
            enabled = false
        )
        buildListSheet(
            hwkeys3List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.hardware_keys_long_press_title),
            accentColor = R.color.green_500,
            feature = "key_menu_long_press_action",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            default = 0,
            summary = getString(R.string.menu_key_press_summary),
            entries = if (ResourceHelper.isAndroidGo()) actionEntriesArrayListGo else actionEntriesArrayList,
            entryValues = if (ResourceHelper.isAndroidGo()) actionValuesArrayListGo else actionValuesArrayList
        )
        buildListSheet(
            hwkeys3List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.hardware_keys_double_tap_title),
            accentColor = R.color.green_500,
            feature = "key_menu_double_tap_action",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            default = 0,
            summary = getString(R.string.menu_key_press_summary),
            entries = if (ResourceHelper.isAndroidGo()) actionEntriesArrayListGo else actionEntriesArrayList,
            entryValues = if (ResourceHelper.isAndroidGo()) actionValuesArrayListGo else actionValuesArrayList
        )
        setupLayoutWithCondition(hwkeys3List, R.id.hwkeys3, ResourceHelper.hasMenuKey(requireContext()))
        if (ResourceHelper.canWakeUsingAssistKey(requireContext()))
        buildSwitch(
            hwkeys4List,
            iconID = R.drawable.ic_search,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.button_wake_title),
            accentColor = R.color.cyan_500,
            feature = "assist_wake_screen",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            summary = getString(R.string.assist_key_press_summary),
            enabled = false
        )
        buildListSheet(
            hwkeys4List,
            iconID = R.drawable.ic_search,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.hardware_keys_long_press_title),
            accentColor = R.color.cyan_500,
            feature = "key_assist_long_press_action",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            default = 0,
            summary = getString(R.string.assist_key_press_summary),
            entries = if (ResourceHelper.isAndroidGo()) actionEntriesArrayListGo else actionEntriesArrayList,
            entryValues = if (ResourceHelper.isAndroidGo()) actionValuesArrayListGo else actionValuesArrayList
        )
        buildListSheet(
            hwkeys4List,
            iconID = R.drawable.ic_search,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.hardware_keys_double_tap_title),
            accentColor = R.color.cyan_500,
            feature = "key_assist_double_tap_action",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            default = 0,
            summary = getString(R.string.assist_key_press_summary),
            entries = if (ResourceHelper.isAndroidGo()) actionEntriesArrayListGo else actionEntriesArrayList,
            entryValues = if (ResourceHelper.isAndroidGo()) actionValuesArrayListGo else actionValuesArrayList
        )
        setupLayoutWithCondition(hwkeys4List, R.id.hwkeys4, ResourceHelper.hasAssistKey(requireContext()))
        if (ResourceHelper.canWakeUsingAppSwitchKey(requireContext()))
        buildSwitch(
            hwkeys5List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.button_wake_title),
            accentColor = R.color.purple_500,
            feature = "app_switch_wake_screen",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            summary = getString(R.string.app_switch_key_press),
            enabled = false
        )
        buildListSheet(
            hwkeys5List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.hardware_keys_long_press_title),
            accentColor = R.color.purple_500,
            feature = "key_app_switch_long_press_action",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            default = 0,
            summary = getString(R.string.app_switch_key_press),
            entries = if (ResourceHelper.isAndroidGo()) actionEntriesArrayListGo else actionEntriesArrayList,
            entryValues = if (ResourceHelper.isAndroidGo()) actionValuesArrayListGo else actionValuesArrayList
        )
        buildListSheet(
            hwkeys5List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.hardware_keys_double_tap_title),
            accentColor = R.color.purple_500,
            feature = "key_app_switch_double_tap_action",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            default = 0,
            summary = getString(R.string.app_switch_key_press),
            entries = if (ResourceHelper.isAndroidGo()) actionEntriesArrayListGo else actionEntriesArrayList,
            entryValues = if (ResourceHelper.isAndroidGo()) actionValuesArrayListGo else actionValuesArrayList
        )
        setupLayoutWithCondition(hwkeys5List, R.id.hwkeys5, ResourceHelper.hasAppSwitchKey(requireContext()))
        if (ResourceHelper.canWakeUsingCameraKey(requireContext()))
        buildSwitch(
            hwkeys6List,
            iconID = R.drawable.ic_camera,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.button_wake_title),
            accentColor = R.color.dot_teal,
            feature = "camera_wake_screen",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            summary = getString(R.string.camera_key_press_summary),
            enabled = false
        )
        buildListSheet(
            hwkeys6List,
            iconID = R.drawable.ic_camera,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.hardware_keys_long_press_title),
            accentColor = R.color.dot_teal,
            feature = "key_camera_long_press_action",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            default = 0,
            summary = getString(R.string.camera_key_press_summary),
            entries = if (ResourceHelper.isAndroidGo()) actionEntriesArrayListGo else actionEntriesArrayList,
            entryValues = if (ResourceHelper.isAndroidGo()) actionValuesArrayListGo else actionValuesArrayList
        )
        buildListSheet(
            hwkeys6List,
            iconID = R.drawable.ic_camera,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.hardware_keys_double_tap_title),
            accentColor = R.color.dot_teal,
            feature = "key_camera_double_tap_action",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            default = 0,
            summary = getString(R.string.camera_key_press_summary),
            entries = if (ResourceHelper.isAndroidGo()) actionEntriesArrayListGo else actionEntriesArrayList,
            entryValues = if (ResourceHelper.isAndroidGo()) actionValuesArrayListGo else actionValuesArrayList
        )
        setupLayoutWithCondition(hwkeys6List, R.id.hwkeys6, ResourceHelper.hasCameraKey(requireContext()))
        if (ResourceHelper.canWakeUsingVolumeKeys(requireContext()))
        buildSwitch(
            hwkeys7List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.button_wake_title),
            accentColor = R.color.purple_500,
            feature = "volume_wake_screen",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            summary = getString(R.string.volume_summary),
            enabled = false
        )
        buildSwitch(
            hwkeys7List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.volume_answer_call_title),
            accentColor = R.color.purple_500,
            feature = "volume_answer_call",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            summary = getString(R.string.volume_summary),
            enabled = false
        )
        buildSwitch(
            hwkeys7List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.volbtn_music_controls_title),
            accentColor = R.color.purple_500,
            feature = "volbtn_music_controls",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            summary = getString(R.string.volume_summary),
            enabled = false
        )
        /*
         * A bit broken for now
        buildListSheet(
            hwkeys7List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.volbtn_cursor_control_title),
            accentColor = R.color.purple_500,
            feature = "volume_key_cursor_control",
            featureType = ContextCardsAdapter.Type.SYSTEM,
            default = 0,
            summary = getString(R.string.volume_summary),
            entries = actionEntriesVolArrayList,
            entryValues = actionValuesVolArrayList
        )
         */
        val volRockerSwapValue = ResourceHelper.getVolRockerSwap(requireContext())
        buildSwitch(
            hwkeys7List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.swap_volume_buttons_title),
            accentColor = R.color.purple_500,
            summary = getString(R.string.swap_volume_buttons_summary),
            default = if (featureManager.System().getInt("swap_volume_keys_on_rotation", 0) != 0) 1 else 0
        ) { value ->
            featureManager.System().setInt("swap_volume_keys_on_rotation", if (value == 1) if (volRockerSwapValue != -1) volRockerSwapValue else 1 else 0)
        }

        setupLayout(hwkeys7List, R.id.hwkeys7)
    }

    override fun isAvailable(context: Context): Boolean {
        return true
    }
}