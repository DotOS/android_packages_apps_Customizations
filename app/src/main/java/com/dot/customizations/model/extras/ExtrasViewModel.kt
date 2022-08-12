/*
 * Copyright (C) 2022 The DotOS Project
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
package com.dot.customizations.model.extras

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import com.android.internal.util.dot.DotUtils
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationSectionController
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.*
import de.Maxr1998.modernpreferences.preferences.choice.SelectionItem

/**
 * ViewModel class that controls 'Extras' preferences
 *
 * [systemSettingsSingleChoice] - Single Choice Dialog Preference
 *
 * [systemSettingsSeekBar] - SeekBar Preference
 *
 * [systemSettingsSwitch] - Switch Preference
 *
 * [fragmentPreference] - Launch a fragment from preference
 */
class ExtrasViewModel(app: Application) : AndroidViewModel(app) {

    init {
        Preference.Config.dialogBuilderFactory = { context ->
            AlertDialog.Builder(
                context,
                com.android.settingslib.R.style.Theme_AlertDialog_SettingsLib
            )
        }
    }

    var navigationController:
            CustomizationSectionController.CustomizationSectionNavigationController? = null

    val preferencesAdapter = PreferencesAdapter(createScreen(getApplication()))

    /**
     * Root method to add subScreens of extras
     *
     * [Required] For every Screen and subScreen set [Preference.title]
     *
     * @author IacobIonut01
     */
    private fun createScreen(context: Context) = screen(context) {
        title = context.getString(R.string.extras_title)
        sectionStatusBar(context)
        sectionNotifications(context)
        sectionQuickSettings(context)
        sectionGestures(context)
        pref("beta") {
            title = "Beta"
            summary = "Features list is incomplete and some might not work.\nDO NOT REPORT"
            enabled = false
        }
    }

