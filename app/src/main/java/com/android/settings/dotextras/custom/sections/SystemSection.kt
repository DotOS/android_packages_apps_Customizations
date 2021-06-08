/*
 * Copyright (C) 2020 The dotOS Project
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
package com.android.settings.dotextras.custom.sections

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.os.UserHandle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL_OVERLAY
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SECURE
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SYSTEM
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.system.OverlayController
import kotlin.properties.Delegates

open class SystemSection : GenericSection() {

    private var gesturesCardList: ArrayList<ContextCards> = ArrayList()
    private var securityCardList: ArrayList<ContextCards> = ArrayList()
    private var miscCardList: ArrayList<ContextCards> = ArrayList()
    private lateinit var mDevicePolicyManager: DevicePolicyManager
    private var mFingerprintManager: FingerprintManager? = null
    private var mEncryptionStatus by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_system, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gesturesCardList.clear()
        securityCardList.clear()
        miscCardList.clear()
        addGestures()
        addSecurity()
        addMisc()
        setupLayout(gesturesCardList, R.id.sectionGestures)
        setupLayout(securityCardList, R.id.sectionSecurity)
        setupLayout(miscCardList, R.id.sectionMisc)
    }

    private fun addGestures() {
        buildSwitch(
            gesturesCardList,
            iconID = R.drawable.ic_torch,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.hold_to_torch),
            accentColor = R.color.dot_sky,
            feature = featureManager.Secure().TORCH_POWER_BUTTON_GESTURE,
            featureType = SECURE,
            summary = getString(R.string.hold_to_torch_summary)
        )
        buildSwitch(
            gesturesCardList,
            iconID = R.drawable.ic_touch,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.stb_brightness_title),
            accentColor = R.color.orange_900,
            feature = featureManager.System().STATUS_BAR_BRIGHTNESS_CONTROL,
            featureType = SYSTEM,
            summary = getString(R.string.stb_brightness_summary)
        )
        buildSwitch(
            gesturesCardList,
            iconID = R.drawable.ic_touch,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.statusbar_dt2s),
            accentColor = R.color.dot_violet,
            feature = featureManager.System().DOUBLE_TAP_SLEEP_GESTURE,
            featureType = SYSTEM,
            summary = getString(R.string.statusbar_dt2s_summary),
            enabled = true
        )
        buildSwitch(
            gesturesCardList,
            iconID = R.drawable.ic_touch,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.lockscreen_dt2s),
            accentColor = R.color.dot_teal,
            feature = featureManager.System().DOUBLE_TAP_SLEEP_LOCKSCREEN,
            featureType = SYSTEM,
            summary = getString(R.string.lockscreen_dt2s_summary),
            enabled = true
        )
        buildSwitch(
            gesturesCardList,
            iconID = R.drawable.ic_touch,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.dt_doze),
            accentColor = R.color.dot_grey,
            feature = featureManager.System().DOZE_TRIGGER_DOUBLETAP,
            featureType = SYSTEM,
            summary = getString(R.string.dt_doze_summary),
            enabled = false
        )
        buildSwitch(
            gesturesCardList,
            iconID = R.drawable.ic_arrow_back,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.lrb_title),
            accentColor = R.color.green_800,
            feature = featureManager.System().NAVIGATION_BAR_MENU_ARROW_KEYS,
            featureType = SYSTEM,
            summary = getString(R.string.lrb_summary),
            enabled = false
        )
        buildSwitch(
            gesturesCardList,
            iconID = R.drawable.ic_three_fingers,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.threewayss),
            accentColor = R.color.dot_yellow,
            feature = featureManager.System().SWIPE_TO_SCREENSHOT,
            featureType = SYSTEM,
            summary = getString(R.string.threewayss_summary),
            enabled = false
        )
        buildSwitch(
            gesturesCardList,
            iconID = R.drawable.ic_direction,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.volume_left),
            accentColor = R.color.purple_500,
            feature = featureManager.System().VOLUME_PANEL_ON_LEFT,
            featureType = SYSTEM,
            summary = getString(R.string.volume_left_summary),
            enabled = false
        )
        buildSwipeable(gesturesCardList,
            iconID = R.drawable.ic_swipe,
            subtitle = getString(R.string.no_navbar),
            accentColor = R.color.cyan_500,
            feature = featureManager.Secure().GESTURE_NAVBAR_LENGTH,
            featureType = SECURE,
            min = 0,
            max = 3,
            default = 1,
            summary = getString(R.string.no_navbar_summary),
            extraTitle = getString(R.string.length_size),
            listener = { value ->
                run {
                    when (value) {
                        0 -> overlayManager.setEnabledExclusiveInCategory(
                            OverlayController.Packages.HIDDEN_OVERLAY_PKG,
                            UserHandle.USER_SYSTEM
                        )
                        1 -> {
                            overlayManager.setEnabledExclusiveInCategory(
                                NAV_BAR_MODE_GESTURAL_OVERLAY,
                                UserHandle.USER_SYSTEM
                            )
                            overlayManager.setEnabled(
                                OverlayController.Packages.HIDDEN_OVERLAY_PKG,
                                false,
                                UserHandle.USER_SYSTEM
                            )
                            overlayManager.setEnabled(
                                OverlayController.Packages.NAVBAR_LONG_OVERLAY_PKG,
                                false,
                                UserHandle.USER_SYSTEM
                            )
                            overlayManager.setEnabled(
                                OverlayController.Packages.NAVBAR_MEDIUM_OVERLAY_PKG,
                                false,
                                UserHandle.USER_SYSTEM
                            )
                        }
                        2 -> overlayManager.setEnabledExclusiveInCategory(
                            OverlayController.Packages.NAVBAR_MEDIUM_OVERLAY_PKG,
                            UserHandle.USER_SYSTEM
                        )
                        3 -> overlayManager.setEnabledExclusiveInCategory(
                            OverlayController.Packages.NAVBAR_LONG_OVERLAY_PKG,
                            UserHandle.USER_SYSTEM
                        )
                    }
                }
            },
            sliderListener = { position, title ->
                run {
                    var newTitle = ""
                    when (position) {
                        0 -> newTitle = getString(R.string.nav_hide)
                        1 -> newTitle = getString(R.string.nav_normal)
                        2 -> newTitle = getString(R.string.nav_medium)
                        3 -> newTitle = getString(R.string.nav_large)
                    }
                    title.text = newTitle
                }
            }
        )
    }

    private fun addSecurity() {
        buildSwitch(
            securityCardList,
            iconID = R.drawable.ic_lock,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.pocket_mode),
            accentColor = R.color.dot_blue,
            feature = featureManager.System().POCKET_JUDGE,
            featureType = SYSTEM
        )

        mFingerprintManager =
            requireContext().getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager?

        if (mFingerprintManager?.isHardwareDetected == true) {
            if (!ResourceHelper.hasFodSupport(requireContext())) {
                buildSwitch(
                    securityCardList,
                    iconID = R.drawable.fod_icon_default_aosp,
                    title = getString(R.string.enabled),
                    subtitle = getString(R.string.fp_wake_unlock),
                    accentColor = R.color.deep_orange_800,
                    feature = featureManager.System().FP_WAKE_UNLOCK,
                    featureType = SYSTEM,
                    summary = getString(R.string.fp_wake_unlock_summary),
                    enabled = true
                )
            }

            mDevicePolicyManager =
                requireContext().getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            mEncryptionStatus = mDevicePolicyManager.storageEncryptionStatus
            if (mEncryptionStatus != DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE
                && mEncryptionStatus != DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER
            ) {
                buildSwitch(
                    securityCardList,
                    iconID = R.drawable.fod_icon_default_aosp,
                    title = getString(R.string.disabled),
                    subtitle = getString(R.string.biometrics_unlock),
                    accentColor = R.color.deep_orange_800,
                    feature = featureManager.System().FP_UNLOCK_KEYSTORE,
                    featureType = SYSTEM,
                    summary = getString(R.string.biometrics_unlock_summary),
                    enabled = false
                )
            }
        }
    }

    private fun addMisc() {
        buildSwitch(
            miscCardList,
            iconID = R.drawable.ic_settings_aosp,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.advanced_reboot_title),
            accentColor = R.color.blue_700,
            feature = featureManager.System().ADVANCED_REBOOT,
            featureType = SYSTEM,
            summary = getString(R.string.advanced_reboot_summary),
            enabled = false
        )
        buildSwitch(
            miscCardList,
            iconID = R.drawable.ic_lock,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.media_art_title),
            accentColor = R.color.light_green_A700,
            feature = featureManager.System().LOCKSCREEN_MEDIA_METADATA,
            featureType = SYSTEM,
            summary = getString(R.string.media_art_summary),
            enabled = false
        )
        buildSwitch(
            miscCardList,
            iconID = R.drawable.ic_visualizer,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.music_visualizer_title),
            accentColor = R.color.purple_500,
            feature = featureManager.Secure().LOCKSCREEN_VISUALIZER_ENABLED,
            featureType = SECURE,
            summary = getString(R.string.music_visualizer_summary),
            enabled = false
        )
    }
}