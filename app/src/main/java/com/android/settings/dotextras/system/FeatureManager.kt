package com.android.settings.dotextras.system

import android.content.ContentResolver
import android.content.res.Configuration
import android.os.UserHandle
import android.provider.Settings
import android.view.Display


class FeatureManager(private val contentResolver: ContentResolver) {

    /**
     * Accent Manager
     */
    inner class AccentManager() {

        private val RESET = "-1"

        /** OLD Implementation
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
        }*/

        fun applyLight(lightColor: String) {
            Secure().setString(Secure().ACCENT_LIGHT_SETTING, lightColor)
        }

        fun applyDark(darkColor: String) {
            Secure().setString(Secure().ACCENT_DARK_SETTING, darkColor)
        }

        fun apply(lightColor: String, darkColor: String) {
            applyLight(lightColor)
            applyDark(darkColor)
        }

        fun getDark(): String {
            return Secure().getString(Secure().ACCENT_DARK_SETTING) ?: RESET
        }

        fun getLight(): String {
            return Secure().getString(Secure().ACCENT_LIGHT_SETTING) ?: RESET
        }

        fun reset() {
            applyDark(RESET)
            applyLight(RESET)
        }

        fun resetLight() {
            applyLight(RESET)
        }

        fun resetDark() {
            applyDark(RESET)
        }

        fun isUsingRGBAccent(configuration: Int): Boolean = when(configuration) {
            Configuration.UI_MODE_NIGHT_NO -> getLight() != "-1"
            Configuration.UI_MODE_NIGHT_YES -> getDark() != "-1"
            else -> false
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

        /**
         * Whether the device should wake when the wake gesture sensor detects motion.
         * @hide
         */
        val WAKE_GESTURE_ENABLED = "wake_gesture_enabled"

        /**
         * Whether the device should doze if configured.
         */
        val DOZE_ENABLED = "doze_enabled"

        /**
         * Indicates whether doze should be always on.
         *
         *
         * Type: int (0 for false, 1 for true)
         *
         */
        val DOZE_ALWAYS_ON = "doze_always_on"

        /**
         * Whether the device should pulse on pick up gesture.
         */
        val DOZE_PICK_UP_GESTURE = "doze_pulse_on_pick_up"

        /**
         * Whether the device should pulse on long press gesture.
         */
        val DOZE_PULSE_ON_LONG_PRESS = "doze_pulse_on_long_press"

        /**
         * Whether the device should pulse on double tap gesture.
         */
        val DOZE_DOUBLE_TAP_GESTURE = "doze_pulse_on_double_tap"

        /**
         * Whether the device should respond to the SLPI tap gesture.
         */
        val DOZE_TAP_SCREEN_GESTURE = "doze_tap_gesture"

        /**
         * Gesture that wakes up the display, showing some version of the lock screen.
         */
        val DOZE_WAKE_LOCK_SCREEN_GESTURE = "doze_wake_screen_gesture"

        /**
         * Gesture that wakes up the display, toggling between [Display.STATE_OFF] and
         * [Display.STATE_DOZE].
         */
        val DOZE_WAKE_DISPLAY_GESTURE = "doze_wake_display_gesture"

        /**
         * Position of gesture bar length slider.
         * 0f = hide
         * 1f = full
         */
        val GESTURE_NAVBAR_LENGTH = "gesture_navbar_length"

        val ACCENT_DARK_SETTING = "accent_dark"

        val ACCENT_LIGHT_SETTING = "accent_light"

        val CLOCK_FACE_SETTING = "lock_screen_custom_clock_face"

        fun disableAOD() {
            setInt(DOZE_ALWAYS_ON, 0)
        }

        fun enableAOD() {
            setInt(DOZE_ALWAYS_ON, 1)
        }

        fun enableAmbient() {
            setInt(DOZE_ENABLED, 1)
        }

        fun disableAmbient() {
            setInt(DOZE_ENABLED, 0)
        }

        fun setInt(feature: String, value: Int) {
            Settings.Secure.putInt(contentResolver, feature, value)
        }

        fun setLong(feature: String, value: Long) {
            Settings.Secure.putLong(contentResolver, feature, value)
        }

        fun setFloat(feature: String, value: Float) {
            Settings.Secure.putFloat(contentResolver, feature, value)
        }

        fun setStringBool(feature: String, value: String) : Boolean {
            return Settings.Secure.putString(contentResolver, feature, value)
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

        fun getString(feature: String): String? =
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

        /**
         * FOD night light
         */
        val FOD_NIGHT_LIGHT = "fod_night_light"

        /**
         * Unlock keystore with fingerprint after reboot
         */
        val FP_UNLOCK_KEYSTORE = "fp_unlock_keystore"

        /**
         * Setting to determine whether or not to show the battery percentage in the qs status bar header.
         * 0 - Show remaining time
         * 1 - Show percentage
         */
        val QS_SHOW_BATTERY_PERCENT = "qs_header_show_battery_percent"

        /**
         * Battery style
         * 0 - Portrait
         * 1 - Circle
         * 2 - Dotted Circle
         * 3 - Filled Circle
         * 4 - Text
         * 5 - Hidden
         */
        val STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style"

        /**
         * Statusbar Battery %
         * 0: Hide the battery percentage
         * 1: Display the battery percentage inside the icon
         * 2: Display the battery percentage next to Icon
         */
        val STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent"

        /**
         * Whether to allow battery light
         * @hide
         */
        val BATTERY_LIGHT_ENABLED = "battery_light_enabled"

        /**
         * Whether to show battery light when DND mode is active
         * @hide
         */
        val BATTERY_LIGHT_ALLOW_ON_DND = "battery_light_allow_on_dnd"

        /**
         * Whether to show blinking light when battery is low
         * @hide
         */
        val BATTERY_LIGHT_LOW_BLINKING = "battery_light_low_blinking"

        /**
         * Low battery charging color
         * @hide
         */
        val BATTERY_LIGHT_LOW_COLOR = "battery_light_low_color"

        /**
         * Medium battery charging color
         * @hide
         */
        val BATTERY_LIGHT_MEDIUM_COLOR = "battery_light_medium_color"

        /**
         * Full battery charging color
         * @hide
         */
        val BATTERY_LIGHT_FULL_COLOR = "battery_light_full_color"

        /**
         * Really full 100 battery charging color
         * @hide
         */
        val BATTERY_LIGHT_REALLYFULL_COLOR = "battery_light_reallyfull_color"

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