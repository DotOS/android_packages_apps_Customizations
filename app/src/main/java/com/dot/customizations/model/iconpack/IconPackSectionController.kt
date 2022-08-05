package com.dot.customizations.model.iconpack

/**
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationManager
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.model.CustomizationSectionController.CustomizationSectionNavigationController
import com.dot.customizations.picker.iconpack.IconPackFragment
import com.dot.customizations.picker.iconpack.IconPackSectionView

/** A [CustomizationSectionController] for system icons.  */
class IconPackSectionController(
    private val mIconPackOptionsManager: IconPackManager,
    sectionNavigationController: CustomizationSectionNavigationController
) : CustomizationSectionController<IconPackSectionView?> {
    private val mSectionNavigationController: CustomizationSectionNavigationController

    init {
        mSectionNavigationController = sectionNavigationController
    }

    override fun isAvailable(context: Context?): Boolean {
        return mIconPackOptionsManager.isAvailable
    }

    override fun createView(context: Context): IconPackSectionView {
        val iconPackSectionView: IconPackSectionView = LayoutInflater.from(context)
            .inflate(R.layout.icon_section_view,null) as IconPackSectionView
        val sectionDescription: TextView =
            iconPackSectionView.findViewById(R.id.icon_section_description)
        val sectionTile: View = iconPackSectionView.findViewById(R.id.icon_section_tile)
        mIconPackOptionsManager.fetchOptions(object :
            CustomizationManager.OptionsFetchedListener<IconPackOption> {
            override fun onOptionsLoaded(options: List<IconPackOption>) {
                val activeOption = getActiveOption(options)
                sectionDescription.text = activeOption.title
                activeOption.bindThumbnailTile(sectionTile)
            }

            override fun onError(throwable: Throwable?) {
                if (throwable != null) {
                    Log.e(TAG, "Error loading icon options", throwable)
                }
                sectionDescription.setText(R.string.something_went_wrong)
                sectionTile.visibility = View.GONE
            }
        },  /* reload= */true)
        iconPackSectionView.setOnClickListener {
            mSectionNavigationController.navigateTo(
                IconPackFragment.newInstance(context.getString(R.string.icon_pack_title))
            )
        }
        return iconPackSectionView
    }

    private fun getActiveOption(options: List<IconPackOption>): IconPackOption {
        return options.stream()
            .filter { option: IconPackOption -> option.isActive(mIconPackOptionsManager) }
            .findAny() // For development only, as there should always be a grid set.
            .orElse(options[0])
    }

    companion object {
        private const val TAG = "IconPackSectionController"
    }
}