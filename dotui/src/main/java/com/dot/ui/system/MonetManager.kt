package com.dot.ui.system

import android.content.Context
import android.content.res.MonetWannabe

class MonetManager(val context: Context) {

    private val featureManager = FeatureManager(context.contentResolver)

    fun isEnabled(): Boolean = MonetWannabe.isMonetEnabled(context)

    fun enableMonet(enabled: Boolean) {
        featureManager.Secure().setInt(featureManager.Secure().MONET_ENGINE, if (enabled) 1 else 0)
    }

}