package com.dot.customizations.model.color

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.model.CustomizationSectionController.CustomizationSectionNavigationController
import com.dot.customizations.picker.color.MonetSettingsSectionView

class MonetSectionController(
    private val navigationController: CustomizationSectionNavigationController
) : CustomizationSectionController<MonetSettingsSectionView?> {

    override fun createView(context: Context): MonetSettingsSectionView {
        val mColorSectionView = LayoutInflater.from(context)
            .inflate(R.layout.monet_section_view, null as ViewGroup?) as MonetSettingsSectionView
        mColorSectionView.setOnClickListener {
            navigationController.navigateTo(
                MonetSettingsFragment.newInstance(context.getString(R.string.monet_title))
            )
        }
        return mColorSectionView
    }

    override fun isAvailable(context: Context?): Boolean {
        return context != null && ColorUtils.isMonetEnabled(context)
    }

}