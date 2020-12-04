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
            val reloadAssets = omsClass.getMethod(
                "reloadAssets",
                String::class.java,
                Int::class.java
            )
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
        /**
         * Whether the torch launch gesture to long press the power button when the
         * screen is off should be enabled.
         *
         * 0: disabled
         * 1: long tap power for torch
         */
        val TORCH_POWER_BUTTON_GESTURE = "torch_power_button_gesture"

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

        /**
         * Whether allowing pocket service to register sensors and dispatch informations.
         * 0 = disabled
         * 1 = enabled
         * @author Carlo Savignano
         */
        val POCKET_JUDGE = "pocket_judge"

        /**
         * FOD recognizing animation
         */
        val FOD_RECOGNIZING_ANIMATION = "fod_recognizing_animation"

        /**
         * Enable statusbar double tap gesture on to put device to sleep
         */
        val DOUBLE_TAP_SLEEP_GESTURE = "double_tap_sleep_gesture"

        /**
         * Double tap on lockscreen to sleep
         */
        val DOUBLE_TAP_SLEEP_LOCKSCREEN = "double_tap_sleep_lockscreen"

        /**
         * FOD recognizing animation picker
         */
        val FOD_ANIM = "fod_anim"

        /**
         * FOD icon picker
         */
        val FOD_ICON = "fod_icon"

        /**
         * FOD pressed color
         */
        val FOD_COLOR = "fod_color"

        /**
         * How many rows to show in the qs panel when in portrait
         */
        val QS_ROWS_PORTRAIT = "qs_rows_portrait"

        /**
         * How many rows to show in the qs panel when in landscape
         */
        val QS_ROWS_LANDSCAPE = "qs_rows_landscape"

        /**
         * How many columns to show in the qs panel when in portrait
         */
        val QS_COLUMNS_PORTRAIT = "qs_columns_portrait"

        /**
         * How many columns to show in the qs panel when in landscape
         */
        val QS_COLUMNS_LANDSCAPE = "qs_columns_landscape"

        /**
         * Whether to display qs tile titles in the qs panel
         */
        val QS_TILE_TITLE_VISIBILITY = "qs_tile_title_visibility"

        /**
         * Three Finger Gesture from Oppo
         */
        val THREE_FINGER_GESTURE = "three_finger_gesture"

        /**
         * Volume panel on left
         */
        val VOLUME_PANEL_ON_LEFT = "volume_panel_on_left"

        /**
         * Screen off fod
         */
        val FOD_GESTURE = "fod_gesture"

        fun setInt(feature: String, value: Int) {
            Settings.System.putInt(contentResolver, feature, value)
        }

        fun setIntForUser(feature: String, value: Int) {
            Settings.System.putIntForUser(contentResolver, feature, value, UserHandle.USER_CURRENT)
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

        fun getIntForUser(feature: String): Int = Settings.System.getIntForUser(
            contentResolver,
            feature,
            UserHandle.USER_CURRENT
        )

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