    /**
     * [subScreen] Gestures
     */
    private fun PreferenceScreen.Builder.sectionGestures(context: Context): PreferenceScreen {
        return subScreen {
            title = context.getString(R.string.gestures_title)

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.status_bar_brightness_control_title,
                mSummaryRes = R.string.status_bar_brightness_control_summary,
                mDefault = 0,
                setting = "status_bar_brightness_control"
            )

            systemSettingsSingleChoice(
                context,
                mTitleRes = R.string.torch_power_button_gesture_title,
                mSelections = arrayListOf(
                    SelectionItem("0", R.string.torch_power_button_gesture_none),
                    SelectionItem("1", R.string.torch_power_button_gesture_dt),
                    SelectionItem("2", R.string.torch_power_button_gesture_lp),
                ),
                mDefault = 0,
                setting = "torch_power_button_gesture"
            )

            subScreen {
                title = context.getString(R.string.gestures_title)
                summary = context.getString(R.string.gestures_double_tap_summary)

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.double_tap_to_sleep_lockscreen_title,
                    mSummaryRes = R.string.double_tap_to_sleep_lockscreen_summary,
                    mDefault = 1,
                    setting = "double_tap_sleep_lockscreen"
                )

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.double_tap_to_sleep_title,
                    mSummaryRes = R.string.double_tap_to_sleep_summary,
                    mDefault = 1,
                    setting = "double_tap_sleep_gesture"
                )

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.double_tap_on_doze_to_wake_title,
                    mSummaryRes = R.string.double_tap_on_doze_to_wake_summary,
                    mDefault = 1,
                    setting = "doze_pulse_on_double_tap"
                )
            }
        }
    }

    /**
     * [subScreen] Notifications
     */
    private fun PreferenceScreen.Builder.sectionNotifications(context: Context): PreferenceScreen {
        return subScreen {
            title = context.getString(R.string.notifications_title)

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.alert_slider_notifications_title,
                mSummaryRes = R.string.alert_slider_notifications_summary,
                mDefault = 1,
                setting = "alert_slider_notifications"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.reticker_title,
                mSummaryRes = R.string.reticker_summary,
                mDefault = 0,
                setting = "reticker_status"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.reticker_colored_title,
                mSummaryRes = R.string.reticker_colored_summary,
                mDefault = 0,
                setting = "reticker_colored"
            ).apply {
                dependency = "reticker_status"
            }

            subScreen {
                title = context.getString(R.string.pulse_ambient_light_title)
                summaryRes = R.string.pulse_ambient_light_summary

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.ambient_notification_light_enabled_title,
                    mSummaryRes = R.string.ambient_notification_light_enabled_summary,
                    mDefault = 0,
                    setting = "ambient_notification_light_enabled"
                )

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.pulse_ambient_light_show_always_title,
                    mSummaryRes = R.string.pulse_ambient_light_show_always_summary,
                    mDefault = 0,
                    setting = "ambient_light_pulse_for_all"
                )

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.ambient_notification_light_hide_aod_title,
                    mSummaryRes = R.string.ambient_notification_light_hide_aod_summary,
                    mDefault = 0,
                    setting = "ambient_notification_light_hide_aod"
                )

                systemSettingsSeekBar(
                    context,
                    mTitleRes = R.string.ambient_notification_light_duration_title,
                    mSummaryRes = R.string.ambient_notification_light_duration_summary,
                    mMin = 1,
                    mMax = 5,
                    mDefault = 2,
                    setting = "ambient_notification_light_duration"
                )

                systemSettingsSeekBar(
                    context,
                    mTitleRes = R.string.ambient_notification_light_repeats_title,
                    mSummaryRes = R.string.ambient_notification_light_repeats_summary,
                    mMin = 0,
                    mMax = 10,
                    mDefault = 0,
                    setting = "ambient_notification_light_repeats"
                )

                systemSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.ambient_notification_color_mode_title,
                    mSelections = arrayListOf(
                        SelectionItem("0", R.string.ambient_notification_color_mode_notification),
                        SelectionItem("1", R.string.ambient_notification_color_mode_wall),
                        SelectionItem("2", R.string.ambient_notification_color_mode_accent),
                    ),
                    mDefault = 0,
                    setting = "ambient_notification_color_mode"
                )

                systemSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.pulse_ambient_light_repeat_direction_title,
                    mSelections = arrayListOf(
                        SelectionItem("0", R.string.pulse_ambient_light_repeat_direction_restart),
                        SelectionItem("1", R.string.pulse_ambient_light_repeat_direction_reverse),
                    ),
                    mDefault = 0,
                    setting = "ambient_light_repeat_direction"
                )

                systemSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.pulse_ambient_light_layout_title,
                    mSelections = arrayListOf(
                        SelectionItem("0", R.string.pulse_ambient_light_layout_faded),
                        SelectionItem("1", R.string.pulse_ambient_light_layout_solid),
                    ),
                    mDefault = 1,
                    setting = "ambient_light_layout"
                )

                systemSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.pulse_ambient_light_layout_title,
                    mSelections = arrayListOf(
                        SelectionItem("0", R.string.pulse_light_both),
                        SelectionItem("1", R.string.pulse_light_top_bottom),
                        SelectionItem("2", R.string.pulse_light_left_right),
                    ),
                    mDefault = 0,
                    setting = "pulse_light_layout_style"
                )

                systemSettingsSeekBar(
                    context,
                    mTitleRes = R.string.pulse_ambient_light_width_title,
                    mMin = 1,
                    mMax = 150,
                    mDefault = 125,
                    setting = "pulse_ambient_light_width"
                )
            }

            subScreen {
                title = context.getString(R.string.headsup_category)
                summaryRes = R.string.heads_up_notifications_summary

                globalSettingsSwitch(
                    context,
                    mTitleRes = R.string.heads_up_notifications,
                    mSummaryRes = R.string.summary_heads_up_disabled,
                    mSummaryOnRes = R.string.summary_heads_up_enabled,
                    mDefault = 1,
                    setting = "HEADS_UP_NOTIFICATIONS_ENABLED"
                )
                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.less_boring_heads_up_title,
                    mSummaryRes = R.string.less_boring_heads_up_summary,
                    mDefault = 0,
                    setting = "LESS_BORING_HEADS_UP"
                )
            }

            categoryHeader("counter") {
                titleRes = R.string.general_title
            }

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.status_bar_notif_count_title,
                mSummaryRes = R.string.status_bar_notif_count_summary,
                mDefault = 0,
                setting = "status_bar_notif_count"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.notification_sound_vib_screen_on_title,
                mSummaryRes = R.string.notification_sound_vib_screen_on_summary,
                mDefault = 1,
                setting = "notification_sound_vib_screen_on"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.notification_headers_title,
                mSummaryRes = R.string.notification_headers_summary,
                mDefault = 1,
                setting = "notification_headers"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.notification_guts_kill_app_button_title,
                mSummaryRes = R.string.notification_guts_kill_app_button_summary,
                mDefault = 0,
                setting = "notification_guts_kill_app_button"
            )

            categoryHeader("flashlightnt") {
                titleRes = R.string.flashlight_category
            }

            systemSettingsSingleChoice(
                context,
                mTitleRes = R.string.flashlight_on_call_title,
                mSelections = arrayListOf(
                    SelectionItem("0", R.string.flashlight_on_call_disabled),
                    SelectionItem("1", R.string.flashlight_on_call_ringer),
                    SelectionItem("2", R.string.flashlight_on_call_no_ringer),
                    SelectionItem("3", R.string.flashlight_on_call_silent),
                    SelectionItem("4", R.string.flashlight_on_call_always),
                ),
                mDefault = 0,
                setting = "flashlight_on_call"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.flashlight_on_call_ignore_dnd_title,
                mSummaryRes = R.string.flashlight_on_call_ignore_dnd_summary,
                mDefault = 0,
                setting = "flashlight_on_call_ignore_dnd"
            )

            systemSettingsSeekBar(
                context,
                mTitleRes = R.string.flashlight_on_call_rate_title,
                mSummaryRes = R.string.flashlight_on_call_rate_summary,
                mMin = 1,
                mMax = 5,
                mDefault = 1,
                setting = "flashlight_on_call_rate"
            )

            categoryHeader("vibing") {
                titleRes = R.string.incall_vibration_category
            }

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.incall_vibrate_connect_title,
                mDefault = 0,
                setting = "vibrate_on_connect"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.incall_vibrate_call_wait_title,
                mDefault = 0,
                setting = "vibrate_on_callwaiting"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.incall_vibrate_disconnect_title,
                mDefault = 0,
                setting = "vibrate_on_disconnect"
            )

        }
    }

    /**
     * [subScreen] Status Bar
     */
    private fun PreferenceScreen.Builder.sectionStatusBar(context: Context): PreferenceScreen {
        return subScreen {
            title = context.getString(R.string.status_bar_title)
            systemSettingsSingleChoice(
                context,
                mTitleRes = R.string.status_bar_clock_position_title,
                mSelections = arrayListOf(
                    SelectionItem("0", R.string.status_bar_clock_position_right),
                    SelectionItem("1", R.string.status_bar_clock_position_center),
                    SelectionItem("2", R.string.status_bar_clock_position_left),
                ),
                mDefault = 2,
                setting = "STATUS_BAR_CLOCK"
            )

            secureSettingsSwitch(
                context,
                mTitleRes = R.string.wifi_standard_title,
                mSummaryRes = R.string.wifi_standard_summary,
                mDefault = 0,
                setting = "show_wifi_standard_icon"
            )

            subScreen {
                title = context.getString(R.string.status_bar_clock_title)
                summaryRes = R.string.status_bar_clock_summary

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.status_bar_clock_auto_hide_title,
                    mSummaryRes = R.string.status_bar_clock_auto_hide_launcher_summary,
                    mDefault = 0,
                    setting = "status_bar_clock_auto_hide"
                )

                systemSettingsSeekBar(
                    context,
                    mTitleRes = R.string.status_bar_clock_auto_hide_hdur_title,
                    mSummaryRes = R.string.status_bar_clock_auto_hide_hdur_summary,
                    mMin = 5,
                    mMax = 300,
                    mDefault = 60,
                    setting = "status_bar_clock_auto_hide_hduration"
                ).apply {
                    dependency = "status_bar_clock_auto_hide"
                }

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.status_bar_clock_seconds_title,
                    mSummaryRes = R.string.status_bar_clock_seconds_summary,
                    mDefault = 0,
                    setting = "status_bar_clock_seconds"
                )

                systemSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.status_bar_am_pm_title,
                    mSelections = arrayListOf(
                        SelectionItem("2", R.string.status_bar_am_pm_hidden),
                        SelectionItem("0", R.string.status_bar_am_pm_normal),
                        SelectionItem("1", R.string.status_bar_am_pm_small),
                    ),
                    mDefault = 2,
                    setting = "status_bar_am_pm"
                )

                systemSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.status_bar_date_title,
                    mSelections = arrayListOf(
                        SelectionItem("0", R.string.status_bar_date_none),
                        SelectionItem("1", R.string.status_bar_date_small),
                        SelectionItem("2", R.string.status_bar_date_normal),
                    ),
                    mDefault = 0,
                    setting = "status_bar_clock_date_display"
                )

                systemSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.clock_date_position,
                    mSelections = arrayListOf(
                        SelectionItem("0", R.string.clock_date_left),
                        SelectionItem("1", R.string.clock_date_right),
                    ),
                    mDefault = 0,
                    setting = "status_bar_clock_date_position"
                )

                systemSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.status_bar_date_style,
                    mSelections = arrayListOf(
                        SelectionItem("0", R.string.status_bar_date_style_normal),
                        SelectionItem("1", R.string.status_bar_date_style_lowercase),
                        SelectionItem("2", R.string.status_bar_date_style_uppercase),
                    ),
                    mDefault = 0,
                    setting = "status_bar_clock_date_style"
                )
            }

            subScreen {
                title = context.getString(R.string.traffic_title)
                summaryRes = R.string.traffic_summary

                systemSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.network_traffic_location_title,
                    mSelections = arrayListOf(
                        SelectionItem("2", R.string.network_traffic_disabled),
                        SelectionItem("0", R.string.network_traffic_statusbar),
                        SelectionItem("1", R.string.network_traffic_qs_header),
                    ),
                    mDefault = 2,
                    setting = "network_traffic_location"
                ).apply {
                    onSelectionChange {
                        if (it.toInt() == 2) {
                            Settings.System.putInt(
                                context.contentResolver,
                                "network_traffic_state",
                                0
                            )
                        } else
                            Settings.System.putInt(
                                context.contentResolver,
                                "network_traffic_state",
                                1
                            )
                        Settings.System.putInt(
                            context.contentResolver,
                            "network_traffic_location",
                            it.toInt()
                        )
                    }
                }

                systemSettingsSeekBar(
                    context,
                    mTitleRes = R.string.network_traffic_autohide_threshold_title,
                    mMin = 0,
                    mMax = 10,
                    mDefault = 0,
                    setting = "network_traffic_autohide_threshold"
                )

                systemSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.network_traffic_mode_title,
                    mSelections = arrayListOf(
                        SelectionItem("0", R.string.network_traffic_dynamic),
                        SelectionItem("1", R.string.network_traffic_download),
                        SelectionItem("2", R.string.network_traffic_upload),
                    ),
                    mDefault = 0,
                    setting = "network_traffic_mode"
                )

                systemSettingsSeekBar(
                    context,
                    mTitleRes = R.string.network_traffic_refresh_interval_title,
                    mMin = 1,
                    mMax = 10,
                    mDefault = 1,
                    setting = "network_traffic_refresh_interval"
                )
            }

            subScreen {
                title = context.getString(R.string.battery_style_category_title)
                secureSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.status_bar_battery_style_title,
                    mSelections = arrayListOf(
                        SelectionItem("0", R.string.status_bar_battery_style_port),
                        SelectionItem("1", R.string.status_bar_battery_style_circle),
                        SelectionItem("2", R.string.status_bar_battery_style_dotted_circle),
                        SelectionItem("3", R.string.status_bar_battery_style_filled_circle),
                        SelectionItem("4", R.string.status_bar_battery_style_text),
                    ),
                    mDefault = 0,
                    setting = "status_bar_battery_style"
                )
                systemSettingsSingleChoice(
                    context,
                    mTitleRes = R.string.status_bar_battery_percentage_title,
                    mSelections = arrayListOf(
                        SelectionItem("0", R.string.status_bar_battery_percentage_default),
                        SelectionItem("1", R.string.status_bar_battery_percentage_text_inside),
                        SelectionItem("2", R.string.status_bar_battery_percentage_text_next),
                    ),
                    mDefault = 0,
                    setting = "status_bar_show_battery_percent"
                )
            }

            if (DotUtils.isVoiceCapable(context)) {

                categoryHeader("statusicons") {
                    titleRes = R.string.telephony_icons_title
                }

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.use_old_mobiletype_title,
                    mSummaryRes = R.string.use_old_mobiletype_summary,
                    mDefault = 0,
                    setting = "use_old_mobiletype"
                )

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.show_fourg_icon_title,
                    mSummaryRes = R.string.show_fourg_icon_summary,
                    mDefault = 0,
                    setting = "show_fourg_icon"
                )

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.volte_icon_title,
                    mSummaryRes = R.string.volte_icon_summary,
                    mDefault = 1,
                    setting = "show_volte_icon"
                )

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.vowifi_icon_title,
                    mSummaryRes = R.string.vowifi_icon_summary,
                    mDefault = 1,
                    setting = "show_vowifi_icon"
                )

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.data_disabled_icon_title,
                    mSummaryRes = R.string.data_disabled_icon_summary,
                    mDefault = 1,
                    setting = "data_disabled_icon"
                )

                systemSettingsSwitch(
                    context,
                    mTitleRes = R.string.roaming_indicator_icon_title,
                    mSummaryRes = R.string.roaming_indicator_icon_summary,
                    mDefault = 1,
                    setting = "roaming_indicator_icon"
                )

            }

            categoryHeader("misc") {
                titleRes = R.string.misc_title
            }

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.bluetooth_battery_title,
                mSummaryRes = R.string.bluetooth_battery_summary,
                mDefault = 1,
                setting = "bluetooth_show_battery"
            )

            secureSettingsSwitch(
                context,
                mTitleRes = R.string.mic_camera_privacy_indicator_title,
                mSummaryRes = R.string.mic_camera_privacy_indicator_summary,
                mDefault = 1,
                setting = "enable_camera_privacy_indicator"
            )

            secureSettingsSwitch(
                context,
                mTitleRes = R.string.location_privacy_indicator_title,
                mSummaryRes = R.string.location_privacy_indicator_summary,
                mDefault = 0,
                setting = "enable_location_privacy_indicator"
            )

            secureSettingsSwitch(
                context,
                mTitleRes = R.string.combined_status_bar_signal_icons,
                mSummaryRes = R.string.combined_status_bar_signal_icons_summary,
                mDefault = 0,
                setting = "show_combined_status_bar_signal_icons"
            )
        }
    }

    /**
     * [subScreen] Lockscreen
     */
    private fun PreferenceScreen.Builder.sectionLockscreen(context: Context): PreferenceScreen {
        return subScreen {
            title = context.getString(R.string.lockscreen_title)

            subScreen {
                title = context.getString(R.string.udfps_settings_title)
                summaryRes = R.string.udfps_settings_summary
            }

            categoryHeader("lockheader1") {
                titleRes = R.string.general_category
            }

            /*
            secureSettingsSwitch(
                context,
                mTitleRes = R.string.lockscreen_double_line_clock_setting_toggle,
                mSummaryRes = R.string.lockscreen_double_line_clock_summary,
                mDefault = 1,
                setting = "lockscreen_use_double_line_clock"
            )*/

        }
    }

    /**
     * [subScreen] Quick Settings
     */
    private fun PreferenceScreen.Builder.sectionQuickSettings(context: Context): PreferenceScreen {
        return subScreen("qs") {
            title = context.getString(R.string.section_qs_title)
            summaryRes = R.string.qs_section_summary

            systemSettingsSwitch(
                context,
                mTitle = "Animate tile state change",
                mSummary = "Tiles will keep their shape on all states",
                mSummaryOn = "Tiles will change their shapes :\n[Active] - Circle\n[Inactive/Disabled] - Rounded Rectangle\n[Change theme to apply changes or restart SystemUI]",
                mDefault = 1,
                setting = "QS_TILE_MORPH"
            )

            categoryHeader("qsbrightness") {
                titleRes = R.string.qs_brightness_slider_category
            }

            secureSettingsSingleChoice(
                context,
                mTitleRes = R.string.qs_show_brightness_slider_title,
                mSelections = arrayListOf(
                    SelectionItem("0", R.string.qs_show_brightness_slider_never),
                    SelectionItem("1", R.string.qs_show_brightness_slider_expanded),
                    SelectionItem("2", R.string.qs_show_brightness_slider_always),
                ),
                mDefault = 2,
                setting = "QS_SHOW_BRIGHTNESS_SLIDER"
            )

            secureSettingsSingleChoice(
                context,
                mTitleRes = R.string.qs_brightness_slider_position_title,
                mSelections = arrayListOf(
                    SelectionItem("0", R.string.qs_brightness_slider_position_top),
                    SelectionItem("1", R.string.qs_brightness_slider_position_bottom)
                ),
                mDefault = 1,
                setting = "QS_BRIGHTNESS_SLIDER_POSITION"
            )

            secureSettingsSwitch(
                context,
                mTitleRes = R.string.qs_show_auto_brightness_title,
                mSummaryRes = R.string.qs_show_auto_brightness_summary,
                mDefault = 1,
                setting = "QS_SHOW_AUTO_BRIGHTNESS"
            )

            categoryHeader("qsheader") {
                title = "Quick Settings Header"
            }

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.qs_clock_title,
                mSummaryRes = R.string.qs_clock_summary,
                mDefault = 1,
                setting = "SHOW_QS_CLOCK"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.qs_date_title,
                mSummaryRes = R.string.qs_date_summary,
                mDefault = 1,
                setting = "SHOW_QS_DATE"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.qstile_requires_unlocking_title,
                mSummaryRes = R.string.qstile_requires_unlocking_summary,
                mDefault = 1,
                setting = "DISABLE_SECURE_TILES_ON_LOCKSCREEN"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.qs_show_battery_estimate_title,
                mSummaryRes = R.string.qs_show_battery_estimate_summary_off,
                mSummaryOnRes = R.string.qs_show_battery_estimate_summary_on,
                mDefault = 1,
                setting = "QS_SHOW_BATTERY_ESTIMATE"
            )

            systemSettingsSingleChoice(
                context,
                mTitleRes = R.string.status_bar_quick_qs_pulldown,
                mSelections = arrayListOf(
                    SelectionItem("0", R.string.quick_pulldown_none),
                    SelectionItem("1", R.string.quick_pulldown_right),
                    SelectionItem("2", R.string.quick_pulldown_left),
                    SelectionItem("3", R.string.quick_pulldown_always)
                ),
                mDefault = 0,
                setting = "STATUS_BAR_QUICK_QS_PULLDOWN"
            )

            categoryHeader("qsmedia") {
                titleRes = R.string.media_category
            }

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.artwork_media_background_title,
                mSummaryRes = R.string.artwork_media_background_summary,
                mDefault = 0,
                setting = "ARTWORK_MEDIA_BACKGROUND"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.artwork_media_background_enable_blur_title,
                mDefault = 0,
                setting = "ARTWORK_MEDIA_BACKGROUND_ENABLE_BLUR"
            ).apply {
                dependency = "ARTWORK_MEDIA_BACKGROUND"
            }

            systemSettingsSeekBar(
                context,
                mTitleRes = R.string.artwork_media_background_alpha_title,
                mMin = 0,
                mMax = 255,
                mDefault = 255,
                setting = "ARTWORK_MEDIA_BACKGROUND_ALPHA"
            ).apply {
                dependency = "ARTWORK_MEDIA_BACKGROUND"
            }

            categoryHeader("qsfooter") {
                titleRes = R.string.qs_footer_category
            }

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.qs_footer_warnings_title,
                mSummaryRes = R.string.qs_footer_warnings_summary,
                mDefault = 1,
                setting = "QS_FOOTER_WARNINGS"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.qs_footer_users_icon_title,
                mSummaryRes = R.string.qs_footer_users_icon_summary,
                mDefault = 0,
                setting = "QS_FOOTER_SHOW_USER"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.qs_footer_edit_icon_title,
                mDefault = 1,
                setting = "QS_FOOTER_SHOW_EDIT"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.qs_footer_show_power_menu_title,
                mDefault = 1,
                setting = "QS_FOOTER_SHOW_POWER_MENU"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.qs_footer_services_icon_title,
                mDefault = 0,
                setting = "QS_FOOTER_SHOW_SERVICES"
            )

            systemSettingsSwitch(
                context,
                mTitleRes = R.string.qs_footer_settings_icon_title,
                mDefault = 1,
                setting = "QS_FOOTER_SHOW_SETTINGS"
            )
            /*
            categoryHeader("qsrowscol") {
                title = "Rows & Columns"
            }

            subScreen {
                title = "Tile Layout"
                summary = "QS Rows & Columns"

                categoryHeader("qsrows") {
                    title = "Quick Settings Rows"
                }

                systemSettingsSeekBar(context,
                    mTitle = "Portrait",
                    mMin = 1,
                    mMax = 6,
                    mDefault = 3,
                    setting = "QS_PANEL_ROWS_PORTRAIT"
                )

                systemSettingsSeekBar(context,
                    mTitle = "Landscape",
                    mMin = 1,
                    mMax = 3,
                    mDefault = 1,
                    setting = "QS_PANEL_ROWS_LANDSCAPE"
                )

                systemSettingsSeekBar(context,
                    mTitle = "Landscape (Media Player active)",
                    mMin = 1,
                    mMax = 3,
                    mDefault = 2,
                    setting = "QS_PANEL_ROWS_LANDSCAPE_MEDIA"
                )

                categoryHeader("qscolumns") {
                    title = "Quick Settings Columns"
                }

                systemSettingsSeekBar(context,
                    mTitle = "Portrait",
                    mMin = 3,
                    mMax = 6,
                    mDefault = 4,
                    setting = "QS_PANEL_COLUMNS_PORTRAIT"
                )

                systemSettingsSeekBar(context,
                    mTitle = "Landscape",
                    mMin = 2,
                    mMax = 6,
                    mDefault = 4,
                    setting = "QS_PANEL_COLUMNS_LANDSCAPE"
                )

                systemSettingsSeekBar(context,
                    mTitle = "Landscape (Media Player active)",
                    mMin = 2,
                    mMax = 6,
                    mDefault = 6,
                    setting = "QS_PANEL_COLUMNS_LANDSCAPE_MEDIA"
                )
            }
             */
        }
    }


}