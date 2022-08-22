/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.dot.customizations.model.color

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationManager
import com.dot.customizations.model.CustomizationOption
import com.dot.customizations.model.ResourceConstants
import com.dot.customizations.model.ResourceConstants.OVERLAY_CATEGORY_SYSTEM_PALETTE
import com.dot.customizations.monet.Style
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.stream.Collectors

/**
 * Represents a color choice for the user.
 * This could be a preset color or those obtained from a wallpaper.
 */
abstract class ColorOption protected constructor(
    val mTitle: String, overlayPackages: Map<String, String>, val isDefault: Boolean,
    style: Style, index: Int
) : CustomizationOption<ColorOption> {
    val packagesByCategory: Map<String, String>
    val mPreviewColorIds = intArrayOf(
        R.id.color_preview_0, R.id.color_preview_1,
        R.id.color_preview_2, R.id.color_preview_3
    )
    private val mStyle: Style

    /**
     * @return the index of this color option
     */
    val index: Int
    private var mContentDescription: CharSequence? = null

    init {
        mStyle = style
        this.index = index
        packagesByCategory = Collections.unmodifiableMap(
            overlayPackages.entries.stream().filter { t -> t.value != null }
                .collect(
                    Collectors.toMap(
                        { t -> t.key },
                        { t -> t.value }
                    )
                )
        )
    }

    override fun isActive(customizationManager: CustomizationManager<ColorOption>): Boolean {
        val colorManager = customizationManager as ColorCustomizationManager
        var currentStyle = colorManager.currentStyle
        if (TextUtils.isEmpty(currentStyle)) {
            currentStyle = Style.TONAL_SPOT.toString()
        }
        val isCurrentStyle = TextUtils.equals(style.toString(), currentStyle)
        return if (isDefault) {
            val serializedOverlays = colorManager.storedOverlays
            (TextUtils.isEmpty(serializedOverlays) || EMPTY_JSON == serializedOverlays || colorManager.currentOverlays!!.isEmpty() || !(serializedOverlays.contains(
                OVERLAY_CATEGORY_SYSTEM_PALETTE
            ) || serializedOverlays.contains(
                ResourceConstants.OVERLAY_CATEGORY_COLOR
            ))) && isCurrentStyle
        } else {
            val currentOverlays = colorManager.currentOverlays
            val currentSource = colorManager.currentColorSource
            val isCurrentSource =
                TextUtils.isEmpty(currentSource) || source == currentSource
            isCurrentSource && isCurrentStyle && packagesByCategory == currentOverlays
        }
    }

    /**
     * This is similar to #equals() but it only compares this theme's packages with the other, that
     * is, it will return true if applying this theme has the same effect of applying the given one.
     */
    fun isEquivalent(other: ColorOption?): Boolean {
        if (other == null) {
            return false
        }
        return if (isDefault) {
            other.isDefault || TextUtils.isEmpty(other.serializedPackages) || EMPTY_JSON == other.serializedPackages
        } else packagesByCategory == other.packagesByCategory
        // Map#equals ensures keys and values are compared.
    }

    /**
     * Returns the [PreviewInfo] object for this ColorOption
     */
    abstract val previewInfo: PreviewInfo?
    val serializedPackages: String
        get() = getJsonPackages(false).toString()
    val serializedPackagesWithTimestamp: String
        get() = getJsonPackages(true).toString()

    /**
     * Get a JSONObject representation of this color option, with the current values for each
     * field, and optionally a [TIMESTAMP_FIELD] field.
     * @param insertTimestamp whether to add a field with the current timestamp
     * @return the JSONObject for this color option
     */
    fun getJsonPackages(insertTimestamp: Boolean): JSONObject {
        val json: JSONObject
        if (isDefault) {
            json = JSONObject()
        } else {
            json = JSONObject(packagesByCategory)
            // Remove items with null values to avoid deserialization issues.
            removeNullValues(json)
        }
        if (insertTimestamp) {
            try {
                json.put(TIMESTAMP_FIELD, System.currentTimeMillis())
            } catch (e: JSONException) {
                Log.e(TAG, "Couldn't add timestamp to serialized themebundle")
            }
        }
        return json
    }

    private fun removeNullValues(json: JSONObject) {
        val keys: Iterator<String> = json.keys()
        val keysToRemove: MutableSet<String> = HashSet()
        while (keys.hasNext()) {
            val key = keys.next()
            if (json.isNull(key)) {
                keysToRemove.add(key)
            }
        }
        for (key in keysToRemove) {
            json.remove(key)
        }
    }

    protected open fun getContentDescription(context: Context): CharSequence? {
        if (mContentDescription == null) {
            val defaultName: CharSequence = context.getString(R.string.default_theme_title)
            mContentDescription = if (isDefault) {
                defaultName
            } else {
                mTitle
            }
        }
        return mContentDescription
    }

    /**
     * @return the source of this color option
     */
    @get:ColorOptionsProvider.ColorSource
    abstract val source: String

    /**
     * @return the style of this color option
     */
    val style: Style
        get() = mStyle

    /**
     * The preview information of [ColorOption]
     */
    interface PreviewInfo
    companion object {
        private const val TAG = "ColorOption"
        private const val EMPTY_JSON = "{}"

        @VisibleForTesting
        val TIMESTAMP_FIELD = "_applied_timestamp"
    }
}