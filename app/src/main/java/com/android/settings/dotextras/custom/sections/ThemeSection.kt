package com.android.settings.dotextras.custom.sections

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.custom.views.AccentColorController
import com.android.settings.dotextras.custom.views.ColorSheet
import com.android.settings.dotextras.custom.views.TwoToneAccentView

class ThemeSection : GenericSection() {

    private lateinit var twoToneAccentView: TwoToneAccentView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.section_themes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as BaseActivity).setTitle(getString(R.string.section_themes_title))
        twoToneAccentView = view.findViewById(R.id.twoTone)
        twoToneAccentView.bindWhiteColor(
            ResourceHelper.getAccent(
                requireContext(),
                Configuration.UI_MODE_NIGHT_NO
            )
        )
        twoToneAccentView.bindDarkColor(
            ResourceHelper.getAccent(
                requireContext(),
                Configuration.UI_MODE_NIGHT_YES
            )
        )
        val accentController: AccentColorController = view.findViewById(R.id.accentController)
        accentController.updateVisibility(
            resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
        )
        twoToneAccentView.setOnTwoTonePressed {
            val colorSheet = ColorSheet()
            colorSheet.colorPicker(resources.getIntArray(R.array.materialColors),
                onResetListener = {
                    if (it == TwoToneAccentView.Shade.LIGHT)
                        featureManager.AccentManager().resetLight()
                    if (it == TwoToneAccentView.Shade.DARK)
                        featureManager.AccentManager().resetDark()
                },
                listener = { color ->
                    if (it == TwoToneAccentView.Shade.LIGHT)
                        twoToneAccentView.bindWhiteColor(color)
                    if (it == TwoToneAccentView.Shade.DARK)
                        twoToneAccentView.bindDarkColor(color)
                    accentController.updateVisibility(
                        resources.configuration.uiMode and
                                Configuration.UI_MODE_NIGHT_MASK
                    )
                }).show(requireActivity().supportFragmentManager)
        }
    }

}