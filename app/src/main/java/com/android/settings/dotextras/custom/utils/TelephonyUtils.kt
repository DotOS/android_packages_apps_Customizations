package com.android.settings.dotextras.custom.utils

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SubscriptionManager
import android.util.Log

object TelephonyUtils {

    @SuppressLint("MissingPermission")
    fun isSimAvailable(context: Context): Boolean {
        val sManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val infoSim1 = sManager.getActiveSubscriptionInfoForSimSlotIndex(0)
        val infoSim2 = sManager.getActiveSubscriptionInfoForSimSlotIndex(1)
        return infoSim1 != null || infoSim2 != null
    }

}