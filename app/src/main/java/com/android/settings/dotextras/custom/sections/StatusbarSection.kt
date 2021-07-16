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
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SYSTEM
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.custom.utils.SettingsConstants

class StatusbarSection : GenericSection() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_statusbar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = ArrayList<ContextCards>()
        val perclist = ArrayList<ContextCards>()
        val batteryLightList = ArrayList<ContextCards>()
        val trafficList = ArrayList<ContextCards>()
        val clockList = ArrayList<ContextCards>()
        val extrasList = ArrayList<ContextCards>()
        buildSwipeable(
            list,
            iconID = R.drawable.round_battery_full_white_36dp,
            subtitle = getString(R.string.battery_styles),
            accentColor = R.color.colorAccent,
            feature = featureManager.System().STATUS_BAR_BATTERY_STYLE,
            featureType = SYSTEM,
            min = 0,
            max = 3,
            default = 0,
            summary = getString(R.string.status_bar),
            extraTitle = getString(R.string.style)
        ) { position, title ->
            run {
                var newTitle = ""
                when (position) {
                    0 -> newTitle = "Default"
                    1 -> newTitle = "Circle"
                    2 -> newTitle = "Dotted"
                    3 -> newTitle = "Filled"
                }
                title.text = newTitle
            }
        }
        setupLayout(list, R.id.sectionBS, 1, true)
        buildSwipeable(
            list = perclist,
            iconID = R.drawable.round_battery_unknown_white_36dp,
            subtitle = getString(R.string.percentage_style),
            accentColor = R.color.teal_500,
            feature = featureManager.System().STATUS_BAR_SHOW_BATTERY_PERCENT,
            featureType = SYSTEM,
            min = 0,
            max = 2,
            default = 0,
            summary = getString(R.string.status_bar),
            extraTitle = getString(R.string.style)
        ) { position, title ->
            run {
                var newTitle = ""
                when (position) {
                    0 -> newTitle = getString(R.string.hide)
                    1 -> newTitle = getString(R.string.inside)
                    2 -> newTitle = getString(R.string.outside)
                }
                title.text = newTitle
            }
        }
        buildSwitch(
            list = perclist,
            iconID = R.drawable.round_battery_unknown_white_36dp,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.qs_percentage_style),
            accentColor = R.color.red_500,
            feature = featureManager.System().QS_SHOW_BATTERY_PERCENT,
            featureType = SYSTEM,
            summary = getString(R.string.qs_percentage),
        )
        setupLayout(perclist, R.id.sectionPercentage)
        createBalloon(R.string.swipe_to_change, 0, R.id.sectionPercentage)
        val trafficModeEntries = resources.getStringArray(R.array.network_traffic_mode_entries)
        val trafficModeEntriesArrayList = ArrayList<String>()
        for (entry in trafficModeEntries) {
            trafficModeEntriesArrayList.add(entry)
        }
        val trafficModeValues = resources.getIntArray(R.array.network_traffic_mode_values)
        val trafficModeValuesArrayList = ArrayList<Int>()
        for (entry in trafficModeValues) {
            trafficModeValuesArrayList.add(entry)
        }
        val trafficUnitsEntries = resources.getStringArray(R.array.network_traffic_units_entries)
        val trafficUnitsEntriesArrayList = ArrayList<String>()
        for (entry in trafficUnitsEntries) {
            trafficUnitsEntriesArrayList.add(entry)
        }
        val trafficUnitsValues = resources.getIntArray(R.array.network_traffic_units_values)
        val trafficUnitsValuesArrayList = ArrayList<Int>()
        for (entry in trafficUnitsValues) {
            trafficUnitsValuesArrayList.add(entry)
        }
        buildListSheet(
            trafficList,
            iconID = R.drawable.ic_traffic,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.traffic_meter_title),
            accentColor = R.color.cyan_700,
            feature = "network_traffic_location",
            featureType = SYSTEM,
            default = 0,
            summary = getString(R.string.network_traffic_mode_title),
            entries = trafficModeEntriesArrayList,
            entryValues = trafficModeValuesArrayList
        )
        buildSwitch(
            trafficList,
            iconID = R.drawable.ic_traffic,
            subtitle = getString(R.string.traffic_meter_title),
            accentColor = R.color.blue_800,
            feature = "network_traffic_autohide",
            featureType = SYSTEM,
            summary = getString(R.string.traffic_meter_treshold_summary),
            enabled = false
        )
        buildListSheet(
            trafficList,
            iconID = R.drawable.ic_traffic,
            title = getString(R.string.nothing),
            subtitle = getString(R.string.traffic_meter_title),
            accentColor = R.color.blue_800,
            feature = "network_traffic_unit_type",
            featureType = SYSTEM,
            default = 0,
            summary = getString(R.string.network_traffic_units_title),
            entries = trafficUnitsEntriesArrayList,
            entryValues = trafficUnitsValuesArrayList
        )
        setupLayout(trafficList, R.id.sectionTraffic)
        buildSwitch(
            batteryLightList,
            iconID = R.drawable.round_battery_full_white_36dp,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.battery_light_title),
            accentColor = R.color.purple_500,
            feature = featureManager.System().BATTERY_LIGHT_ENABLED,
            featureType = SYSTEM,
            summary = getString(R.string.show_batterylight),
            enabled = true
        )
        buildSwitch(
            batteryLightList,
            iconID = R.drawable.ic_dnd,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.battery_light_dnd),
            accentColor = R.color.orange_500,
            feature = featureManager.System().BATTERY_LIGHT_ALLOW_ON_DND,
            featureType = SYSTEM,
            summary = getString(R.string.show_batterylight_dnd)
        )
        buildSwitch(
            batteryLightList,
            iconID = R.drawable.ic_light,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.blinking),
            accentColor = R.color.pink_500,
            feature = featureManager.System().BATTERY_LIGHT_LOW_BLINKING,
            featureType = SYSTEM,
            summary = getString(R.string.blinking_on_low)
        )
        val pref = requireActivity().getSharedPreferences(
            SettingsConstants.SETTINGS_PREF,
            Context.MODE_PRIVATE
        )
        if (ResourceHelper.hasRGBLed(requireContext())) {
            buildRGB(
                list = batteryLightList,
                iconID = R.drawable.ic_light,
                subtitle = getString(R.string.led_color),
                feature = featureManager.System().BATTERY_LIGHT_REALLYFULL_COLOR,
                featureType = SYSTEM,
                summary = getString(R.string.light_charged),
                defaultColor = resources.getColor(R.color.purple_500, null)
            ) {}
            buildRGB(
                list = batteryLightList,
                iconID = R.drawable.ic_light,
                subtitle = getString(R.string.led_color),
                feature = featureManager.System().BATTERY_LIGHT_FULL_COLOR,
                featureType = SYSTEM,
                summary = getString(R.string.light_full),
                defaultColor = resources.getColor(R.color.green_500, null)
            ) {}
            buildRGB(
                list = batteryLightList,
                iconID = R.drawable.ic_light,
                subtitle = getString(R.string.led_color),
                feature = featureManager.System().BATTERY_LIGHT_LOW_COLOR,
                featureType = SYSTEM,
                summary = getString(R.string.light_low),
                defaultColor = resources.getColor(R.color.orange_500, null)
            ) {}
            buildRGB(
                list = batteryLightList,
                iconID = R.drawable.ic_light,
                subtitle = getString(R.string.led_color),
                feature = featureManager.System().BATTERY_LIGHT_MEDIUM_COLOR,
                featureType = SYSTEM,
                summary = getString(R.string.light_medium),
                defaultColor = resources.getColor(R.color.blue_500, null)
            ) {}
        }
        setupLayout(batteryLightList, R.id.sectionBatteryLight)
        buildSwitch(
            clockList,
            iconID = R.drawable.ic_alarm_clock,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.show_clock_tile),
            accentColor = R.color.blue_700,
            feature = featureManager.System().STATUSBAR_CLOCK,
            featureType = SYSTEM,
            summary = getString(R.string.show_clock_summary),
            enabled = true
        )
        buildSwipeable(
            list = clockList,
            iconID = R.drawable.ic_alarm_clock,
            subtitle = getString(R.string.clock_position_title),
            accentColor = R.color.teal_500,
            feature = featureManager.System().STATUSBAR_CLOCK_STYLE,
            featureType = SYSTEM,
            min = 0,
            max = 2,
            default = 0,
            summary = getString(R.string.clock_position_summary),
            extraTitle = getString(R.string.clock)
        ) { position, title ->
            run {
                var newTitle = ""
                when (position) {
                    0 -> newTitle = getString(R.string.left_clock)
                    1 -> newTitle = getString(R.string.center_clock)
                    2 -> newTitle = getString(R.string.right_clock)
                }
                title.text = newTitle
            }
        }
        buildSwitch(
            clockList,
            iconID = R.drawable.ic_alarm_clock,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.show_seconds_title),
            accentColor = R.color.orange_600,
            feature = featureManager.System().STATUSBAR_CLOCK_SECONDS,
            featureType = SYSTEM,
            summary = getString(R.string.show_seconds_summary)
        )
        buildSwipeable(
            list = clockList,
            iconID = R.drawable.ic_alarm_clock,
            subtitle = getString(R.string.ampm_title),
            accentColor = R.color.cyan_800,
            feature = featureManager.System().STATUSBAR_CLOCK_AM_PM_STYLE,
            featureType = SYSTEM,
            min = 0,
            max = 2,
            default = 2,
            summary = getString(R.string.ampm_summary),
            extraTitle = getString(R.string.clock)
        ) { position, title ->
            run {
                var newTitle = ""
                when (position) {
                    0 -> newTitle = getString(R.string.normal_ampm)
                    1 -> newTitle = getString(R.string.small_ampm)
                    2 -> newTitle = getString(R.string.no_ampm)
                }
                title.text = newTitle
            }
        }
        buildSwipeable(
            list = clockList,
            iconID = R.drawable.ic_alarm_clock,
            subtitle = getString(R.string.show_date_title),
            accentColor = R.color.red_600,
            feature = featureManager.System().STATUSBAR_CLOCK_DATE_DISPLAY,
            featureType = SYSTEM,
            min = 0,
            max = 2,
            default = 0,
            summary = getString(R.string.show_date_summary),
            extraTitle = getString(R.string.clock)
        ) { position, title ->
            run {
                var newTitle = ""
                when (position) {
                    0 -> newTitle = getString(R.string.no_date)
                    1 -> newTitle = getString(R.string.small_date)
                    2 -> newTitle = getString(R.string.normal_date)
                }
                title.text = newTitle
            }
        }
        setupLayout(clockList, R.id.sectionClock)
        buildSwitch(
            extrasList,
            iconID = R.drawable.ic_color_icon,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.statusbar_icons_style),
            accentColor = R.color.orange_600,
            feature = featureManager.System().STATUS_BAR_COLORED_ICONS_STYLES,
            featureType = SYSTEM,
            summary = getString(R.string.statusbar_icons_style_summary),
            enabled = false
        )
        setupLayout(extrasList, R.id.sectionStatusBarExtras)
    }
}