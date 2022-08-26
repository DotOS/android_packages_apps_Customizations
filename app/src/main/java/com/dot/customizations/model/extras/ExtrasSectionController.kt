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
package com.dot.customizations.model.extras

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.picker.extras.ExtrasSectionView

class ExtrasSectionController(private val mSectionNavigationController:
                              CustomizationSectionController.CustomizationSectionNavigationController) :
    CustomizationSectionController<ExtrasSectionView> {

    @SuppressLint("InflateParams")
    override fun createView(context: Context?): ExtrasSectionView {
        val sectionView = LayoutInflater.from(context)
            .inflate(R.layout.extras_section_view, null) as ExtrasSectionView
        val targetFragment =
            ExtrasFragment.newInstance(context!!.getString(R.string.extras_title))
        sectionView.setOnClickListener {
            mSectionNavigationController.navigateTo(
                targetFragment
            )
        }
        return sectionView
    }

    override fun isAvailable(context: Context?): Boolean {
        return true
    }
}