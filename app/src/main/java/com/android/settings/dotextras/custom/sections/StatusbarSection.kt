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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.batterystyles.BatteryStylesAdapter
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SYSTEM

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
        val batteryLightOptList = ArrayList<ContextCards>()
        val batteryLightList = ArrayList<ContextCards>()
        buildPager(list,
            iconID = R.drawable.round_battery_full_white_36dp,
            title = getString(R.string.battery_styles),
            accentColor = R.color.colorAccent,
            feature = featureManager.System().STATUS_BAR_BATTERY_STYLE,
            featureType = SYSTEM,
            pagerAdapter = BatteryStylesAdapter(requireActivity()))
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
        buildSwipeable(
            list = perclist,
            iconID = R.drawable.round_battery_unknown_white_36dp,
            subtitle = getString(R.string.qs_percentage_style),
            accentColor = R.color.red_500,
            feature = featureManager.System().QS_SHOW_BATTERY_PERCENT,
            featureType = SYSTEM,
            min = 0,
            max = 1,
            default = 0,
            summary = getString(R.string.quick_settings),
            extraTitle = getString(R.string.style)
        ) { position, title ->
            run {
                var newTitle = ""
                when (position) {
                    0 -> newTitle = getString(R.string.estimate)
                    1 -> newTitle = getString(R.string.percentage)
                }
                title.text = newTitle
            }
        }
        setupLayout(perclist, R.id.sectionPercentage)
        buildSwitch(batteryLightList,
            iconID = R.drawable.round_battery_full_white_36dp,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.battery_light_title),
            accentColor = R.color.purple_500,
            feature = featureManager.System().BATTERY_LIGHT_ENABLED,
            featureType = SYSTEM,
            summary = getString(R.string.show_batterylight))
        buildSwitch(batteryLightList,
            iconID = R.drawable.ic_dnd,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.battery_light_dnd),
            accentColor = R.color.orange_500,
            feature = featureManager.System().BATTERY_LIGHT_ALLOW_ON_DND,
            featureType = SYSTEM,
            summary = getString(R.string.show_batterylight_dnd))
        buildSwitch(batteryLightList,
            iconID = R.drawable.ic_light,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.blinking),
            accentColor = R.color.pink_500,
            feature = featureManager.System().BATTERY_LIGHT_LOW_BLINKING,
            featureType = SYSTEM,
            summary = getString(R.string.blinking_on_low))
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
        setupLayout(batteryLightList, R.id.sectionBatteryLight)
    }
}