package com.android.settings.dotextras.custom.sections

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.android.settings.dotextras.R

class IconsSection : PreferenceFragmentCompat() {

    @SuppressLint("RestrictedApi")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.icons, rootKey);
    }

}