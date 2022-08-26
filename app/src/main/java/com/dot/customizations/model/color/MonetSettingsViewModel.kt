package com.dot.customizations.model.color

import android.app.Application
import android.content.Context
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.model.extras.PreferenceViewModel
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.screen

class MonetSettingsViewModel(app: Application) : PreferenceViewModel(app) {

    var navigationController:
            CustomizationSectionController.CustomizationSectionNavigationController? = null

    override var preferencesAdapter: PreferencesAdapter? = PreferencesAdapter(createScreen(getApplication()))

    private fun createScreen(context: Context) = screen(context) {
        title = context.getString(R.string.monet_title)

        secureSettingsSwitch(
            context,
            mTitleRes = R.string.system_black_theme_title,
            mSummaryRes = R.string.system_black_theme_summary,
            mDefault = 0,
            setting = "system_black_theme"
        )

        secureSettingsSeekBar(
            context,
            mTitleRes = R.string.monet_engine_white_luminance_user_title,
            mMin = 200,
            mMax = 1000,
            mDefault = 425,
            mStep = 5,
            setting = "monet_engine_white_luminance_user"
        )

        secureSettingsSwitch(
            context,
            mTitleRes = R.string.monet_engine_accurate_shades_title,
            mDefault = 1,
            setting = "monet_engine_accurate_shades"
        )

        secureSettingsSeekBar(
            context,
            mTitleRes = R.string.monet_engine_chroma_factor_title,
            mMin = 50,
            mMax = 400,
            mDefault = 100,
            mStep = 5,
            setting = "monet_engine_chroma_factor"
        )

        secureSettingsSwitch(
            context,
            mTitleRes = R.string.monet_engine_linear_lightness_title,
            mDefault = 0,
            setting = "monet_engine_linear_lightness"
        )

    }

}