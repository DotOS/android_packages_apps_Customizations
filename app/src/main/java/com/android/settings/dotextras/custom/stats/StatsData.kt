package com.android.settings.dotextras.custom.stats

import android.content.Context
import android.os.SystemProperties
import android.telephony.TelephonyManager
import android.text.TextUtils

class StatsData {
    var codeName: String? = null
        get() = SystemProperties.get(Constants.KEY_DEVICE)
        set(device) {
            field = if (TextUtils.isEmpty(device)) "unknown" else device
        }
    private var version: String? = null
    var buildType: String? = null
        get() = SystemProperties.get(Constants.KEY_BUILD_TYPE)
        set(buildType) {
            field = if (TextUtils.isEmpty(buildType)) "unknown" else buildType
        }
    private var countryCode: String? = null
    var buildDate: String? = null
        get() = SystemProperties.get(Constants.KEY_BUILD_DATE)
        set(buildDate) {
            field = if (TextUtils.isEmpty(buildDate)) "unknown" else buildDate
        }

    fun getVersion(): String {
        return Constants.KEY_VERSION
    }

    fun setVersion(version: String?) {
        this.version = if (TextUtils.isEmpty(version)) "unknown" else version
    }

    fun getCountryCode(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.networkCountryIso
    }

    fun setCountryCode(countryCode: String?) {
        this.countryCode = if (TextUtils.isEmpty(countryCode)) "unknown" else countryCode
    }
}