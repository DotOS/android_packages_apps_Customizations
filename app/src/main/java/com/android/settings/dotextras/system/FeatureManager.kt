/*
 * Copyright (C) 2020 The dotOS Project & The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.dotextras.system

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import com.android.settings.dotextras.custom.utils.ResourceHelper


class FeatureManager(private val contentResolver: ContentResolver) {

    /**
     * Accent Manager
     */
    inner class AccentManager {

        private val RESET = "-1"

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

        fun isUsingRGBAccent(configuration: Int): Boolean {
            if (isMonetEnabled()) {
                return true
            }
            return when (configuration) {
                Configuration.UI_MODE_NIGHT_NO -> getLight() != "-1"
                Configuration.UI_MODE_NIGHT_YES -> getDark() != "-1"
                else -> false
            }
        }

        fun isMonetEnabled(): Boolean = Secure().getInt(Secure().MONET_ENGINE, 1) == 1
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
         * Type: int (0 for false, 1 for true)
         */
        val DOZE_ALWAYS_ON = "doze_always_on"

        /**
         * Gesture that wakes up the display, showing some version of the lock screen.
         */
        val DOZE_WAKE_LOCK_SCREEN_GESTURE = "doze_wake_screen_gesture"

        /**
         * Position of gesture bar length slider.
         * 0f = hide
         * 1f = full
         */
        val GESTURE_NAVBAR_LENGTH = "gesture_navbar_length"

        val ACCENT_DARK_SETTING = "accent_dark"

        val ACCENT_LIGHT_SETTING = "accent_light"

        val CLOCK_FACE_SETTING = "lock_screen_custom_clock_face"

        /**
         * Monet base accent.
         * Do not override.
         */
        val MONET_BASE_ACCENT = "monet_base_accent"

        /**
         * Monet Theme Engine Switch.
         * 1 - Enabled (Default)
         * 0 - Disabled
         */
        val MONET_ENGINE = "monet_engine"

        /**
         * Monet Theme Engine
         * Set amount of colors to be generated
         * from wallpaper.
         * Default = 16 (best one yet)
         * Lowering the amount of colors will decrease the accuracy (FASTER);
         * Increasing the amount of colors will increase the accuracy (SLOWER);
         */
        val MONET_COLOR_GEN = "monet_color_gen"

        /**
         * Monet Theme Engine
         * Set palette type
         * 0 - Vibrant (default)
         * 1 - Light Vibrant
         * 2 - Dark Vibrant
         * 3 - Dominant
         * 4 - Muted
         * 5 - Light Muted
         * 6 - Dark Muted
         */
        val MONET_PALETTE = "monet_palette"

        /**
         * Enable and disable Lockscreen visualizer
         */
        val LOCKSCREEN_VISUALIZER_ENABLED = "lockscreen_visualizer"

        val SYSUI_NAV_BAR_INVERSE = "sysui_nav_bar_inverse"

        fun disableAOD() {
            setInt(DOZE_ALWAYS_ON, 0)
        }

        fun enableAOD() {
            setInt(DOZE_ALWAYS_ON, 1)
        }

        fun enableDozeIfNeeded(context: Context) {
            if (ResourceHelper.hasAmbient(context) && getInt(DOZE_ENABLED, 0) != 1)
                setInt(DOZE_ENABLED, 1)
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

        fun setStringBool(feature: String, value: String): Boolean {
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
         * Three Finger Gesture from MIUI
         */
        val SWIPE_TO_SCREENSHOT = "swipe_to_screenshot"

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
         */
        val BATTERY_LIGHT_ENABLED = "battery_light_enabled"

        /**
         * Whether to show battery light when DND mode is active
         */
        val BATTERY_LIGHT_ALLOW_ON_DND = "battery_light_allow_on_dnd"

        /**
         * Whether to show blinking light when battery is low
         */
        val BATTERY_LIGHT_LOW_BLINKING = "battery_light_low_blinking"

        /**
         * Low battery charging color
         */
        val BATTERY_LIGHT_LOW_COLOR = "battery_light_low_color"

        /**
         * Medium battery charging color
         */
        val BATTERY_LIGHT_MEDIUM_COLOR = "battery_light_medium_color"

        /**
         * Full battery charging color
         */
        val BATTERY_LIGHT_FULL_COLOR = "battery_light_full_color"

        /**
         * Really full 100 battery charging color
         */
        val BATTERY_LIGHT_REALLYFULL_COLOR = "battery_light_reallyfull_color"

        /**
         * Whether to enable PULSE Edge lights
         */
        val AMBIENT_NOTIFICATION_LIGHT = "ambient_notification_light"

        /**
         * Hex Int Color for Custom Color mode
         */
        val AMBIENT_NOTIFICATION_LIGHT_COLOR = "ambient_notification_light_color"

        /**
         * 0 - Default Color
         * 1 - Accent Color
         * 2 - Custom Color (defined by AMBIENT_NOTIFICATION_LIGHT_COLOR)
         * 3 - Notification Color
         */
        val AMBIENT_NOTIFICATION_LIGHT_MODE = "ambient_notification_light_mode"

        /**
         * QS Tiles New Tint
         */
        val QS_PANEL_BG_USE_NEW_TINT = "qs_panel_bg_use_new_tint"

        /**
         * Wheter to show network traffic indicator in statusbar
         */
        val NETWORK_TRAFFIC_STATE = "network_traffic_state"

        /**
         * Network traffic inactivity threshold (default is 1 kBs)
         */
        val NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD = "network_traffic_autohide_threshold"

        /**
         * Lockscreen media art
         */
        val LOCKSCREEN_MEDIA_METADATA = "lockscreen_media_metadata"

        /**
         * Show or hide clock
         * 0 - hide
         * 1 - show (default)
         */
        val STATUSBAR_CLOCK = "statusbar_clock"

        /**
         * Style of clock
         * 0 - Left Clock (default)
         * 1 - Center Clock
         * 2 - Right Clock
         */
        val STATUSBAR_CLOCK_STYLE = "statusbar_clock_style"

        /**
         * Whether to show seconds next to clock in status bar
         * 0 - hide (default)
         * 1 - show
         */
        val STATUSBAR_CLOCK_SECONDS = "statusbar_clock_seconds"

        /**
         * AM/PM Style for clock options
         * 0 - Normal AM/PM
         * 1 - Small AM/PM
         * 2 - No AM/PM  (default)
         */
        val STATUSBAR_CLOCK_AM_PM_STYLE = "statusbar_clock_am_pm_style"

        /**
         * Shows custom date before clock time
         * 0 - No Date (default)
         * 1 - Small Date
         * 2 - Normal Date
         */
        val STATUSBAR_CLOCK_DATE_DISPLAY = "statusbar_clock_date_display"

        /**
         * Sets the date string style
         * 0 - Regular style (default)
         * 1 - Lowercase
         * 2 - Uppercase
         */
        val STATUSBAR_CLOCK_DATE_STYLE = "statusbar_clock_date_style"

        /**
         * Stores the java DateFormat string for the date
         */
        val STATUSBAR_CLOCK_DATE_FORMAT = "statusbar_clock_date_format"

        /**
         * Position of date
         * 0 - Left of clock
         * 1 - Right of clock
         */
        val STATUSBAR_CLOCK_DATE_POSITION = "statusbar_clock_date_position"

        /**
         * Use doubletap as doze pulse triggers
         */
        val DOZE_TRIGGER_DOUBLETAP = "doze_trigger_doubletap"

        /**
         * Control brightness by swiping on statusbar
         */
        val STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control"

        /**
         *  Statusbar Color icons
         */
        val STATUS_BAR_COLORED_ICONS_STYLES = "statusbar_icons_style"

        /**
         * Show current active data sim data usage
         */
        val QS_SHOW_DATA_USAGE = "qs_show_data_usage"

        /**
         * Whether the button backlight is only lit when pressed (and not when screen is touched)
         * The value is boolean (1 or 0).
         */
        val BUTTON_BACKLIGHT_ONLY_WHEN_PRESSED = "button_backlight_only_when_pressed"

        /**
         * Advanced reboot switch
         */
        val ADVANCED_REBOOT = "advanced_reboot"

        val NAVIGATION_BAR_MENU_ARROW_KEYS = "navigation_bar_menu_arrow_keys"

        val SCREEN_OFF_ANIMATION = "screen_off_animation"

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

        fun getString(feature: String): String =
            Settings.Global.getString(contentResolver, feature)
    }

    inner class Values {
        val ON: Int = 1
        val OFF: Int = 0
    }
}