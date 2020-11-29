package com.android.settings.dotextras.custom.sections

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.android.settings.dotextras.R

class QSSection : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.qs, rootKey);
    }

}