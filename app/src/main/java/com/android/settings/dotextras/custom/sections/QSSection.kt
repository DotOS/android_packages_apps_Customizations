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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SYSTEM
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.system.FeatureManager

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
        buildSwitch(qsList,
            iconID = R.drawable.ic_qs_title,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.qs_title),
            accentColor = R.color.dot_red,
            feature = featureManager.System().QS_TILE_TITLE_VISIBILITY,
            featureType = SYSTEM,
            enabled = true)
        setupLayout(qsList, R.id.sectionQS)
        /**
         * Rows & Columns
         */
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = "QS Columns",
            accentColor = R.color.dot_red,
            feature = featureManager.System().QS_COLUMNS_PORTRAIT,
            featureType = SYSTEM,
            min = 1,
            max = 7,
            default = 4,
            summary = "Portrait",
            extraTitle = "Column(s)"
        )
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = "QS Columns",
            accentColor = R.color.dot_pink,
            feature = featureManager.System().QS_COLUMNS_LANDSCAPE,
            featureType = SYSTEM,
            min = 1,
            max = 9,
            default = 4,
            summary = "Landscape",
            extraTitle = "Column(s)"
        )
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = "QS Rows",
            accentColor = R.color.dot_violet,
            feature = featureManager.System().QS_ROWS_PORTRAIT,
            featureType = SYSTEM,
            min = 1,
            max = 5,
            default = 3,
            summary = "Portrait",
            extraTitle = "Row(s)"
        )
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = "QS Rows",
            accentColor = R.color.dot_green,
            feature = featureManager.System().QS_ROWS_LANDSCAPE,
            featureType = SYSTEM,
            min = 1,
            max = 5,
            default = 1,
            summary = "Landscape",
            extraTitle = "Row(s)"
        )
        setupLayout(rows_columnsList, R.id.sectionRows)
    }
}