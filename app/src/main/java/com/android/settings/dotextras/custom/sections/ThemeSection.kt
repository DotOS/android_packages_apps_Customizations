package com.android.settings.dotextras.custom.sections

import android.content.Context
import android.content.om.IOverlayManager
import android.os.Bundle
import android.os.ServiceManager
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.system.FeatureManager
import com.android.settings.dotextras.system.OverlayController
import com.android.settings.dotextras.custom.utils.ColorSheetUtils
import com.android.settings.dotextras.custom.views.ColorSheet

class ThemeSection : PreferenceFragmentCompat() {

    private val ACCENT_CONTROLLER_LEGACY = "accent_default"
    private val ACCENT_CONTROLLER_RGB = "accent_choice"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.themes, rootKey)
        val featureManager = FeatureManager(requireActivity().contentResolver)
        val accentPref: Preference = findPreference(ACCENT_CONTROLLER_RGB)!!
        accentPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val colorSheet = ColorSheet()
            colorSheet.colorPicker(
                colors = resources.getIntArray(R.array.materialColors),
                listener = { color ->
                    featureManager.AccentManager().apply(ColorSheetUtils.colorToHex(color).replace("#", ""))
                    colorSheet.dismiss()
                }).show(requireActivity().supportFragmentManager)
            true
        }
        val accentDef: ListPreference = findPreference(ACCENT_CONTROLLER_LEGACY)!!
        val overlayController = OverlayController(OverlayController.Categories.ACCENT_CATEGORY,
            requireActivity().packageManager,
            IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE)))
        accentDef.isEnabled = overlayController.isAvailable()
        accentDef.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            overlayController.Analog(accentDef).setOverlay((newValue as String))
            true
        }
        overlayController.Analog(accentDef).updatePreferenceOverlays()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as BaseActivity).setTitle(getString(R.string.section_themes_title))
    }

}