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
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import com.dot.customizations.R
import com.dot.customizations.model.color.ColorOptionsProvider.ColorSource
import com.dot.customizations.monet.Style
import java.util.*

/**
 * Represents a seed color obtained from WallpaperColors, for the user to chose as their theming
 * option.
 */
class ColorSeedOption @VisibleForTesting internal constructor(
    title: String?,
    overlayPackages: Map<String, String>,
    isDefault: Boolean,
    @field:ColorSource @param:ColorSource override val source: String,
    style: Style?,
    index: Int,
    override val previewInfo: PreviewInfo
) : ColorOption(title!!, overlayPackages, isDefault, style!!, index) {

    override fun getLayoutResId(): Int {
        return R.layout.color_option
    }

    override fun getTitle(): String {
        return mTitle
    }

    override fun bindThumbnailTile(view: View) {
        val res = view.context.resources
        @ColorInt val colors = previewInfo.resolveColors(res)
        val padding =
            if (view.isActivated) res.getDimensionPixelSize(R.dimen.color_seed_option_tile_padding_selected) else res.getDimensionPixelSize(
                R.dimen.color_seed_option_tile_padding
            )
        for (i in mPreviewColorIds.indices) {
            val colorPreviewImageView = view.findViewById<ImageView>(
                mPreviewColorIds[i]
            )
            colorPreviewImageView.drawable.setColorFilter(colors[i], PorterDuff.Mode.SRC)
            colorPreviewImageView.setPadding(padding, padding, padding, padding)
        }
        view.contentDescription = getContentDescription(view.context)
    }

    override fun getContentDescription(context: Context): CharSequence? {
        // Override because we want all options with the same description.
        return context.getString(R.string.wallpaper_color_title)
    }

    /**
     * The preview information of [ColorOption]
     */
    class PreviewInfo(
        @field:ColorInt @param:ColorInt var lightColors: IntArray,
        @field:ColorInt @param:ColorInt var darkColors: IntArray
    ) : ColorOption.PreviewInfo {
        /**
         * Returns the colors to be applied corresponding with the current
         * configuration's UI mode.
         * @return one of [.lightColors] or [.darkColors]
         */
        @ColorInt
        fun resolveColors(res: Resources): IntArray {
            val night = (res.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    == Configuration.UI_MODE_NIGHT_YES)
            return if (night) darkColors else lightColors
        }
    }

    /**
     * The builder of ColorSeedOption
     */
    class Builder {
        /**
         * Gets title of [ColorOption] object
         * @return title string
         */
        var title: String? = null
            protected set

        @ColorInt
        private lateinit var mLightColors: IntArray

        @ColorInt
        private lateinit var mDarkColors: IntArray

        @ColorSource
        private var mSource: String? = null
        private var mIsDefault = false
        private var mStyle = Style.TONAL_SPOT
        private var mIndex = 0
        protected var mPackages: MutableMap<String, String> = HashMap()

        /**
         * Builds the ColorSeedOption
         * @return new [ColorOption] object
         */
        fun build(): ColorSeedOption {
            return ColorSeedOption(
                title, mPackages, mIsDefault, mSource!!, mStyle, mIndex,
                createPreviewInfo()
            )
        }

        /**
         * Creates preview information
         * @return the [PreviewInfo] object
         */
        fun createPreviewInfo(): PreviewInfo {
            return PreviewInfo(mLightColors, mDarkColors)
        }

        val packages: Map<String?, String?>
            get() = Collections.unmodifiableMap(mPackages)

        /**
         * Sets title of bundle
         * @param title specified title
         * @return this of [ColorBundle.Builder]
         */
        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        /**
         * Sets the colors for preview in light mode
         * @param lightColors  [ColorInt] colors for light mode
         * @return this of [Builder]
         */
        fun setLightColors(@ColorInt lightColors: IntArray): Builder {
            mLightColors = lightColors
            return this
        }

        /**
         * Sets the colors for preview in light mode
         * @param darkColors  [ColorInt] colors for light mode
         * @return this of [Builder]
         */
        fun setDarkColors(@ColorInt darkColors: IntArray): Builder {
            mDarkColors = darkColors
            return this
        }

        /**
         * Sets overlay package for bundle
         * @param category the category of bundle
         * @param packageName tha name of package in the category
         * @return this of [Builder]
         */
        fun addOverlayPackage(category: String, packageName: String): Builder {
            mPackages[category] = packageName
            return this
        }

        /**
         * Sets the source of this color seed
         * @param source typically either [ColorOptionsProvider.COLOR_SOURCE_HOME] or
         * [ColorOptionsProvider.COLOR_SOURCE_LOCK]
         * @return this of [Builder]
         */
        fun setSource(@ColorSource source: String?): Builder {
            mSource = source
            return this
        }

        /**
         * Sets the source of this color seed
         * @param style color style of [Style]
         * @return this of [Builder]
         */
        fun setStyle(style: Style): Builder {
            mStyle = style
            return this
        }

        /**
         * Sets color option index of seed
         * @param index color option index
         * @return this of [ColorBundle.Builder]
         */
        fun setIndex(index: Int): Builder {
            mIndex = index
            return this
        }

        /**
         * Sets as default bundle
         * @return this of [Builder]
         */
        fun asDefault(): Builder {
            mIsDefault = true
            return this
        }
    }
}