package com.android.settings.dotextras.system

import android.content.ContentResolver
import android.content.Context
import android.content.om.IOverlayManager
import android.os.RemoteException
import android.os.ServiceManager
import android.os.SystemProperties
import android.os.UserHandle
import android.provider.Settings

class FeatureManager(private val contentResolver: ContentResolver) {

    /**
     * Accent Manager
     */

    inner class AccentManager {

        private val RESET = "-1"
        private val SETTINGS_PACKAGE = "com.android.settings"
        private val SYSTEMUI_PACKAGE = "com.android.systemui"
        private val ACCENT_COLOR_PROP = "persist.sys.theme.accentcolor"

        fun apply(hexColor: String) {
            val omsClass = Class.forName("android.content.om.IOverlayManager")
            val overlayManager = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
            val reloadAndroidAssets = omsClass.getMethod("reloadAndroidAssets", Int::class.java)
            val reloadAssets = omsClass.getMethod("reloadAssets", String::class.java, Int::class.java)
            SystemProperties.set(ACCENT_COLOR_PROP, hexColor)
            try {
                reloadAndroidAssets.invoke(overlayManager, UserHandle.USER_CURRENT)
                reloadAssets.invoke(overlayManager, SETTINGS_PACKAGE, UserHandle.USER_CURRENT)
                reloadAssets.invoke(overlayManager, SYSTEMUI_PACKAGE, UserHandle.USER_CURRENT)
            } catch (ignored: RemoteException) {
            }
        }

        fun get(): String = SystemProperties.get(ACCENT_COLOR_PROP)

        fun reset() {
            apply(RESET)
        }
    }

    /**
     * Secure Settings
     */

    inner class Secure {
        val TORCH_POWER_BUTTON_GESTURE: String = "torch_power_button_gesture"
        val POCKET_JUDGE: String = "pocket_judge"

        fun setInt(feature: String, value: Int) {
            Settings.Secure.putInt(contentResolver, feature, value)
        }

        fun setLong(feature: String, value: Long) {
            Settings.Secure.putLong(contentResolver, feature, value)
        }

        fun setFloat(feature: String, value: Float) {
            Settings.Secure.putFloat(contentResolver, feature, value)
        }

        fun setString(feature: String, value: String) {
            Settings.Secure.putString(contentResolver, feature, value)
        }

        fun getInt(feature: String): Int = Settings.Secure.getInt(contentResolver, feature)

        fun getInt(feature: String, default: Int): Int =
            Settings.Secure.getInt(contentResolver, feature, default)

        fun getLong(feature: String): Long = Settings.Secure.getLong(contentResolver, feature)

        fun getLong(feature: String, default: Long): Long =
            Settings.Secure.getLong(contentResolver, feature, default)

        fun getFloat(feature: String): Float = Settings.Secure.getFloat(contentResolver, feature)

        fun getFloat(feature: String, default: Float): Float =
            Settings.Secure.getFloat(contentResolver, feature, default)

        fun getString(feature: String): String =
            Settings.Secure.getString(contentResolver, feature)
    }

    /**
     * System Settings
     */

    inner class System {
        fun setInt(feature: String, value: Int) {
            Settings.System.putInt(contentResolver, feature, value)
        }

        fun setLong(feature: String, value: Long) {
            Settings.System.putLong(contentResolver, feature, value)
        }

        fun setFloat(feature: String, value: Float) {
            Settings.System.putFloat(contentResolver, feature, value)
        }

        fun setString(feature: String, value: String) {
            Settings.System.putString(contentResolver, feature, value)
        }

        fun getInt(feature: String): Int = Settings.System.getInt(contentResolver, feature)

        fun getInt(feature: String, default: Int): Int =
            Settings.System.getInt(contentResolver, feature, default)

        fun getLong(feature: String): Long = Settings.System.getLong(contentResolver, feature)

        fun getLong(feature: String, default: Long): Long =
            Settings.System.getLong(contentResolver, feature, default)

        fun getFloat(feature: String): Float = Settings.System.getFloat(contentResolver, feature)

        fun getFloat(feature: String, default: Float): Float =
            Settings.System.getFloat(contentResolver, feature, default)

        fun getString(feature: String): String =
            Settings.System.getString(contentResolver, feature)
    }

    /**
     * Global Settings
     */

    inner class Global {
        fun setInt(feature: String, value: Int) {
            Settings.Global.putInt(contentResolver, feature, value)
        }

        fun setLong(feature: String, value: Long) {
            Settings.System.putLong(contentResolver, feature, value)
        }

        fun setFloat(feature: String, value: Float) {
            Settings.System.putFloat(contentResolver, feature, value)
        }

        fun setString(feature: String, value: String) {
            Settings.System.putString(contentResolver, feature, value)
        }

        fun getInt(feature: String): Int = Settings.Global.getInt(contentResolver, feature)

        fun getInt(feature: String, default: Int): Int =
            Settings.Global.getInt(contentResolver, feature, default)

        fun getLong(feature: String): Long = Settings.Global.getLong(contentResolver, feature)

        fun getLong(feature: String, default: Long): Long =
            Settings.Global.getLong(contentResolver, feature, default)

        fun getFloat(feature: String): Float = Settings.Global.getFloat(contentResolver, feature)

        fun getFloat(feature: String, default: Float): Float =
            Settings.Global.getFloat(contentResolver, feature, default)

        fun getString(feature: String): String =
            Settings.Global.getString(contentResolver, feature)
    }

    inner class Values {
        val ON: Int = 1
        val OFF: Int = 0
    }
}