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

import android.content.res.ColorStateList
import android.content.res.MonetWannabe
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SYSTEM
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.custom.utils.TelephonyUtils
import com.android.settings.dotextras.system.FeatureManager
import com.android.settings.dotextras.system.MonetManager

open class QSSection : GenericSection() {

    private var qsList: ArrayList<ContextCards> = ArrayList()
    private var rows_columnsList: ArrayList<ContextCards> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_qs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val featureManager = FeatureManager(requireActivity().contentResolver)
        /**
         * Clean
         */
        qsList.clear()
        rows_columnsList.clear()
        /**
         * Header
         */
        val accentColor: Int = ResourceHelper.getAccent(requireContext())
        val button = view.findViewById<ImageView>(R.id.accButton)
        val image = view.findViewById<ImageView>(R.id.accHeader)
        button.imageTintList = ColorStateList.valueOf(accentColor)
        image.imageTintList = ColorStateList.valueOf(accentColor)
        /**
         * Options
         */
        if (TelephonyUtils.isSimAvailable(requireContext())) {
            buildSwitch(
                qsList,
                iconID = R.drawable.ic_sim,
                title = getString(R.string.disabled),
                subtitle = getString(R.string.data_usage_title),
                accentColor = R.color.green_800,
                feature = featureManager.System().QS_SHOW_DATA_USAGE,
                featureType = SYSTEM,
                summary = getString(R.string.data_usage_summary)
            )
        }
        buildSwitch(
            qsList,
            iconID = R.drawable.ic_qs_title,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.qs_title),
            accentColor = R.color.dot_red,
            feature = featureManager.System().QS_TILE_TITLE_VISIBILITY,
            featureType = SYSTEM,
            enabled = true
        )
        if (!MonetWannabe.isMonetEnabled(context))
        buildSwitch(
            qsList,
            iconID = R.drawable.ic_settings_aosp,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.qs_tiles_accent_v2),
            accentColor = R.color.blue_800,
            feature = featureManager.System().QS_PANEL_BG_USE_NEW_TINT,
            featureType = SYSTEM,
            summary = getString(R.string.qs_tiles_accent_v2_summary),
            enabled = true
        )
        setupLayout(qsList, R.id.sectionQS)
        createBalloon(R.string.click_to_toggle, 0, R.id.sectionQS)
        /**
         * Rows & Columns
         */
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = getString(R.string.qs_columns),
            accentColor = R.color.dot_red,
            feature = featureManager.System().QS_COLUMNS_PORTRAIT,
            featureType = SYSTEM,
            min = 1,
            max = 7,
            default = 4,
            summary = getString(R.string.portrait),
            extraTitle = getString(R.string.columns)
        )
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = getString(R.string.qs_columns),
            accentColor = R.color.dot_pink,
            feature = featureManager.System().QS_COLUMNS_LANDSCAPE,
            featureType = SYSTEM,
            min = 1,
            max = 9,
            default = 4,
            summary = getString(R.string.landscape),
            extraTitle = getString(R.string.columns)
        )
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = getString(R.string.qs_rows),
            accentColor = R.color.dot_violet,
            feature = featureManager.System().QS_ROWS_PORTRAIT,
            featureType = SYSTEM,
            min = 1,
            max = 5,
            default = 3,
            summary = getString(R.string.portrait),
            extraTitle = getString(R.string.rows)
        )
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = getString(R.string.qs_rows),
            accentColor = R.color.dot_green,
            feature = featureManager.System().QS_ROWS_LANDSCAPE,
            featureType = SYSTEM,
            min = 1,
            max = 3,
            default = 1,
            summary = getString(R.string.landscape),
            extraTitle = getString(R.string.rows)
        )
        setupLayout(rows_columnsList, R.id.sectionRows)
    }
}