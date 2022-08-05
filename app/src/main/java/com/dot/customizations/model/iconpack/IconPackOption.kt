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

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationManager
import com.dot.customizations.model.CustomizationOption
import com.dot.customizations.model.ResourceConstants
import com.dot.customizations.util.ResourceUtils

class IconPackOption(private val mTitle: String) : CustomizationOption<IconPackOption> {
    private val mIcons: MutableList<Drawable> = ArrayList()

    // Mapping from category to overlay package name
    private val mOverlayPackageNames: MutableMap<String, String?> = HashMap()
    override fun bindThumbnailTile(view: View) {
        val icon = mIcons[THUMBNAIL_ICON_POSITION]
            .constantState!!.newDrawable().mutate()
        var colorFilter = ResourceUtils.getColorAttr(
            view.context,
            android.R.attr.textColorPrimary
        )
        var resId = R.id.icon_section_tile
        if (view.findViewById<View?>(R.id.option_icon) != null) {
            resId = R.id.option_icon
            colorFilter = ResourceUtils.getColorAttr(
                view.context,
                if (view.isActivated) android.R.attr.textColorPrimary else android.R.attr.textColorTertiary
            )
        }
        icon.setColorFilter(colorFilter, PorterDuff.Mode.SRC_ATOP)
        (view.findViewById<View>(resId) as ImageView).setImageDrawable(icon)
        view.contentDescription = mTitle
    }

    override fun isActive(manager: CustomizationManager<IconPackOption>): Boolean {
        val iconManager = manager as IconPackManager
        val overlayManager = iconManager.overlayManager
        if (mTitle == "Default") {
            return overlayManager.getEnabledPackageName(
                ResourceConstants.SYSUI_PACKAGE,
                ResourceConstants.OVERLAY_CATEGORY_ICON_SYSUI
            ) == null && overlayManager.getEnabledPackageName(
                ResourceConstants.SETTINGS_PACKAGE, ResourceConstants.OVERLAY_CATEGORY_ICON_SETTINGS
            ) == null && overlayManager.getEnabledPackageName(
                ResourceConstants.ANDROID_PACKAGE, ResourceConstants.OVERLAY_CATEGORY_ICON_ANDROID
            ) == null
        }
        for ((key, value) in overlayPackages) {
            if (value == null || value != overlayManager.getEnabledPackageName(
                    determinePackage(key),
                    key
                )
            ) {
                return false
            }
        }
        return true
    }

    override fun getLayoutResId(): Int {
        return R.layout.theme_icon_option
    }

    override fun getTitle(): String {
        return mTitle
    }

    fun bindPreview(container: ViewGroup) {
        val cardBody =
            container.findViewById<ViewGroup>(R.id.theme_preview_card_body_container)
        if (cardBody.childCount == 0) {
            LayoutInflater.from(container.context).inflate(
                R.layout.preview_card_icon_content, cardBody, true
            )
        }
        var i = 0
        while (i < mIconIds.size && i < mIcons.size) {
            (container.findViewById<View>(mIconIds[i]) as ImageView).setImageDrawable(
                mIcons[i]
            )
            i++
        }
    }

    private fun determinePackage(category: String): String? {
        return when (category) {
            ResourceConstants.OVERLAY_CATEGORY_ICON_SYSUI -> ResourceConstants.SYSUI_PACKAGE
            ResourceConstants.OVERLAY_CATEGORY_ICON_SETTINGS -> ResourceConstants.SETTINGS_PACKAGE
            ResourceConstants.OVERLAY_CATEGORY_ICON_ANDROID -> ResourceConstants.ANDROID_PACKAGE
            else -> null
        }
    }

    fun addIcon(previewIcon: Drawable) {
        mIcons.add(previewIcon)
    }

    fun addOverlayPackage(category: String, overlayPackage: String?) {
        mOverlayPackageNames[category] = overlayPackage
    }

    val overlayPackages: Map<String, String?>
        get() = mOverlayPackageNames

    /**
     * @return whether this icon option has overlays and previews for all the required packages
     */
    fun isValid(): Boolean {
        return mOverlayPackageNames.keys.size == 3
    }

    companion object {
        const val THUMBNAIL_ICON_POSITION = 0
        private val mIconIds = intArrayOf(
            R.id.preview_icon_0,
            R.id.preview_icon_1,
            R.id.preview_icon_2,
            R.id.preview_icon_3,
            R.id.preview_icon_4,
            R.id.preview_icon_5
        )
        val OVERLAY_PACKAGES = arrayOf(
            ResourceConstants.ANDROID_PACKAGE,
            ResourceConstants.SETTINGS_PACKAGE,
            ResourceConstants.SYSUI_PACKAGE
        )
    }
}