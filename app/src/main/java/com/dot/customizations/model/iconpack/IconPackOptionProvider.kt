/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.dot.customizations.model.iconpack

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.util.Log
import com.dot.customizations.R
import com.dot.customizations.model.ResourceConstants
import com.dot.customizations.model.theme.OverlayManagerCompat

class IconPackOptionProvider(private val mContext: Context, manager: OverlayManagerCompat) {
    private val mPm: PackageManager = mContext.packageManager
    private val mOverlayPackages: MutableList<String>
    private val mOptions: MutableList<IconPackOption> = ArrayList()
    private val mSysUiIconsOverlayPackages: MutableList<String> = ArrayList()
    private val mSettingsIconsOverlayPackages: MutableList<String> = ArrayList()

    init {
        val targetPackages = IconPackOption.OVERLAY_PACKAGES
        mSysUiIconsOverlayPackages.addAll(
            manager.getOverlayPackagesForCategory(
                ResourceConstants.OVERLAY_CATEGORY_ICON_SYSUI, UserHandle.myUserId(), *targetPackages
            )
        )
        mSettingsIconsOverlayPackages.addAll(
            manager.getOverlayPackagesForCategory(
                ResourceConstants.OVERLAY_CATEGORY_ICON_SETTINGS,
                UserHandle.myUserId(),
                *targetPackages
            )
        )
        mOverlayPackages = ArrayList()
        mOverlayPackages.addAll(
            manager.getOverlayPackagesForCategory(
                ResourceConstants.OVERLAY_CATEGORY_ICON_ANDROID,
                UserHandle.myUserId(), *IconPackOption.OVERLAY_PACKAGES
            )
        )
    }

    val options: List<IconPackOption>
        get() {
            if (mOptions.isEmpty()) loadOptions()
            return mOptions
        }

    private fun loadOptions() {
        addDefault()
        val optionsByPrefix: MutableMap<String, IconPackOption> = HashMap()
        for (overlayPackage in mOverlayPackages) {
            val option = addOrUpdateOption(
                optionsByPrefix, overlayPackage,
                ResourceConstants.OVERLAY_CATEGORY_ICON_ANDROID
            )
            try {
                for (iconName in ResourceConstants.ICONS_FOR_PREVIEW) {
                    option!!.addIcon(loadIconPreviewDrawable(iconName, overlayPackage))
                }
            } catch (e: Resources.NotFoundException) {
                Log.w(
                    TAG, String.format(
                        "Couldn't load icon overlay details for %s, will skip it",
                        overlayPackage
                    ), e
                )
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(
                    TAG, String.format(
                        "Couldn't load icon overlay details for %s, will skip it",
                        overlayPackage
                    ), e
                )
            }
        }
        for (overlayPackage in mSysUiIconsOverlayPackages) {
            addOrUpdateOption(
                optionsByPrefix,
                overlayPackage,
                ResourceConstants.OVERLAY_CATEGORY_ICON_SYSUI
            )
        }
        for (overlayPackage in mSettingsIconsOverlayPackages) {
            addOrUpdateOption(
                optionsByPrefix,
                overlayPackage,
                ResourceConstants.OVERLAY_CATEGORY_ICON_SETTINGS
            )
        }
        for (option in optionsByPrefix.values) {
            if (option.isValid()) {
                mOptions.add(option)
            }
        }
    }

    private fun addOrUpdateOption(
        optionsByPrefix: MutableMap<String, IconPackOption>,
        overlayPackage: String, category: String
    ): IconPackOption? {
        val prefix = overlayPackage.substring(0, overlayPackage.lastIndexOf("."))
        var option: IconPackOption? = null
        try {
            if (!optionsByPrefix.containsKey(prefix)) {
                option = IconPackOption(
                    mPm.getApplicationInfo(overlayPackage, 0).loadLabel(mPm).toString()
                )
                optionsByPrefix[prefix] = option
            } else {
                option = optionsByPrefix[prefix]
            }
            option!!.addOverlayPackage(category, overlayPackage)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, String.format("Package %s not found", overlayPackage), e)
        }
        return option
    }

    @Throws(PackageManager.NameNotFoundException::class, Resources.NotFoundException::class)
    private fun loadIconPreviewDrawable(drawableName: String, packageName: String): Drawable {
        val resources =
            if (ResourceConstants.ANDROID_PACKAGE == packageName) Resources.getSystem() else mPm.getResourcesForApplication(
                packageName
            )
        return resources.getDrawable(
            resources.getIdentifier(drawableName, "drawable", packageName), null
        )
    }

    private fun addDefault() {
        val option = IconPackOption(mContext.getString(R.string.default_theme_title))
        try {
            for (iconName in ResourceConstants.ICONS_FOR_PREVIEW) {
                option.addIcon(loadIconPreviewDrawable(iconName, ResourceConstants.ANDROID_PACKAGE))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Didn't find SystemUi package icons, will skip option", e)
        } catch (e: Resources.NotFoundException) {
            Log.w(TAG, "Didn't find SystemUi package icons, will skip option", e)
        }
        option.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_ICON_ANDROID, null)
        option.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_ICON_SYSUI, null)
        option.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_ICON_SETTINGS, null)
        mOptions.add(option)
    }

    companion object {
        private const val TAG = "IconPackOptionProvider"
    }
}