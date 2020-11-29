package com.android.settings.dotextras.custom.sections

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.android.settings.dotextras.R
import com.android.settings.dotextras.system.FeatureManager

open class SystemSection : PreferenceFragmentCompat() {

    private val HOLD_POWER_TO_TORCH = "hold_power_to_torch"

    var featureManager: FeatureManager? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.system, rootKey)
        featureManager = FeatureManager(requireActivity().contentResolver)
        addSwitchFeatureControl(
            findPreference(HOLD_POWER_TO_TORCH),
            featureManager!!.Secure().TORCH_POWER_BUTTON_GESTURE
        )
    }

    private fun addSwitchFeatureControl(
        preference: SwitchPreferenceCompat?,
        feature: String,
    ) {
        preference!!.isChecked = featureManager!!.Secure().getInt(feature) == 1
        preference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
                if (preference.isChecked) {
                    featureManager!!.Secure().setInt(
                        feature,
                        featureManager!!.Values().OFF
                    )
                    preference.isChecked = false
                } else {
                    featureManager!!.Secure().setInt(
                        feature,
                        featureManager!!.Values().ON
                    )
                    preference.isChecked = true
                }
                true
            }
    }

}